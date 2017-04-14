package io.mycat.jcache.nio.handler;

import io.mycat.jcache.nio.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jiangjf2 on 2017/4/11.
 */
public abstract class NioHandler {
    private Logger logger = LoggerFactory.getLogger(NioHandler.class);

    protected NioHandler handler;

    public NioHandler addHandler(NioHandler handler){
        this.handler = handler;
        return this.handler;
    }

    public void onConnected(Connection conn){
        if(this.handler != null){
            this.handler.onConnected(conn);
        }
    }

    public void handle(Connection conn, Object obj){
        if(this.handler != null){
            this.handler.handle(conn, obj);
        }
    }

    public void onError(){
        if(this.handler != null){
            this.handler.onError();
        }
    }

    public void onClosed(String reason){
        if(this.handler != null){
            this.handler.onClosed(reason);
        }
    }
}
