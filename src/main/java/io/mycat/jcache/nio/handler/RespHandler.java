package io.mycat.jcache.nio.handler;

import io.mycat.jcache.nio.Connection;
import io.mycat.jcache.nio.NioAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jiangjf2 on 2017/4/13.
 */
public class RespHandler extends NioHandler {
    private Logger logger = LoggerFactory.getLogger(NioAcceptor.class);

    @Override
    public void onConnected(Connection conn) {
        logger.debug("new client connect");
    }

    @Override
    public void handle(Object obj) {
        logger.debug("handle message: {}", obj);
    }

    @Override
    public void onClosed(String reason) {
        logger.debug("connection closed: {}", reason);
    }
}
