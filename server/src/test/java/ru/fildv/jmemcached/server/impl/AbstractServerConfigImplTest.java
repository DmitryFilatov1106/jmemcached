package ru.fildv.jmemcached.server.impl;

import ru.fildv.jmemcached.server.Storage;

import java.util.Properties;

import static org.mockito.Mockito.mock;

public abstract class AbstractServerConfigImplTest {
    protected Storage storage;

    protected ServerConfigImpl createServerConfigMock(Properties overrideApplicationProperties) {
        storage = mock(Storage.class);
        return new ServerConfigImpl(overrideApplicationProperties) {
            @Override
            protected Storage createStorage() {
                return storage;
            }
        };
    }
}
