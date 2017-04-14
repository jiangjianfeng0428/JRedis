package io.mycat.jcache.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by jiangjf2 on 2017/4/11.
 */
public class NioReactor extends Thread{
    private Logger logger = LoggerFactory.getLogger(NioReactor.class);

    private final String name;
    private final Selector selector;
    private final ConcurrentLinkedQueue<Connection> registerQueue;

    public NioReactor(String name) throws IOException{
        super(name);
        this.name = name;
        this.selector = Selector.open();
        this.registerQueue = new ConcurrentLinkedQueue<>();
    }

    public void postRegister(Connection conn){
        this.registerQueue.offer(conn);
        this.selector.wakeup();
    }

    @Override
    public void run() {
        Set<SelectionKey> keys;
        while(true){
            try{
                register(this.selector);

                if(selector.select(500L) == 0){
                    continue;
                }

                keys = selector.selectedKeys();
                keys.forEach((key)->{
                    if(key.isValid() && key.attachment() != null){
                        Connection conn = (Connection)key.attachment();
                        if(key.isReadable()){
                            logger.debug("receive read event.");
                            try{
                                conn.read();
                            }catch (Throwable e){
                                logger.warn("[{}]: {}", this.name, e.getMessage());
                                conn.close("program err: " + e.getMessage());
                            }
                        }else if(key.isWritable()){
                            logger.debug("receive write event.");
                            try{
                                conn.write();
                            }catch (Throwable e){
                                logger.warn("[{}]: {}", this.name, e.getMessage());
                                conn.close("program err: " + e.getMessage());
                            }
                        }
                    }else{
                        key.channel();
                    }
                });
                keys.clear();
            }catch (IOException e){
                logger.warn("[{}]: {}", this.name, e.getMessage());
            }
        }
    }

    private void register(Selector selector){
        if(registerQueue.isEmpty()){
            return;
        }

        Connection conn;
        while((conn = registerQueue.poll()) != null){
            try{
                conn.register(selector);
            }catch (Throwable e){
                logger.warn("[register error] {}", e.getMessage());
                conn.close("register error");
            }
        }
    }
}
