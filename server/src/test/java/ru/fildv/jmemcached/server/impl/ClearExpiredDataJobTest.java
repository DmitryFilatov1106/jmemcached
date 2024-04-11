package ru.fildv.jmemcached.server.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

public class ClearExpiredDataJobTest {
    private StorageImpl.ClearExpiredDataJob clearExpiredDataJob;
    private Map<String, StorageImpl.StorageItem> map;
    private Set<Map.Entry<String, StorageImpl.StorageItem>> set;
    private Iterator<Map.Entry<String, StorageImpl.StorageItem>> iterator;
    private final int clearDataIntervalInMs = 10000;

    @BeforeEach
    public void before() throws Exception {
        map = mock(Map.class);
        set = mock(Set.class);
        when(map.entrySet()).thenReturn(set);
        iterator = mock(Iterator.class);
        when(set.iterator()).thenReturn(iterator);
        clearExpiredDataJob = spy(new StorageImpl.ClearExpiredDataJob(map, clearDataIntervalInMs) {
            private boolean stop = true;

            @Override
            protected boolean interrupted() {
                stop = !stop;
                return stop;
            }

            @Override
            protected void sleepClearExpiredDataJob() throws InterruptedException {
                //do nothing
            }
        });
    }

    @Test
    public void verifyWhenMapIsEmpty() throws InterruptedException {
        when(iterator.hasNext()).thenReturn(false);

        clearExpiredDataJob.run();

        verifyCommonOperations();
    }

    @Test
    public void verifyWhenMapEntryIsNotExpired() throws InterruptedException {
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        Map.Entry<String, StorageImpl.StorageItem> entry = mock(Map.Entry.class);
        when(iterator.next()).thenReturn(entry);
        StorageImpl.StorageItem item = mock(StorageImpl.StorageItem.class);
        when(entry.getValue()).thenReturn(item);
        when(item.isExpired()).thenReturn(false);

        clearExpiredDataJob.run();

        assertAll(() -> {
            verifyCommonOperations();
            verify(map, never()).remove(anyString());
        });
    }

    @Test
    public void verifyWhenMapEntryIsExpired() throws InterruptedException {
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        Map.Entry<String, StorageImpl.StorageItem> entry = mock(Map.Entry.class);
        when(iterator.next()).thenReturn(entry);
        StorageImpl.StorageItem item = mock(StorageImpl.StorageItem.class);
        when(entry.getKey()).thenReturn("key");
        when(map.remove("key")).thenReturn(item);
        when(entry.getValue()).thenReturn(item);
        when(item.isExpired()).thenReturn(true);

        clearExpiredDataJob.run();

        assertAll(() -> {
            verifyCommonOperations();
            verify(map).remove("key");
        });
    }

    @Test
    public void verifyWhenInterruptedException() throws InterruptedException {
        when(iterator.hasNext()).thenReturn(false);

        clearExpiredDataJob = spy(new StorageImpl.ClearExpiredDataJob(map, clearDataIntervalInMs) {
            @Override
            protected void sleepClearExpiredDataJob() throws InterruptedException {
                throw new InterruptedException("InterruptedException");
            }
        });

        clearExpiredDataJob.run();
        assertAll(() -> {
            verify(clearExpiredDataJob).sleepClearExpiredDataJob();
            verify(clearExpiredDataJob, times(1)).interrupted();
        });
    }

    private void verifyCommonOperations() throws InterruptedException {
        assertAll(() -> {
            verify(clearExpiredDataJob).sleepClearExpiredDataJob();
            verify(clearExpiredDataJob, times(2)).interrupted();
        });
    }
}
