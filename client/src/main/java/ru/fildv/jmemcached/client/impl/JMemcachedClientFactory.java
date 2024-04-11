package ru.fildv.jmemcached.client.impl;

import lombok.experimental.UtilityClass;
import ru.fildv.jmemcached.client.Client;

import java.io.IOException;

@UtilityClass
public class JMemcachedClientFactory {
    public Client buildNewClient(final String host, final int port)
            throws IOException {
        return new ClientImpl(new ClientConfigImpl(host, port));
    }

    public Client buildNewClient(final String host) throws IOException {
        return buildNewClient(host, 9010);
    }

    public Client buildNewClient() throws IOException {
        return buildNewClient("localhost");
    }
}
