package io.mycat.jcache.nio.handler;

import io.mycat.jcache.nio.Connection;
import io.mycat.jcache.nio.buffer.ReadByteBufferQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by jiangjf2 on 2017/4/14.
 */
public class DelimiterHandler extends NioHandler {
    private Logger logger = LoggerFactory.getLogger(DelimiterHandler.class);

    private final byte[] delimiter;

    public DelimiterHandler(byte[] delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public void handle(Connection conn, Object obj) {
        logger.debug("handle message: {}", obj);
        ReadByteBufferQueue queue = (ReadByteBufferQueue)obj;
        int index = queue.indexOf(delimiter);
        if(index > 0) {
            byte[] data = new byte[queue.indexOf(delimiter) - queue.readPosition() + delimiter.length];
            queue.writeToBytes(data);
            conn.write("\r\n".getBytes());
            super.handle(conn, data);
        }
    }
}
