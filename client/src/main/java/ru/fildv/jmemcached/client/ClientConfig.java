package ru.fildv.jmemcached.client;

import ru.fildv.jmemcached.protocol.ObjectSerializer;
import ru.fildv.jmemcached.protocol.RequestConverter;
import ru.fildv.jmemcached.protocol.ResponseConverter;

public interface ClientConfig {
    String getHost();

    int getPort();

    RequestConverter getRequestConverter();

    ResponseConverter getResponseConverter();

    ObjectSerializer getObjectSerializer();
}
