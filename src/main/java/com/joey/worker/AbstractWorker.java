package com.joey.worker;

import com.joey.common.Protocol;
import com.joey.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

/**
 * Created by joey on 14-7-2.
 */
public abstract class AbstractWorker {
    private final static Logger logger = Logger.getLogger(AbstractWorker.class);

    protected Protocol protocol;

    @Deprecated
    protected void write(ChannelHandlerContext ctx, Protocol header, ByteBuf msgBody)  {
        if (ctx == null || ctx.isRemoved()) {
            logger.error("ChannelHandlerContext is null or be removed");
            return ;
        }
        byte[] buffer = msgBody.array();
        short len = (short)buffer.length;
        byte[] lengthBytes = Utils.packProtocolHeader(header);
        ByteBuf writeBuffer = ctx.alloc().directBuffer(len+lengthBytes.length);
        writeBuffer.writeBytes(lengthBytes);
        writeBuffer.writeBytes(buffer);

        if (buffer != null) {
            final ChannelFuture writeResp;
            synchronized (ctx) {
                writeResp = ctx.writeAndFlush(writeBuffer);
                writeResp.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        assert writeResp == future;
                        logger.info("Write data back to client completed !");
                    }
                });
            }
            if (writeResp == null) {
                logger.error("write data back to client channel error.");
                ctx.close();
            }
        } else {
        }
    }
}
