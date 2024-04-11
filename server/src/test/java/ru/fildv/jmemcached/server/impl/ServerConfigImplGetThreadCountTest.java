package ru.fildv.jmemcached.server.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.fildv.jmemcached.exception.JMemcachedConfigException;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServerConfigImplGetThreadCountTest extends AbstractServerConfigImplTest {
    private ServerConfigImpl serverConfig;

    private void setUpDefaultServerConfig(String property, String value, String message) {
        Properties p = new Properties();
        p.setProperty(property, value);
        serverConfig = createServerConfigMock(p);
    }

    @ParameterizedTest
    @CsvSource({
            "0, should be more than 0",
            "qw, should be a number"
    })
    public void getInitThreadCount(String value, String message) throws Exception {
        String property = "server.thread.count.init";
        setUpDefaultServerConfig(property, value, message);
        JMemcachedConfigException e = assertThrows(JMemcachedConfigException.class,
                () -> serverConfig.getInitThreadCount()
        );
        assertEquals(property + " " + message, e.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "0, should be more than 0",
            "qw, should be a number"
    })
    public void getMaxThreadCount(String value, String message) throws Exception {
        String property = "server.thread.count.max";
        setUpDefaultServerConfig(property, value, message);
        JMemcachedConfigException e = assertThrows(JMemcachedConfigException.class,
                () -> serverConfig.getMaxThreadCount()
        );
        assertEquals(property + " " + message, e.getMessage());
    }
}
