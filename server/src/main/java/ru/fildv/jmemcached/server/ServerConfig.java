package ru.fildv.jmemcached.server;

import ru.fildv.jmemcached.protocol.RequestConverter;
import ru.fildv.jmemcached.protocol.ResponseConverter;

import java.net.Socket;
import java.util.concurrent.ThreadFactory;

public interface ServerConfig extends AutoCloseable {
    RequestConverter getRequestConverter();

    ResponseConverter getResponseConverter();

    ThreadFactory getWorkerThreadFactory();

    int getServerPort();

    int getInitThreadCount();

    int getMaxThreadCount();

    int getClearDataIntervalInMs();

    Storage getStorage();

    CommandHandler getCommandHandler();

    ClientSocketHandler buildNewClientSocketHandler(Socket clientSocket);
}
