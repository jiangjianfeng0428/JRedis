package io.mycat.jcache.server;

import io.mycat.jcache.nio.NioAcceptor;
import io.mycat.jcache.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by jiangjf2 on 2017/4/11.
 */
public class Bootstrap {
    public static void main(String[] args) throws IOException{
        NioReactor reactor = new NioReactor("jredis-reactor");
        NioAcceptor acceptor = new NioAcceptor("jredis-acceptor", "127.0.0.1", 6379
                , reactor);
        acceptor.start();
        reactor.start();
    }
}
