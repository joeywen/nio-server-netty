package com.joey.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.ByteOrder;

/**
 * Created by joey on 2014-7-2.
 */
public class Protocol {

    public static final int PROTO_MIN_LEN = 18;
    public static final int PROTO_MAX_LEN = 60 * 1024;

    public int		len;	// 协议总长度
    public int		seq;	// 序列号，用于一一对应
    public int		hash;	// 用于分库分表的数字
    public int	    cmd;	// 命令号：1. 用于选择正确的库； 2. 用于定位到正确的处理函数。
    public short	ret;	// 错误码
    public short	hlen;	// protobuf格式的标准包头

    public byte[]   data = null;        // 协议请求主体数据
    public Object attachment;

    public Object attachment() {
        return attachment;
    }

    public void attach(Object attachment) {
        this.attachment = attachment;
    }

    public void send(ChannelHandlerContext ctx, byte[] body)
    {
        len = PROTO_MIN_LEN + body.length;
        ret = 0;

        final ByteBuf buf = ctx.alloc().buffer(len).order(ByteOrder.LITTLE_ENDIAN);
        buf.writeInt(len);
        buf.writeInt(seq);
        buf.writeInt(hash);
        buf.writeShort((short)cmd);
        buf.writeShort(ret);
        buf.writeShort(hlen);
        buf.writeBytes(body);
        ctx.writeAndFlush(buf);
    }

    public void send(ChannelHandlerContext ctx, int errCode)
    {
        len = PROTO_MIN_LEN;
        ret = (short)errCode;

        final ByteBuf buf = ctx.alloc().buffer(len).order(ByteOrder.LITTLE_ENDIAN);
        buf.writeInt(len);
        buf.writeInt(seq);
        buf.writeInt(hash);
        buf.writeShort((short)cmd);
        buf.writeShort(ret);
        buf.writeShort(hlen);
        ctx.writeAndFlush(buf);
    }

    @Override
    public String toString()
    {
        return "[Protocol][len = " + len + ", seq = " + seq + ", hash = " + hash + ", cmd = " + cmd + ", ret = " + ret + ", hlen = " + hlen
                + ", date = " + new String(data) + "]";
    }
}
