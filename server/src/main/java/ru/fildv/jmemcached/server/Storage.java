package ru.fildv.jmemcached.server;

import ru.fildv.jmemcached.protocol.model.Status;

public interface Storage extends AutoCloseable {
    Status put(String key, Long ttl, byte[] data);

    byte[] get(String key);

    Status remove(String key);

    Status clear();
}
