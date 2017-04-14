package io.mycat.jcache.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by jiangjf2 on 2017/4/14.
 */
public class ReadByteBufferQueue extends ByteBufferQueue {
    private int readPosition = 0;
    private int currentBufferNum;
    private ByteBuffer readBuffer;

    public ReadByteBufferQueue(ByteBufferPool bufferPool) {
        super(bufferPool);
    }

    public ReadByteBufferQueue(ByteBufferPool bufferPool, int bufferSize, int maxBufferNum) {
        super(bufferPool, bufferSize, maxBufferNum);
    }

    public int readFromChannel(SocketChannel channel) throws IOException {
        int size, total = 0;
        ByteBuffer buf;
        do{
            if((buf = getWriteBuffer()) != null) {
                size = channel.read(buf);
                if(size > 0) {
                    total += size;
                }else if(size == -1){
                    return -1;
                }
            }else{
                break;
            }
        }while(size > 0);

        return total;
    }

    /**
     * 查询ByteBufferQueue中首次匹配给定字节数组的位置
     *
     * @param targetBytes 匹配目标
     * @return ByteBufferQueue中的位置
     */
    public int indexOf(byte[] targetBytes){
        int start = readPosition;
        int stop = (queue.size() - 1) * bufferSize + queue.getLast().limit() - targetBytes.length + 1;
        for(int i = start; i < stop; i++){
            int j = 0;
            for(; j < targetBytes.length; j++){
                if(targetBytes[j] != get(i + j)){
                    break;
                }
            }

            if(j == targetBytes.length){
                return i;
            }
        }

        return -1;
    }

    /**
     * 从ByteBufferQueue中读取指定长度的字节
     * @param bytes 目标字节数组
     * @return 字节数组
     */
    public byte[] writeToBytes(byte[] bytes){
        Iterator<ByteBuffer> iterator = queue.iterator();
        int index = 0, copyLength = bufferSize - readPosition, length = bytes.length;
        while(length > copyLength){
            if(iterator.hasNext()){
                ByteBuffer buf = iterator.next();
                System.arraycopy(buf.array(), readPosition, bytes, index, copyLength);
                iterator.remove();
                readPosition = 0;
                index += copyLength;
                length -= copyLength;
                copyLength = bufferSize - readPosition;
            }else{
                return bytes;
            }
        }

        if(iterator.hasNext()){
            ByteBuffer buf = iterator.next();
            System.arraycopy(buf.array(), readPosition, bytes, index, length);
            readPosition += length;

            if(readPosition == bufferSize){
                buf.clear();
            }
        }

        return bytes;
    }

    public int readPosition(){
        return this.readPosition;
    }

    private byte get(int index){
        int bufNum = index / bufferSize;
        if(readBuffer == null || bufNum != currentBufferNum){
            readBuffer = queue.get(bufNum);
            currentBufferNum = bufNum;
        }
        return readBuffer.get(index % bufferSize);
    }
}
