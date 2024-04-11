package ru.fildv.jmemcached.server;

import ru.fildv.jmemcached.protocol.model.Request;
import ru.fildv.jmemcached.protocol.model.Response;

public interface CommandHandler {
    Response handle(Request request);
}
