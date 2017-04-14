package io.mycat.jcache.nio.handler;

import io.mycat.jcache.nio.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * Created by jiangjf2 on 2017/4/14.
 */
public class StringHandler extends NioHandler {
    private Logger logger = LoggerFactory.getLogger(StringHandler.class);

    private final Charset charset;

    public StringHandler(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void handle(Connection conn, Object obj) {
        logger.debug("handle message: {}", obj);
        String message = new String((byte[])obj, charset);
        super.handle(conn, message);
    }
}
