package com.joey.util;

import com.joey.common.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * Created by joey on 14-7-5.
 */
public final class Utils {
    public static byte[] packProtocolHeader(Protocol header) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(Protocol.PROTO_MIN_LEN);
        buffer.writeInt(header.len);
        buffer.writeInt(header.seq);
        buffer.writeInt(header.hash);
        buffer.writeShort(header.cmd);
        buffer.writeShort(header.ret);
        buffer.writeShort(header.hlen);

        return buffer.readBytes(Protocol.PROTO_MAX_LEN).array();
    }
}

