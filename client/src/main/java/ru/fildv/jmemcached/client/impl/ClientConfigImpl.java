package ru.fildv.jmemcached.client.impl;

import lombok.Getter;
import ru.fildv.jmemcached.client.ClientConfig;
import ru.fildv.jmemcached.protocol.ObjectSerializer;
import ru.fildv.jmemcached.protocol.RequestConverter;
import ru.fildv.jmemcached.protocol.ResponseConverter;
import ru.fildv.jmemcached.protocol.impl.ObjectSerializerImpl;
import ru.fildv.jmemcached.protocol.impl.RequestConverterImpl;
import ru.fildv.jmemcached.protocol.impl.ResponseConverterImpl;

@Getter
class ClientConfigImpl implements ClientConfig {
    private final String host;
    private final int port;
    private final RequestConverter requestConverter;
    private final ResponseConverter responseConverter;
    private final ObjectSerializer objectSerializer;

    ClientConfigImpl(final String host, final int port) {
        this.host = host;
        this.port = port;
        this.requestConverter = new RequestConverterImpl();
        this.responseConverter = new ResponseConverterImpl();
        this.objectSerializer = new ObjectSerializerImpl();
    }
}
