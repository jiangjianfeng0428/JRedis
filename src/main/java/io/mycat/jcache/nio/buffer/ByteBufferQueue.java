package io.mycat.jcache.nio.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by jiangjf2 on 2017/4/11.
 */
public abstract class ByteBufferQueue {
    private Logger logger = LoggerFactory.getLogger(ByteBufferQueue.class);

    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final int DEFAULT_MAX_BUFFER_NUM = 10;
    private final ByteBufferPool bufferPool;
    protected final LinkedList<ByteBuffer> queue;
    protected final int bufferSize;
    private final int maxBufferNum;

    public ByteBufferQueue(ByteBufferPool bufferPool) {
        this(bufferPool, DEFAULT_BUFFER_SIZE, DEFAULT_MAX_BUFFER_NUM);
    }

    public ByteBufferQueue(ByteBufferPool bufferPool, int bufferSize, int maxBufferNum) {
        this.bufferPool = bufferPool;
        this.bufferSize = bufferSize;
        this.maxBufferNum = maxBufferNum;
        this.queue = new LinkedList<>();
        this.queue.addLast(this.bufferPool.allocate(this.bufferSize));
    }

    public void recycle(){
        Iterator<ByteBuffer> iterator = queue.iterator();
        while(iterator.hasNext()){
            this.bufferPool.recycle(iterator.next());
            iterator.remove();
        }
    }

    protected ByteBuffer getWriteBuffer(){
        ByteBuffer buf = this.queue.getLast();
        if(buf.hasRemaining()){
            return buf;
        }

        if(queue.size() > maxBufferNum){
            logger.error("can not write anymore.");
            return null;
        }

        buf = this.bufferPool.allocate(this.bufferSize);
        queue.addLast(buf);
        return buf;
    }
}
