package io.mycat.jcache.nio;

import io.mycat.jcache.nio.buffer.ByteBufferQueue;
import io.mycat.jcache.nio.handler.NioHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by jiangjf2 on 2017/4/11.
 */
public class Connection {
    private Logger logger = LoggerFactory.getLogger(Connection.class);

    private final SocketChannel channel;
    private SelectionKey processKey;
    private State state = State.CONNECTING;
    private ByteBufferQueue readBufQueue;
    private ByteBufferQueue writeBufQueue;
    private NioHandler handler;

    public enum State{
        CONNECTING, CONNECTED, CLOSING, CLOSED, FAILED
    }

    public Connection(SocketChannel channel) {
        this.channel = channel;
    }

    /**
     * 注册新建连接
     *
     * @param selector
     * @throws IOException
     */
    public void register(Selector selector) throws IOException{
        this.processKey = this.channel.register(selector, SelectionKey.OP_READ, this);
//        this.readBufQueue = new ByteBufferQueue();
//        this.writeBufQueue
        if(this.handler != null) {
            this.handler.onConnected(this);
        }
    }

    public void read() throws IOException{
        int size, total = 0;
        do{
            size = channel.read(this.readBufQueue.getWriteBuffer());
            total += size;
        }while(size > 0);

        if(size == -1){
            close("client closed");
            return;
        }

        if(total > 0 && this.handler != null){
            this.handler.handle(readBufQueue);
        }

        this.readBufQueue.compact();
    }

    public void write(){

    }

    public void write(byte[] bytes){

    }

    /**
     * 设置处理器
     *
     * @param handler nio处理器
     * @return nio处理器
     */
    public NioHandler addHandler(NioHandler handler){
        this.handler = handler;
        return this.handler;
    }

    /**
     * 关闭连接
     *
     * @param reason 关闭原因
     */
    public void close(String reason){
        if(channel != null){
            try{
                if(processKey != null && processKey.isValid()){
                    processKey.channel();
                }
                channel.close();
            }catch (Throwable e){
                logger.warn("[{}]: {}", this, e.getMessage());
            }
        }

        if(this.readBufQueue != null){
            this.readBufQueue.recycle();
            this.readBufQueue = null;
        }

        if(this.writeBufQueue != null){
            this.writeBufQueue.recycle();
            this.writeBufQueue = null;
        }

        if(this.handler != null){
            this.handler.onClosed(reason);
            this.handler = null;
        }
    }
}
