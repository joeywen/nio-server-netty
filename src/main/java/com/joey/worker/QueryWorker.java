package com.joey.worker;

import com.joey.common.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * A single thread process every impala query and write back the query result
 *
 * Created by joey on 14-7-2.
 */
public class QueryWorker extends AbstractWorker implements Callable<Protocol> {
    private final Logger logger = LoggerFactory.getLogger(QueryWorker.class);

    public QueryWorker(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    /**
     * main method to execute the query command, pack the result
     * and write it back to client
     */
    public Protocol call() throws Exception {
        ChannelHandlerContext ctx = (ChannelHandlerContext) this.protocol.attachment();

        // TODO invoke impala/Hive query method, process message
        // message = invoke_impala_method(cmd)
        ChannelFuture future = ctx.writeAndFlush(this.protocol);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isDone() && future.isSuccess()) {
                    logger.info("Write data back finished successful.");
                } else if (future.isDone() && future.isCancelled()) {
                    logger.info("Write data back failed .");
                }
            }
        });
        return null;
    }

}
