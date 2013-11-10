package org.nanomvc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ExecutorContextListener
        implements ServletContextListener {

    private ExecutorService executor;

    public void contextInitialized(ServletContextEvent arg0) {
        ServletContext context = arg0.getServletContext();
        int nr_executors = 1;
        ThreadFactory daemonFactory = new DaemonThreadFactory();
        try {
            nr_executors = Integer.parseInt(context.getInitParameter("nr-executors"));
        } catch (NumberFormatException ignore) {
        }
        if (nr_executors <= 1) {
            this.executor = Executors.newSingleThreadExecutor(daemonFactory);
        } else {
            this.executor = Executors.newFixedThreadPool(nr_executors, daemonFactory);
        }
        context.setAttribute("NANOMVC_EXECUTOR", this.executor);
    }

    public void contextDestroyed(ServletContextEvent arg0) {
        ServletContext context = arg0.getServletContext();
        this.executor.shutdownNow();
    }
}