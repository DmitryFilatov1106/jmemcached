package ru.fildv.jmemcached.server.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.fildv.jmemcached.exception.JMemcachedConfigException;

import java.util.Properties;

public class ServerConfigImplGetServerPortTest extends AbstractServerConfigImplTest {
    private ServerConfigImpl serverConfig;

    @ParameterizedTest
    @CsvSource({
            "-1, server.port should be between 0-65535",
            "65536, server.port should be between 0-65535",
            "qw, server.port should be a number"
    })
    public void getServerPort(String value, String message) throws Exception {
        Properties p = new Properties();
        p.setProperty("server.port", value);

        JMemcachedConfigException e = Assertions.assertThrows(JMemcachedConfigException.class,
                () -> {
                    serverConfig = createServerConfigMock(p);
                    serverConfig.getServerPort();
                });
        Assertions.assertEquals(message, e.getMessage());
    }
}
