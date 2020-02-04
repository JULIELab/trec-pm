package de.julielab.ir;

import at.medunigraz.imi.bst.config.TrecConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Multithreading {
    private static final Logger log = LoggerFactory.getLogger(Multithreading.class);
    private static Multithreading instance;
    private final ExecutorService executorService;

    private Multithreading() {
        log.info("Creating executor service with a thread pool of size {}", TrecConfig.CONCURRENCY_MAX);
        executorService = Executors.newFixedThreadPool(TrecConfig.CONCURRENCY_MAX);
    }

    public synchronized static Multithreading getInstance() {
        if (instance == null)
            instance = new Multithreading();
        return instance;
    }


    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        return executorService.submit(callable);
    }

    public synchronized void shutdown() {
        log.info("Shutting down Multithreading executor service.");
        executorService.shutdown();
        log.info("Shutdown successful: {}", executorService.isShutdown());
        log.info("Executor terminated: {}", executorService.isTerminated());
        if (!executorService.isTerminated())
            executorService.shutdownNow();
    }
}
