package com.joey.common;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by joey on 2014-7-2.
 * singleton instance
 */
public class JobQueue<E> extends ConcurrentLinkedQueue<E> {

    private static JobQueue INSTANCE_ = new JobQueue();

    private JobQueue() {
        // do nothing
    }

    public static JobQueue getInstance() {
        if (INSTANCE_ == null) {
            synchronized (JobQueue.class) {
                if (INSTANCE_ == null) { // double check
                    INSTANCE_ = new JobQueue();
                }
            }
        }

        return INSTANCE_;
    }
}
