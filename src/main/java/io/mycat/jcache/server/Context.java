package io.mycat.jcache.server;

import io.mycat.jcache.nio.buffer.ByteBufferPool;

/**
 * Created by jiangjf2 on 2017/4/13.
 */
public class Context {
    private static final Context context;
    static{
        context = new Context(new ByteBufferPool());
    }

    private final ByteBufferPool bufferPool;

    public Context(ByteBufferPool bufferPool) {
        this.bufferPool = bufferPool;
    }

    public static Context getInstance(){
        return context;
    }

    public ByteBufferPool getByteBufferPool(){
        return this.bufferPool;
    }
}
