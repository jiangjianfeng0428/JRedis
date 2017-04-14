package io.mycat.jcache.nio;

import io.mycat.jcache.nio.buffer.ReadByteBufferQueue;
import io.mycat.jcache.nio.buffer.WriteByteBufferQueue;
import io.mycat.jcache.nio.handler.NioHandler;
import io.mycat.jcache.server.Context;
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
    private ReadByteBufferQueue readBufQueue;
    private WriteByteBufferQueue writeBufQueue;
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
        this.readBufQueue = new ReadByteBufferQueue(Context.getInstance().getByteBufferPool());
        this.writeBufQueue = new WriteByteBufferQueue(Context.getInstance().getByteBufferPool());
        if(this.handler != null) {
            this.handler.onConnected(this);
        }
    }

    public void read() throws IOException{
        int size = this.readBufQueue.readFromChannel(this.channel);

        if(size == -1){
            close("client closed");
            return;
        }

        if(size > 0 && this.handler != null){
            logger.debug("read {} bytes from channel.", size);
            this.handler.handle(this, readBufQueue);
        }
    }

    public void write() throws IOException{
        int size = this.writeBufQueue.writeToChannel(this.channel);
        logger.debug("write {} bytes to channel.", size);

        if(this.writeBufQueue.hasMoreData()){
            if ((processKey.isValid() && (processKey.interestOps() & SelectionKey.OP_WRITE) == 0)) {
                enableWrite(false);
            }
        }else{
            if ((processKey.isValid() && (processKey.interestOps() & SelectionKey.OP_WRITE) != 0)) {
                disableWrite();
            }
        }
    }

    public void write(byte[] bytes){
        this.writeBufQueue.readFromBytes(bytes);
        enableWrite(true);
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

    private void disableWrite() {
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
        } catch (Exception e) {
            logger.warn("can't disable write {}", e);
        }

    }

    private void enableWrite(boolean wakeup) {
        boolean needWakeup = false;
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            needWakeup = true;
        } catch (Exception e) {
            logger.warn("can't enable write " + e);

        }

        if (needWakeup && wakeup) {
            processKey.selector().wakeup();
        }
    }
}
