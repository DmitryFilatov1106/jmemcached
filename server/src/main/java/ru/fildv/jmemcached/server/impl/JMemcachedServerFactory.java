package ru.fildv.jmemcached.server.impl;

import lombok.experimental.UtilityClass;
import ru.fildv.jmemcached.server.Server;

import java.util.Properties;

@UtilityClass
public class JMemcachedServerFactory {
    public Server buildNewServer(final Properties properties) {
        return new ServerImpl(new ServerConfigImpl(properties));
    }
}
