package ru.fildv.jmemcached.server.impl;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class StorageItemTest {

    private StorageImpl.StorageItem storageItem;

    @Test
    public void isNotExpiredTtlNull() {
        storageItem = new StorageImpl.StorageItem("key", null, new byte[]{1, 2, 3});
        assertFalse(storageItem.isExpired());
    }

    @Test
    public void isNotExpiredTtlNotNull() {
        storageItem = new StorageImpl.StorageItem("key", TimeUnit.SECONDS.toMillis(5), new byte[]{1, 2, 3});
        assertFalse(storageItem.isExpired());
    }

    @Test
    public void isExpiredTtlNotNull() throws InterruptedException {
        storageItem = new StorageImpl.StorageItem("key", TimeUnit.MILLISECONDS.toMillis(5), new byte[]{1, 2, 3});
        TimeUnit.MILLISECONDS.sleep(10);
        assertTrue(storageItem.isExpired());
    }

    @Test
    public void toStringWithData() {
        storageItem = new StorageImpl.StorageItem("key", null, new byte[]{1, 2, 3});
        assertEquals("[key]=3 bytes", storageItem.toString());
    }

    @Test
    public void toStringWithoutData() {
        storageItem = new StorageImpl.StorageItem("key", null, null);
        assertEquals("[key]=null", storageItem.toString());
    }

    @Test
    public void toStringWithTTL() {
        storageItem = new StorageImpl.StorageItem("key", -System.currentTimeMillis(), null);
        assertEquals("[key]=null (Thu Jan 01 03:00:00 GMT+03:00 1970)", storageItem.toString());
    }
}
