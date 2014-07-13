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

    private Protocol proto_ = null;
    private int lenToRead_ = 0;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        if (proto_ == null) {
            if (in.readableBytes() < 4) {
                return;
            }

            proto_ = new Protocol();
            proto_.len = Integer.reverseBytes(in.readInt());
            if ((proto_.len < Protocol.PROTO_MIN_LEN) || (proto_.len > Protocol.PROTO_MAX_LEN)) {
                throw new InvalidProtoException("ProtoLen Must >= 18B && <= 1M: " + proto_.len);
            }
            lenToRead_ = proto_.len - 4;
        }

        if (in.readableBytes() >= lenToRead_) {
            proto_.seq = Integer.reverseBytes(in.readInt());
            proto_.hash = Integer.reverseBytes(in.readInt());
            proto_.cmd = (Integer.reverseBytes(in.readUnsignedShort()) >>> 16);
            proto_.ret = Short.reverseBytes(in.readShort());
            proto_.hlen = Short.reverseBytes(in.readShort());

            int bodyLen = proto_.len - Protocol.PROTO_MIN_LEN;
            if (bodyLen > 0) {
                proto_.data = new byte[bodyLen];
                in.readBytes(proto_.data);
            }
            out.add(proto_);

            proto_ = null;
            lenToRead_ = 0;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        logger.error(cause.getMessage(), cause);

        proto_ = null;
        lenToRead_ = 0;
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
