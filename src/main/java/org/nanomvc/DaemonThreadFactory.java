package org.nanomvc;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory
        implements ThreadFactory {

    private final ThreadFactory factory;

    public DaemonThreadFactory() {
        this(Executors.defaultThreadFactory());
    }

    public DaemonThreadFactory(ThreadFactory factory) {
        if (factory == null) {
            throw new NullPointerException("factory cannot be null");
        }
        this.factory = factory;
    }

    public Thread newThread(Runnable r) {
        Thread t = this.factory.newThread(r);
        t.setDaemon(true);
        return t;
    }
}