package ru.fildv.jmemcached.server.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.fildv.jmemcached.exception.JMemcachedConfigException;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerConfigImplGetClearDataIntervalInMsTest extends AbstractServerConfigImplTest {
    private ServerConfigImpl serverConfig;

    @ParameterizedTest
    @CsvSource({
            "999, storage.clear.interval.ms should be more than 999 ms",
            "qw, storage.clear.interval.ms should be a number",
    })
    public void getClearDataIntervalInMs(String value, String message) throws Exception {
        Properties p = new Properties();
        p.setProperty("storage.clear.interval.ms", value);

        JMemcachedConfigException e = Assertions.assertThrows(JMemcachedConfigException.class,
                () -> {
                    serverConfig = createServerConfigMock(p);
                    serverConfig.getClearDataIntervalInMs();
                });
        assertEquals(message, e.getMessage());
    }
}
