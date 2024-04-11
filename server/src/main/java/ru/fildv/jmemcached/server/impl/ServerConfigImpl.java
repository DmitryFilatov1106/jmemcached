package ru.fildv.jmemcached.server.impl;

import lombok.Getter;
import ru.fildv.jmemcached.exception.JMemcachedConfigException;
import ru.fildv.jmemcached.protocol.RequestConverter;
import ru.fildv.jmemcached.protocol.ResponseConverter;
import ru.fildv.jmemcached.protocol.impl.RequestConverterImpl;
import ru.fildv.jmemcached.protocol.impl.ResponseConverterImpl;
import ru.fildv.jmemcached.server.ClientSocketHandler;
import ru.fildv.jmemcached.server.CommandHandler;
import ru.fildv.jmemcached.server.ServerConfig;
import ru.fildv.jmemcached.server.Storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ThreadFactory;

@Getter
class ServerConfigImpl implements ServerConfig {
    private static final String FILE_PROPERTIES = "server.properties";
    private final Properties applicationProperties = new Properties();
    private final RequestConverter requestConverter;
    private final ResponseConverter responseConverter;
    private final Storage storage;
    private final CommandHandler commandHandler;

    ServerConfigImpl(final Properties properties) {
        loadApplicationProperties(FILE_PROPERTIES);
        if (properties != null) {
            applicationProperties.putAll(properties);
        }
        requestConverter = createRequestConverter();
        responseConverter = createResponseConverter();
        storage = createStorage();
        commandHandler = createCommandHandler();
    }

    @Override
    public ThreadFactory getWorkerThreadFactory() {
        return new ThreadFactory() {
            private int threadCount = 0;

            @Override
            public Thread newThread(final Runnable r) {
                Thread thread = new Thread(r, "Thread-" + threadCount);
                threadCount++;
                thread.setDaemon(true);
                return thread;
            }
        };
    }

    @Override
    public int getClearDataIntervalInMs() {
        String value = applicationProperties.getProperty(
                "storage.clear.interval.ms");
        try {
            int clearDataIntervalInMs = Integer.parseInt(value);
            if (clearDataIntervalInMs < 1000) {
                throw new JMemcachedConfigException(
                        "storage.clear.interval.ms should be more than 999 ms");
            }
            return clearDataIntervalInMs;
        } catch (NumberFormatException e) {
            throw new JMemcachedConfigException(
                    "storage.clear.interval.ms should be a number", e);
        }
    }

    @Override
    public int getServerPort() {
        String value = applicationProperties.getProperty("server.port");
        try {
            int port = Integer.parseInt(value);
            if (port < 0 || port > 65535) {
                throw new JMemcachedConfigException(
                        "server.port should be between 0-65535");
            }
            return port;
        } catch (NumberFormatException e) {
            throw new JMemcachedConfigException(
                    "server.port should be a number", e);
        }
    }

    @Override
    public int getInitThreadCount() {
        return getThreadCount("server.thread.count.init");
    }

    @Override
    public int getMaxThreadCount() {
        return getThreadCount("server.thread.count.max");
    }

    @Override
    public ClientSocketHandler buildNewClientSocketHandler(
            final Socket clientSocket) {
        return new ClientSocketHandlerImpl(clientSocket, this);
    }

    @Override
    public void close() throws Exception {
        storage.close();
    }

    @Override
    public String toString() {
        return String.format(
                "ServerConfigImpl: port=%s, initThreadCount=%s,"
                        + " maxThreadCount=%s, clearDataIntervalInMs=%sms",
                getServerPort(), getInitThreadCount(),
                getMaxThreadCount(), getClearDataIntervalInMs());
    }

    protected CommandHandler createCommandHandler() {
        return new CommandHandlerImpl(storage);
    }

    protected Storage createStorage() {
        return new StorageImpl(this);
    }

    protected ResponseConverter createResponseConverter() {
        return new ResponseConverterImpl();
    }

    protected RequestConverter createRequestConverter() {
        return new RequestConverterImpl();
    }

    protected InputStream getClassPathResourceInputStream(
            final String resource) {
        return getClass().getClassLoader().getResourceAsStream(resource);
    }

    protected void loadApplicationProperties(final String resource) {
        try (InputStream is = getClassPathResourceInputStream(resource)) {
            if (is == null) {
                throw new JMemcachedConfigException("Not found resource: "
                        + resource);
            } else {
                applicationProperties.load(is);
            }
        } catch (IOException e) {
            throw new JMemcachedConfigException("Can't load: " + resource, e);
        }
    }

    protected int getThreadCount(final String propertyName) {
        String value = applicationProperties.getProperty(propertyName);
        try {
            int threadCount = Integer.parseInt(value);
            if (threadCount < 1) {
                throw new JMemcachedConfigException(propertyName
                        + " should be more than 0");
            }
            return threadCount;
        } catch (NumberFormatException e) {
            throw new JMemcachedConfigException(propertyName
                    + " should be a number", e);
        }
    }
}
