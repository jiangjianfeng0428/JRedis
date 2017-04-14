package io.mycat.jcache.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by jiangjf2 on 2017/4/14.
 */
public class WriteByteBufferQueue extends ByteBufferQueue {
    public WriteByteBufferQueue(ByteBufferPool bufferPool) {
        super(bufferPool);
    }

    public WriteByteBufferQueue(ByteBufferPool bufferPool, int bufferSize, int maxBufferNum) {
        super(bufferPool, bufferSize, maxBufferNum);
    }

    public synchronized int readFromBytes(byte[] bytes){
        int size = 0, total = bytes.length;

        do{
            final ByteBuffer buf = getWriteBuffer();
            if(buf != null) {
                if(buf.remaining() >= total){
                    System.arraycopy(bytes, size, buf.array(), buf.position(), total);
                    size += total;
                    buf.position(buf.position() + total);
                }else{
                    System.arraycopy(bytes, size, buf.array(), buf.position(), buf.remaining());
                    size += buf.remaining();
                    buf.position(buf.position() + buf.remaining());
                }
                total -= size;
            }else{
                break;
            }
        }while(total > 0);

        return size;
    }

    public synchronized int writeToChannel(SocketChannel channel) throws IOException {
        ByteBuffer buf = this.queue.getFirst();
        buf.flip();
        int size, total = 0;
        while(buf.hasRemaining()){
            size = channel.write(buf);
            total += size;

            if(size == 0){
                break;
            }else{
                if(!buf.hasRemaining()){
                    if(this.queue.size() > 1){
                        queue.removeFirst();
                        buf = queue.getFirst();
                        buf.flip();
                    }else{
                        buf.position(0);
                        break;
                    }
                }
            }
        }

        return total;
    }

    public boolean hasMoreData(){
        if(this.queue.size() > 1){
            return true;
        }else{
            return this.queue.getFirst().position() > 0;
        }
    }
}
