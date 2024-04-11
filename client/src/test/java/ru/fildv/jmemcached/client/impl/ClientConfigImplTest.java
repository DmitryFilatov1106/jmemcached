package ru.fildv.jmemcached.client.impl;

import org.junit.jupiter.api.Test;
import ru.fildv.jmemcached.protocol.impl.ObjectSerializerImpl;
import ru.fildv.jmemcached.protocol.impl.RequestConverterImpl;
import ru.fildv.jmemcached.protocol.impl.ResponseConverterImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientConfigImplTest {
    private final ClientConfigImpl clientConfig = new ClientConfigImpl("localhost", 9010);

    @Test
    void getHost() {
        assertEquals("localhost", clientConfig.getHost());
    }

    @Test
    void getPort() {
        assertEquals(9010, clientConfig.getPort());
    }

    @Test
    void getRequestConverter() {
        assertEquals(RequestConverterImpl.class, clientConfig.getRequestConverter().getClass());
    }

    @Test
    void getResponseConverter() {
        assertEquals(ResponseConverterImpl.class, clientConfig.getResponseConverter().getClass());
    }

    @Test
    void getObjectSerializer() {
        assertEquals(ObjectSerializerImpl.class, clientConfig.getObjectSerializer().getClass());
    }
}