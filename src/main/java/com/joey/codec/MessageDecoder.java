package com.joey.codec;

import com.joey.common.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by joey on 14-7-4.
 */
public class MessageDecoder extends ByteToMessageDecoder {
    private final static Logger logger = Logger.getLogger(MessageDecoder.class);

    private Protocol protocol = null;
    private int lenToRead = 0;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        if (protocol == null) {
            if (in.readableBytes() < 4) {
                return;
            }

            protocol = new Protocol();
            protocol.len = Integer.reverseBytes(in.readInt());
            if ((protocol.len < Protocol.PROTO_MIN_LEN) || (protocol.len > Protocol.PROTO_MAX_LEN)) {
                throw new InvalidProtoException("ProtoLen Must >= 18B && <= 1M: " + protocol.len);
            }
            lenToRead = protocol.len - 4;
        }

        if (in.readableBytes() >= lenToRead) {
            protocol.seq = Integer.reverseBytes(in.readInt());
            protocol.hash = Integer.reverseBytes(in.readInt());
            protocol.cmd = (Integer.reverseBytes(in.readUnsignedShort()) >>> 16);
            protocol.ret = Short.reverseBytes(in.readShort());
            protocol.hlen = Short.reverseBytes(in.readShort());

            int bodyLen = protocol.len - Protocol.PROTO_MIN_LEN;
            if (bodyLen > 0) {
                protocol.data = new byte[bodyLen];
                in.readBytes(protocol.data);
            }
            out.add(protocol);

            protocol = null;
            lenToRead = 0;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        logger.error(cause.getMessage(), cause);

        protocol = null;
        lenToRead = 0;
        ctx.close();
    }

    @SuppressWarnings("serial")
    private static class InvalidProtoException extends RuntimeException {
        public InvalidProtoException(String msg)
        {
            super(msg);
        }
    }

}
