package io.mycat.jcache.nio;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by jiangjf2 on 2017/4/13.
 */
public class NioAcceptorTest extends TestCase{
    @Test
    public void testNioAcceptor() throws IOException{
        NioReactor reactor = new NioReactor("testReactor");
        NioAcceptor acceptor = new NioAcceptor("testAcceptor", "127.0.0.1", 6379, reactor);
        acceptor.start();
        System.out.println(acceptor);
    }
}
