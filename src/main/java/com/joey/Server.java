package com.joey;

import com.joey.common.Protocol;
import com.joey.common.ServerChnlInitializer;
import com.joey.common.JobQueue;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.net.InetSocketAddress;

public class Server {

    private final static Logger logger = Logger.getLogger(Server.class);

    private static final int PORT = Integer.parseInt(System.getProperty("port", "11000"));
    private static final boolean SSL = System.getProperty("ssl") != null;

    private JobQueue<Protocol> jobQueue = null;
    private MessageConsumer consumer = null;
    private ChannelFuture channelFuture = null;
    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workGroup = null;

    private int port;
    private String host;

    public Server() {
        this(null, PORT);
    }

    public Server(String host, int port) {
        this.port = port;
        this.host = host;
        this.jobQueue = JobQueue.getInstance();
        this.consumer = new MessageConsumer(this.jobQueue);
        final InetSocketAddress addr = new InetSocketAddress(port);

        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_RCVBUF, 64 * 1024)
                .childOption(ChannelOption.SO_SNDBUF, 64 * 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ServerChnlInitializer(jobQueue));
        if (host == null) {
            channelFuture = b.bind(addr);
        } else {
            channelFuture = b.bind(host, port);
        }

        try {
            // Get the address we bound to
            InetSocketAddress boundAddress = ((InetSocketAddress) channelFuture.sync().channel().localAddress());
            this.port = boundAddress.getPort();
        } catch (InterruptedException e) {
            this.port = 0;
        }
    }

    /**
     * start to listen client connect
     */
    public void listen() {
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (Throwable e) {
            e.printStackTrace();
            stop();
        }
        this.consumer.start();
    }

    public int getPort() {
        return this.port;
    }

    public void stop() {
        // Close the bound channel
        if (channelFuture != null) {
            channelFuture.channel().close().awaitUninterruptibly();
            channelFuture = null;
        }

        // Shut down event groups
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }

        // TODO shut down all accepted channels as well ??
    }

    public static void main(String[] argv) {
        PropertyConfigurator.configure(System.getProperty("user.dir") + "/log4j.properties");
        Server server = new Server();
        server.listen();
    }
}
