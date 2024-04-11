package ru.fildv.jmemcached.server.impl;

import lombok.extern.slf4j.Slf4j;
import ru.fildv.jmemcached.exception.JMemcachedException;
import ru.fildv.jmemcached.server.Server;
import ru.fildv.jmemcached.server.ServerConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
class ServerImpl implements Server {
    private final ServerConfig serverConfig;
    private final ServerSocket serverSocket;
    private final ExecutorService executorService;
    private final Thread mainServerThread;
    private volatile boolean serverStopped;

    ServerImpl(final ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.serverSocket = createServerSocket();
        this.executorService = createExecutorService();
        this.mainServerThread = createMainServerThread(createServerRunnable());
    }

    @Override
    public void start() {
        if (mainServerThread.getState() != Thread.State.NEW) {
            throw new JMemcachedException(
                    "Current server already started or stopped!");
        }
        Runtime.getRuntime().addShutdownHook(getShutdownHook());
        mainServerThread.start();
        log.info("Server started:" + serverConfig);
    }

    @Override
    public void stop() {
        log.info("Detected stop cmd");
        mainServerThread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            log.warn("Error during close server socket: {}", e.getMessage());
        }
    }

    protected Thread createMainServerThread(final Runnable runnable) {
        Thread thread = new Thread(runnable, "Main server thread");
        thread.setPriority(Thread.MAX_PRIORITY);
        return thread;
    }

    protected Runnable createServerRunnable() {
        return () -> {
            while (!mainServerThread.isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    try {
                        executorService.execute(serverConfig
                                .buildNewClientSocketHandler(clientSocket));
                        log.info("Client has connected: {}",
                                clientSocket.getRemoteSocketAddress()
                                        .toString());
                    } catch (RejectedExecutionException e) {
                        log.error("All threads are busy: {}", e.getMessage());
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        log.error("Can't accept new client socket: {}",
                                e.getMessage());
                    }
                    destroyJMemcachedServer();
                    break;
                }
            }
        };
    }

    protected Thread getShutdownHook() {
        return new Thread(() -> {
            if (!serverStopped) {
                destroyJMemcachedServer();
            }
        }, "ShutdownHook");
    }

    protected void destroyJMemcachedServer() {
        try {
            serverConfig.close();
        } catch (Exception e) {
            log.error("Close serverConfig failed: {}", e.getMessage());
        }
        executorService.shutdownNow();
        log.info("Server stopped");
        serverStopped = true;
    }

    protected ExecutorService createExecutorService() {
        ThreadFactory threadFactory = serverConfig.getWorkerThreadFactory();
        int initThreadCount = serverConfig.getInitThreadCount();
        int maxThreadCount = serverConfig.getMaxThreadCount();
        return new ThreadPoolExecutor(initThreadCount, maxThreadCount,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(), threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }

    protected ServerSocket createServerSocket() {
        try {
            ServerSocket serverSocket = new ServerSocket(
                    serverConfig.getServerPort());
            serverSocket.setReuseAddress(true);
            return serverSocket;
        } catch (IOException e) {
            throw new JMemcachedException(
                    "Can't create server socket with port: "
                    + serverConfig.getServerPort(), e);
        }
    }
}
