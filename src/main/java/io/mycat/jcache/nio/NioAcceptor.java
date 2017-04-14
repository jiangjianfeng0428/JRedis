package io.mycat.jcache.nio;

import io.mycat.jcache.nio.handler.DelimiterHandler;
import io.mycat.jcache.nio.handler.RespHandler;
import io.mycat.jcache.nio.handler.StringHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * Created by jiangjf2 on 2017/4/11.
 */
public class NioAcceptor extends Thread{
    private Logger logger = LoggerFactory.getLogger(NioAcceptor.class);

    private final String name;
    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final NioReactor reactor;

    public NioAcceptor(String name, String ip, int port, NioReactor reactor) throws IOException{
        super(name);
        this.name = name;
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.configureBlocking(false);
        this.serverChannel.bind(new InetSocketAddress(ip, port));
        this.serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        this.reactor = reactor;
    }

    @Override
    public void run() {
        try{
            logger.info("server started at {}", serverChannel.getLocalAddress());
        }catch (Throwable e){
            // to do nothing
        }

        while(true){
            try{
                if(this.selector.select(500L) == 0){
                    continue;
                }

                Set<SelectionKey> keys = selector.selectedKeys();
                try {
                    keys.forEach((key)->{
                        if (key.isValid() && key.isAcceptable()) {
                            accept();
                        } else {
                            key.cancel();
                        }
                    });
                } finally {
                    keys.clear();
                }
            }catch (Throwable e){
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 接受新连接
     */
    private void accept(){
        SocketChannel channel = null;
        try{
            channel = serverChannel.accept();
            channel.configureBlocking(false);
            Connection conn = new Connection(channel);
            conn.addHandler(new DelimiterHandler(new byte[]{0X0D, 0X0A}))
                    .addHandler(new StringHandler(Charset.forName("utf-8")))
                    .addHandler(new RespHandler());
            this.reactor.postRegister(conn);
        }catch (Throwable e){
            logger.warn("[{}]: {}", this.name, e.getMessage());
            closeChannel(channel);
        }
    }

    private void closeChannel(SocketChannel channel){
        if(channel == null) {
            return;
        }

        Socket socket = channel.socket();
        if(socket != null && !socket.isClosed()){
            try{
                socket.close();
            }catch (IOException e){
                logger.warn("[{}]: {}", this.name, e.getMessage());
            }
        }

        try{
            channel.close();
        }catch (IOException e){
            logger.warn("[{}]: {}", this.name, e.getMessage());
        }
    }
}
