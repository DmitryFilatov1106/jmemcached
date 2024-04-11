package ru.fildv.jmemcached.server.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.fildv.jmemcached.exception.JMemcachedConfigException;
import ru.fildv.jmemcached.protocol.impl.RequestConverterImpl;
import ru.fildv.jmemcached.protocol.impl.ResponseConverterImpl;
import ru.fildv.jmemcached.server.ClientSocketHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ThreadFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ServerConfigImplTest extends AbstractServerConfigImplTest {
    private ServerConfigImpl serverConfig;

    @BeforeEach
    public void before() {
        serverConfig =createServerConfigMock(null);
    }

    @Test
    public void testDefaultInitState() throws Exception {
        try (ServerConfigImpl defaultServerConfig = new ServerConfigImpl(null)) {
            assertAll(() -> {
                assertEquals(RequestConverterImpl.class, defaultServerConfig.getRequestConverter().getClass());
                assertEquals(ResponseConverterImpl.class, defaultServerConfig.getResponseConverter().getClass());
                assertEquals(StorageImpl.class, defaultServerConfig.getStorage().getClass());
                assertEquals(CommandHandlerImpl.class, defaultServerConfig.getCommandHandler().getClass());

                assertEquals(9010, defaultServerConfig.getServerPort());
                assertEquals(1, defaultServerConfig.getInitThreadCount());
                assertEquals(10, defaultServerConfig.getMaxThreadCount());
                assertEquals(10000, defaultServerConfig.getClearDataIntervalInMs());
            });
        }
    }

    @Test
    public void getWorkerThreadFactory() {
        ThreadFactory threadFactory = serverConfig.getWorkerThreadFactory();
        Thread thread = threadFactory.newThread(mock(Runnable.class));
        assertAll(() -> {
            assertTrue(thread.isDaemon());
            assertEquals("Thread-0", thread.getName());
        });
    }

    @Test
    public void close() throws Exception {
        serverConfig.close();
        verify(storage).close();
    }

    @Test
    public void buildNewClientSocketHandler() {
        ClientSocketHandler clientSocketHandler = serverConfig.buildNewClientSocketHandler(mock(Socket.class));
        assertEquals(ClientSocketHandlerImpl.class, clientSocketHandler.getClass());
    }

    @Test
    public void verifyToString() {
        assertEquals("ServerConfigImpl: port=9010, initThreadCount=1, maxThreadCount=10, clearDataIntervalInMs=10000ms", serverConfig.toString());
    }

    @Test
    public void loadApplicationPropertiesNotFound() {
        JMemcachedConfigException e = Assertions.assertThrows(JMemcachedConfigException.class,
                ()->serverConfig.loadApplicationProperties("not_found.properties"));
        assertEquals("Not found resource: not_found.properties", e.getMessage());
    }

    @Test
    public void loadApplicationPropertiesIOException() throws IOException {
        final IOException ex = new IOException("IO");

        JMemcachedConfigException e = Assertions.assertThrows(JMemcachedConfigException.class,
                ()->{
                    serverConfig = new ServerConfigImpl(null) {
                        @Override
                        protected InputStream getClassPathResourceInputStream(String classPathResource) {
                            return new InputStream() {
                                @Override
                                public int read() throws IOException {
                                    throw ex;
                                }
                            };
                        }
                    };
                });
        assertEquals("Can't load: server.properties", e.getMessage());
        assertEquals(ex, e.getCause());
    }
}