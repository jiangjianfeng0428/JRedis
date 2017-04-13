package io.mycat.jcache.nio.buffer;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.IntStream;

/**
 * Created by jiangjf2 on 2017/4/11.
 */
public class ByteBufferQueue {
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final int MAX_BUFFER_NUM = 10;
    private final ByteBufferPool bufferPool;
    private final LinkedList<ByteBuffer> queue;
    private final int bufferSize;
    private int readPosition = 0;

    public ByteBufferQueue(ByteBufferPool bufferPool) {
        this(bufferPool, DEFAULT_BUFFER_SIZE);
    }

    public ByteBufferQueue(ByteBufferPool bufferPool, int bufferSize) {
        this.bufferPool = bufferPool;
        this.bufferSize = bufferSize;
        this.queue = new LinkedList<>();
        this.queue.addLast(this.bufferPool.allocate(this.bufferSize));
    }

    public ByteBuffer getWriteBuffer(){
        ByteBuffer buf = this.queue.getLast();
        if(buf.hasRemaining()){
            return buf;
        }

        if(queue.size() > MAX_BUFFER_NUM){
            return null;
        }

        buf = this.bufferPool.allocate(this.bufferSize);
        queue.addLast(buf);
        return buf;
    }

    public void compact(){
        IntStream.range(0, readPosition / bufferSize).forEach((i)->{
            if(queue.size() > 1){
                queue.removeFirst();
            }
        });

        compact(queue.getLast(), readPosition % bufferSize);
    }

    public void recycle(){
        Iterator<ByteBuffer> iterator = queue.iterator();
        while(iterator.hasNext()){
            this.bufferPool.recycle(iterator.next());
            iterator.remove();
        }
    }

    private void compact(ByteBuffer buf, int pos){
        if(pos == 0){
            buf.clear();
        }else{
            buf.position(0);
            buf.limit(pos);
            buf.compact();
        }
    }

    private void recycle(ByteBuffer buffer){
        this.bufferPool.recycle(buffer);
    }
}
