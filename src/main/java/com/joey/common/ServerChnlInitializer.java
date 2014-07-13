package com.joey.common;

import com.joey.codec.MessageDecoder;
import com.joey.codec.MessageEncoder;
import com.joey.handler.MessageHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;


/**
 * Created by joey on 14-7-4.
 */
public class ServerChnlInitializer extends ChannelInitializer<SocketChannel> {

    private static final boolean SSL = System.getProperty("ssl") != null;
    private JobQueue jobQueue;

    public ServerChnlInitializer(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        final SslContext sslCtx;
        ChannelPipeline p = ch.pipeline();
        // TODO Why check SSL ??
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
        } else {
            sslCtx = null;
        }

        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }

        /** process received data by the add-order */
        p.addLast("decoder", new MessageDecoder())
         .addLast("encoder", new MessageEncoder())
         .addLast("handler", new MessageHandler(this.jobQueue));
    }
}
