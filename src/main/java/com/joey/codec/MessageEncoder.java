package com.joey.codec;

import com.joey.common.Protocol;
import com.joey.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.log4j.Logger;

/**
 * Created by joey on 14-7-4.
 */
public class MessageEncoder extends MessageToByteEncoder<Protocol> {
    private final static Logger logger = Logger.getLogger(MessageEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Protocol protocol, ByteBuf byteBuf) throws Exception {
        if (protocol == null) {
            logger.error("Write back Protocol is null");
            return;
        }
        byte[] body = protocol.data;
        byte[] lengthBytes = Utils.packProtocolHeader(protocol);
        byteBuf.writeBytes(lengthBytes);
        if (body != null) {
            byteBuf.writeBytes(body);
        } else {
            logger.info("Data buffer is empty....");
        }
    }

}