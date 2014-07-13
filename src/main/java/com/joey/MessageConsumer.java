package com.joey;

import com.joey.common.Protocol;
import com.joey.handler.MsgConsumerExceptionHandler;
import com.joey.common.JobQueue;
import com.joey.worker.QueryWorker;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageConsumer extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    private JobQueue<Protocol> jobQueue;
    private MsgConsumerExceptionHandler msgExceptionHandler;

    public MessageConsumer(JobQueue<Protocol> jobQueue) {
        this.jobQueue = jobQueue;
        this.msgExceptionHandler = new MsgConsumerExceptionHandler();
        this.setUncaughtExceptionHandler(this.msgExceptionHandler);
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (jobQueue.isEmpty()) {
                    Thread.currentThread().sleep(10);
                    continue;
                }

                Protocol protocol = jobQueue.poll();
                QueryWorker queryWorker = new QueryWorker(protocol);
                ChannelHandlerContext ctx = (ChannelHandlerContext) protocol.attachment();
                if (ctx != null) {
                    ctx.channel().eventLoop().submit(queryWorker);
                } else {
                    throw new RuntimeException("Channel handler is null");
                }
            }
        } catch (InterruptedException e) {
            // TODO need define exception handler to process exception
            e.printStackTrace();
        }
    }

    public void setJobQueue(JobQueue<Protocol> jobQueue) {
        this.jobQueue = jobQueue;
    }
}
