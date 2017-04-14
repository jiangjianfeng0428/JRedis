package io.mycat.jcache.nio.handler;

import io.mycat.jcache.nio.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jiangjf2 on 2017/4/13.
 */
public class RespHandler extends NioHandler {
    private Logger logger = LoggerFactory.getLogger(RespHandler.class);

    @Override
    public void onConnected(Connection conn) {
        logger.debug("new client connect");
        conn.write("Hello ...\r\n".getBytes());
    }

    @Override
    public void handle(Connection conn, Object obj) {
        logger.debug("handle message: {}", obj);

        conn.write("message received.\r\n".getBytes());
    }

    @Override
    public void onClosed(String reason) {
        logger.debug("connection closed: {}", reason);
    }
}
