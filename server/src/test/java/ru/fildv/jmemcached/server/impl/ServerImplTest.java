package ru.fildv.jmemcached.server.impl;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import ru.fildv.jmemcached.exception.JMemcachedException;
import ru.fildv.jmemcached.server.ClientSocketHandler;
import ru.fildv.jmemcached.server.ServerConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServerImplTest {
    private ServerImpl server;
    private Logger logger;
    private ServerConfig serverConfig;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private Thread mainServerThread;

    @BeforeEach
    public void before() throws Exception {
        logger = mock(Logger.class);
        serverConfig = mock(ServerConfig.class);
        when(serverConfig.toString()).thenReturn("serverConfig");
        serverSocket = mock(ServerSocket.class);
        executorService = mock(ExecutorService.class);
        mainServerThread = mock(Thread.class);
    }

    @Test
    public void createMainServerThread() {
        server = new ServerImpl(serverConfig) {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }
        };
        Thread thread = server.createMainServerThread(mock(Runnable.class));

        assertAll(() -> {
            assertEquals(Thread.MAX_PRIORITY, thread.getPriority());
            assertEquals("Main Server Thread", thread.getName());
            assertFalse(thread.isDaemon());
            assertFalse(thread.isAlive());
        });
    }

    @Test
    public void startSuccess() {
        when(mainServerThread.getState()).thenReturn(Thread.State.NEW);
        server = new ServerImpl(serverConfig) {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }

            @Override
            protected Thread createMainServerThread(Runnable r) {
                return mainServerThread;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }

            @Override
            protected Thread getShutdownHook() {
                return new Thread();
            }
        };
        server.start();
        assertAll(() -> {
            verify(mainServerThread).getState();
            verify(mainServerThread).start();
        });
    }

    @Test
    public void startFailed() {
        when(mainServerThread.getState()).thenReturn(Thread.State.TERMINATED);
        server = new ServerImpl(serverConfig) {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }

            @Override
            protected Thread createMainServerThread(Runnable r) {
                return mainServerThread;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }
        };

        JMemcachedException e = Assertions.assertThrows(JMemcachedException.class,
                () -> server.start()
        );
        assertEquals("Current JMemcached server already started or stopped! Please create a new server instance", e.getMessage());
    }

    @Test
    public void stopSuccess() throws IOException {
        server = new ServerImpl(serverConfig) {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }

            @Override
            protected Thread createMainServerThread(Runnable r) {
                return mainServerThread;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }
        };
        server.stop();
        assertAll(() -> {
            verify(mainServerThread).interrupt();
            verify(serverSocket).close();
        });
    }

    @Test
    public void stopWithIOException() throws IOException {
        IOException ex = new IOException("Close");
        doThrow(ex).when(serverSocket).close();
        server = new ServerImpl(serverConfig) {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }

            @Override
            protected Thread createMainServerThread(Runnable r) {
                return mainServerThread;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }
        };
        server.stop();
        assertAll(() -> {
            verify(mainServerThread).interrupt();
            verify(serverSocket).close();
        });
    }

    @Test
    public void destroyServerWithException() throws Exception {
        doThrow(new Exception("Close")).when(serverConfig).close();
        server = new ServerImpl(serverConfig) {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }

            @Override
            protected Thread createMainServerThread(Runnable r) {
                return mainServerThread;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }
        };
        server.destroyJMemcachedServer();
        assertAll(() -> {
            verify(serverConfig).close();
            verify(executorService).shutdownNow();
        });
    }

    @Test
    public void shutdownHookWithSuccessDestroyServer() throws Exception {
        server = new ServerImpl(serverConfig) {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }

            @Override
            protected Thread createMainServerThread(Runnable r) {
                return mainServerThread;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }
        };
        server.getShutdownHook().run();
        assertAll(() -> {
            verify(serverConfig).close();
            verify(executorService).shutdownNow();
        });
    }

    @Test
    public void shutdownHookWithoutDestroyServer() throws Exception {
        server = new ServerImpl(serverConfig) {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }

            @Override
            protected Thread createMainServerThread(Runnable r) {
                return mainServerThread;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }
        };
        //set private field serverStopped via reflection
        FieldUtils.getDeclaredField(ServerImpl.class, "serverStopped", true).set(server, Boolean.TRUE);
        server.getShutdownHook().run();
        assertAll(() -> {
            verify(serverConfig, never()).close();
            verify(executorService, never()).shutdownNow();
        });
    }

    @Test
    public void createExecutorService() throws IllegalAccessException {
        when(serverConfig.getInitThreadCount()).thenReturn(1);
        when(serverConfig.getMaxThreadCount()).thenReturn(10);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        when(serverConfig.getWorkerThreadFactory()).thenReturn(threadFactory);
        server = new ServerImpl(serverConfig) {
            @Override
            protected Thread createMainServerThread(Runnable r) {
                return mainServerThread;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }
        };

        //get private field executorService via reflection
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)
                FieldUtils.getDeclaredField(ServerImpl.class, "executorService", true).get(server);

        assertAll(() -> {
            verify(serverConfig).getInitThreadCount();
            verify(serverConfig).getMaxThreadCount();
            verify(serverConfig).getWorkerThreadFactory();

            assertEquals(1, threadPoolExecutor.getCorePoolSize());
            assertEquals(10, threadPoolExecutor.getMaximumPoolSize());
            assertEquals(60, threadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS));
            assertSame(threadFactory, threadPoolExecutor.getThreadFactory());
            assertEquals(SynchronousQueue.class, threadPoolExecutor.getQueue().getClass());
            assertEquals(ThreadPoolExecutor.AbortPolicy.class, threadPoolExecutor.getRejectedExecutionHandler().getClass());
        });
    }

    @Test
    public void createServerRunnableSuccessRun() throws IOException {
        server = spy(new ServerImpl(serverConfig) {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }

            @Override
            protected Thread createMainServerThread(Runnable r) {
                return mainServerThread;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }
        });
        when(mainServerThread.isInterrupted()).thenReturn(false).thenReturn(true);
        Socket clientSocket = mock(Socket.class);
        SocketAddress socketAddress = mock(SocketAddress.class);
        when(clientSocket.getRemoteSocketAddress()).thenReturn(socketAddress);
        when(socketAddress.toString()).thenReturn("localhost");
        when(serverSocket.accept()).thenReturn(clientSocket);
        ClientSocketHandler clientSocketHandler = mock(ClientSocketHandler.class);
        when(serverConfig.buildNewClientSocketHandler(clientSocket)).thenReturn(clientSocketHandler);

        server.createServerRunnable().run();
        assertAll(() -> {
            verify(mainServerThread, times(2)).isInterrupted();
            verify(serverSocket).accept();
            verify(serverConfig).buildNewClientSocketHandler(clientSocket);

            verify(clientSocket, never()).close();
            verify(server, never()).destroyJMemcachedServer();
        });
    }

    @Test
    public void createServerRunnableRunWithRejectedExecutionException() throws IOException {
        server = spy(new ServerImpl(serverConfig) {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }

            @Override
            protected Thread createMainServerThread(Runnable r) {
                return mainServerThread;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }
        });
        when(mainServerThread.isInterrupted()).thenReturn(false).thenReturn(true);
        Socket clientSocket = mock(Socket.class);
        when(serverSocket.accept()).thenReturn(clientSocket);
        ClientSocketHandler clientSocketHandler = mock(ClientSocketHandler.class);
        when(serverConfig.buildNewClientSocketHandler(clientSocket)).thenReturn(clientSocketHandler);
        when(executorService.submit(clientSocketHandler)).thenThrow(new RejectedExecutionException("RejectedExecutionException"));

        server.createServerRunnable().run();
        assertAll(() -> {
            verify(mainServerThread, times(2)).isInterrupted();
            verify(serverSocket).accept();
            verify(serverConfig).buildNewClientSocketHandler(clientSocket);
            verify(clientSocket).close();

            verify(server, never()).destroyJMemcachedServer();
        });
    }

    @Test
    public void createServerRunnableRunWithIOExceptionAndServerSocketIsClosed() throws IOException {
        server = spy(new ServerImpl(serverConfig) {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }

            @Override
            protected Thread createMainServerThread(Runnable r) {
                return mainServerThread;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }
        });
        when(mainServerThread.isInterrupted()).thenReturn(false);
        when(serverSocket.accept()).thenThrow(new IOException("IOException"));
        when(serverSocket.isClosed()).thenReturn(true);

        server.createServerRunnable().run();

        assertAll(() -> {
            verify(mainServerThread, times(1)).isInterrupted();
            verify(serverSocket).accept();
            verify(server).destroyJMemcachedServer();
            verify(serverConfig, never()).buildNewClientSocketHandler(any(Socket.class));
        });
    }

    @Test
    public void createServerRunnableRunWithIOExceptionAndServerSocketIsNotClosed() throws IOException {
        server = spy(new ServerImpl(serverConfig) {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }

            @Override
            protected Thread createMainServerThread(Runnable r) {
                return mainServerThread;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }
        });
        when(mainServerThread.isInterrupted()).thenReturn(false);
        IOException ex = new IOException("IOException");
        when(serverSocket.accept()).thenThrow(ex);
        when(serverSocket.isClosed()).thenReturn(false);

        server.createServerRunnable().run();

        assertAll(() -> {
            verify(mainServerThread, times(1)).isInterrupted();
            verify(serverSocket).accept();
            verify(server).destroyJMemcachedServer();
            verify(serverConfig, never()).buildNewClientSocketHandler(any(Socket.class));
        });
    }
}