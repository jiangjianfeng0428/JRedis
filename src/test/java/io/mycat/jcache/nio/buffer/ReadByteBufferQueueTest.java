package io.mycat.jcache.nio.buffer;

import junit.framework.TestCase;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by jiangjf2 on 2017/4/14.
 */
public class ReadByteBufferQueueTest extends TestCase{
    private ReadByteBufferQueue queue;

    @Override
    protected void setUp() throws Exception {
        queue = new ReadByteBufferQueue(new ByteBufferPool(), 3, 10);
        queue.queue.removeFirst();
        queue.queue.add(ByteBuffer.wrap(new byte[]{0X1, 0X2, 0X3}));
        queue.queue.add(ByteBuffer.wrap(new byte[]{0X4, 0X5, 0X6}));
        queue.queue.add(ByteBuffer.wrap(new byte[]{0X7, 0X8}));
    }

    @Test
    public void testWriteToBytes(){
        byte[] result = new byte[5];
        queue.writeToBytes(result);
        assertEquals(result.length, 5);
        assertEquals(result[0], 0X1);
        assertEquals(result[1], 0X2);
        assertEquals(result[2], 0X3);
        assertEquals(result[3], 0X4);
        assertEquals(result[4], 0X5);

        result = new byte[2];
        queue.writeToBytes(result);
        assertEquals(result.length, 2);
        assertEquals(result[0], 0X6);
        assertEquals(result[1], 0X7);

        result = new byte[1];
        queue.writeToBytes(result);
        assertEquals(result.length, 1);
        assertEquals(result[0], 0X8);
    }

    @Test
    public void testIndexOf(){
        int index = queue.indexOf(new byte[]{0X1});
        assertEquals(index, 0);
        index = queue.indexOf(new byte[]{0X5});
        assertEquals(index, 4);
        index = queue.indexOf(new byte[]{0X8});
        assertEquals(index, 7);
        index = queue.indexOf(new byte[]{0X9});
        assertEquals(index, -1);

        index = queue.indexOf(new byte[]{0X1, 0X2});
        assertEquals(index, 0);
        index = queue.indexOf(new byte[]{0X5, 0X6});
        assertEquals(index, 4);
        index = queue.indexOf(new byte[]{0X7, 0X8});
        assertEquals(index, 6);
        index = queue.indexOf(new byte[]{0X5, 0X7});
        assertEquals(index, -1);

        index = queue.indexOf(new byte[]{0X2, 0X3, 0X4, 0X5, 0X6, 0X7});
        assertEquals(index, 1);
        index = queue.indexOf(new byte[]{0X3, 0X4, 0X5, 0X6, 0X7, 0X8});
        assertEquals(index, 2);

        index = queue.indexOf(new byte[]{0X1, 0X2, 0X3, 0X4, 0X5, 0X6, 0X7, 0X8});
        assertEquals(index, 0);

        queue.writeToBytes(new byte[2]);
        index = queue.indexOf(new byte[]{0X1, 0X2, 0X3, 0X4, 0X5, 0X6, 0X7, 0X8});
        assertEquals(index, -1);
        index = queue.indexOf(new byte[]{0X1, 0X2});
        assertEquals(index, -1);
        index = queue.indexOf(new byte[]{0X3, 0X4, 0X5, 0X6, 0X7, 0X8});
        assertEquals(index, 2);
    }

    @Test
    public void testReadData(){
        byte[] targetBytes = new byte[]{0X2, 0X3, 0X4};
        int index = queue.indexOf(targetBytes);
        byte[] result = new byte[index - queue.readPosition() + targetBytes.length];
        queue.writeToBytes(result);
        assertEquals(result.length, 4);
        assertEquals(result[0], 0X1);
        assertEquals(result[1], 0X2);
        assertEquals(result[2], 0X3);
        assertEquals(result[3], 0X4);

        targetBytes = new byte[]{0X7};
        index = queue.indexOf(targetBytes);
        result = new byte[index - queue.readPosition() + targetBytes.length];
        queue.writeToBytes(result);
        assertEquals(result.length, 3);
        assertEquals(result[0], 0X5);
        assertEquals(result[1], 0X6);
        assertEquals(result[2], 0X7);
    }
}
