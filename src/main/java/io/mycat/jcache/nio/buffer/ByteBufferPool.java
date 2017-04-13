package io.mycat.jcache.nio.buffer;

import java.nio.ByteBuffer;

/**
 * TODO: 替换为DirectByteBufferPool
 *
 * Created by jiangjf2 on 2017/4/13.
 */
public class ByteBufferPool {
    public ByteBuffer allocate(int size) {
        return ByteBuffer.allocate(size);
    }


    public void recycle(ByteBuffer theBuf) {
    }
}
