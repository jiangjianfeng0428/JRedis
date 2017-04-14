package io.mycat.jcache.nio.buffer;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by jiangjf2 on 2017/4/14.
 */
public class WriteByteBufferQueueTest extends TestCase {

    @Test
    public void testReadFromBytes(){
        WriteByteBufferQueue queue = new WriteByteBufferQueue(new ByteBufferPool(), 5, 10);
        int result = queue.readFromBytes("test".getBytes());
        assertEquals(result, 4);

        result = queue.readFromBytes("test".getBytes());
        assertEquals(result, 4);

        result = queue.readFromBytes("hello".getBytes());
        assertEquals(result, 5);
        assertEquals(queue.queue.size(), 3);
    }
}
