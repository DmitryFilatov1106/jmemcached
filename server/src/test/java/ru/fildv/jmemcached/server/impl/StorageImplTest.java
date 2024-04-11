package ru.fildv.jmemcached.server.impl;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.fildv.jmemcached.protocol.model.Status;
import ru.fildv.jmemcached.server.ServerConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StorageImplTest {
    private ExecutorService executorServiceMock;
    private ServerConfig serverConfig;
    private StorageImpl storage;

    @BeforeEach
    public void before() {
        executorServiceMock = mock(ExecutorService.class);
        serverConfig = mock(ServerConfig.class);
        when(serverConfig.getClearDataIntervalInMs()).thenReturn(10);
        storage = new StorageImpl(serverConfig) {
            @Override
            protected ExecutorService createClearExpiredDataExecutorService() {
                return executorServiceMock;
            }
        };
        // Default state for storage
        storage.put("test", TimeUnit.SECONDS.toMillis(1), new byte[]{5, 6, 7});
    }

    @Test
    public void startClearExpiredDataExecutorService() throws IllegalAccessException {
        Runnable clearExpiredDataJob = (Runnable) FieldUtils.getDeclaredField(
                StorageImpl.class, "clearExpiredDataJob", true).get(storage);
        verify(executorServiceMock).submit(clearExpiredDataJob);
    }

    @Test
    public void close() throws Exception {
        storage.close();
        verify(executorServiceMock, never()).shutdownNow();
        verify(executorServiceMock, never()).shutdown();
    }

    @Test
    public void createClearExpiredDataThreadFactory() {
        ThreadFactory threadFactory = storage.createClearExpiredDataThreadFactory();
        Thread thread = threadFactory.newThread(mock(Runnable.class));

        assertAll(() -> {
            assertTrue(thread.isDaemon());
            assertEquals(Thread.MIN_PRIORITY, thread.getPriority());
            assertEquals("ClearExpiredDataJobThread", thread.getName());
        });
    }

    @Test
    public void putAdded() {
        Status status = storage.put("key", null, new byte[]{1, 2, 3});
        assertEquals(Status.ADDED, status);
    }

    @Test
    public void putReplaced() {
        Status status = storage.put("test", null, new byte[]{1, 2, 3});
        assertEquals(Status.REPLACED, status);
    }

    @Test
    public void getSuccess() {
        byte[] data = storage.get("test");
        assertArrayEquals(new byte[]{5, 6, 7}, data);
    }

    @Test
    public void getNotFound() {
        byte[] data = storage.get("not_found");
        assertNull(data);
    }

    @Test
    public void getExpired() throws InterruptedException {
        // Default test value ttl is 1 SECOND
        TimeUnit.MILLISECONDS.sleep(1100);
        byte[] data = storage.get("test");
        assertNull(data);
    }

    @Test
    public void removeSuccess() {
        Status status = storage.remove("test");
        assertEquals(Status.REMOVED, status);
    }

    @Test
    public void removeNotFound() {
        Status status = storage.remove("not_found");
        assertEquals(Status.NOT_FOUND, status);
    }

    @Test
    public void removeExpired() throws InterruptedException {
        // Default test value ttl is 1 SECOND
        TimeUnit.MILLISECONDS.sleep(1100);
        Status status = storage.remove("test");
        assertEquals(Status.NOT_FOUND, status);
    }

    @Test
    public void clear() {
        Status status = storage.clear();
        assertEquals(Status.CLEARED, status);
        assertNull(storage.get("test"));
    }
}
