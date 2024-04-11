package ru.fildv.jmemcached.server.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.fildv.jmemcached.protocol.model.Status;
import ru.fildv.jmemcached.server.ServerConfig;
import ru.fildv.jmemcached.server.Storage;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Slf4j
class StorageImpl implements Storage {
    protected final Map<String, StorageItem> map;
    protected final ExecutorService executorService;
    protected final Runnable clearExpiredDataJob;

    StorageImpl(final ServerConfig serverConfig) {
        int clearDataIntervalInMs = serverConfig.getClearDataIntervalInMs();
        this.map = createMap();
        this.executorService = createClearExpiredDataExecutorService();
        this.clearExpiredDataJob = new ClearExpiredDataJob(map,
                clearDataIntervalInMs);
        this.executorService.submit(clearExpiredDataJob);
    }

    protected Map<String, StorageItem> createMap() {
        return new ConcurrentHashMap<>();
    }

    protected ExecutorService createClearExpiredDataExecutorService() {
        return Executors.newSingleThreadExecutor(
                createClearExpiredDataThreadFactory());
    }

    protected ThreadFactory createClearExpiredDataThreadFactory() {
        return r -> {
            Thread clearExpiredDataJobThread =
                    new Thread(r, "ClearExpiredDataJobThread");
            clearExpiredDataJobThread.setPriority(Thread.MIN_PRIORITY);
            clearExpiredDataJobThread.setDaemon(true);
            return clearExpiredDataJobThread;
        };
    }

    @Override
    public Status put(final String key, final Long ttl, final byte[] data) {
        StorageItem item = map.put(key, new StorageItem(key, ttl, data));
        return item == null ? Status.ADDED : Status.REPLACED;
    }

    @Override
    public byte[] get(final String key) {
        StorageItem item = map.get(key);
        if (item == null || item.isExpired()) {
            return null;
        }
        return item.data;
    }

    @Override
    public Status remove(final String key) {
        StorageItem item = map.remove(key);
        return item != null && !item.isExpired()
                ? Status.REMOVED : Status.NOT_FOUND;
    }

    @Override
    public Status clear() {
        map.clear();
        return Status.CLEARED;
    }

    @Override
    public void close() throws Exception {
        // It's a demon!
    }

    @RequiredArgsConstructor
    protected static class ClearExpiredDataJob implements Runnable {
        private final Map<String, StorageItem> map;
        private final int clearDataIntervalInMs;

        protected boolean interrupted() {
            return Thread.interrupted();
        }

        protected void sleepClearExpiredDataJob() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(clearDataIntervalInMs);
        }

        @Override
        public void run() {
            log.debug("ClearExpiredDataJobThread started with interval {} ms",
                    clearDataIntervalInMs);
            while (!interrupted()) {
                log.trace("Invoke clear job");
                for (Map.Entry<String, StorageItem> entry : map.entrySet()) {
                    if (entry.getValue().isExpired()) {
                        StorageItem item = map.remove(entry.getKey());
                        log.debug("Removed expired storageItem = {}", item);
                    }
                }
                try {
                    sleepClearExpiredDataJob();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    protected static class StorageItem {
        private final String key;
        private final Long ttl;
        private final byte[] data;

        protected StorageItem(final String key,
                              final Long ttl, final byte[] data) {
            this.key = key;
            this.ttl = ttl != null ? ttl + System.currentTimeMillis() : null;
            this.data = data;
        }

        protected boolean isExpired() {
            return ttl != null && ttl < System.currentTimeMillis();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("[")
                    .append(key).append("]=");
            if (data == null) {
                sb.append("null");
            } else {
                sb.append(data.length).append(" bytes");
            }
            if (ttl != null) {
                sb.append(" (").append(new Date(ttl)).append(')');
            }
            return sb.toString();
        }
    }
}
