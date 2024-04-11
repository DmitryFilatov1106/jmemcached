package ru.fildv.jmemcached.server.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.fildv.jmemcached.exception.JMemcachedException;
import ru.fildv.jmemcached.protocol.model.Request;
import ru.fildv.jmemcached.protocol.model.Response;
import ru.fildv.jmemcached.protocol.model.Status;
import ru.fildv.jmemcached.server.CommandHandler;
import ru.fildv.jmemcached.server.Storage;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class CommandHandlerImpl implements CommandHandler {
    private final Storage storage;

    @Override
    public Response handle(final Request request) {
        if (request == null) {
            throw new JMemcachedException(
                    "Unsupported command with: request = null");
        }
        Status status;
        byte[] data = null;
        status = switch (request.getCommand()) {
            case CLEAR -> storage.clear();
            case PUT -> storage.put(request.getKey(), request.getTtl(),
                    request.getData());
            case REMOVE -> storage.remove(request.getKey());
            case GET -> {
                data = storage.get(request.getKey());
                yield data == null ? Status.NOT_FOUND : Status.GOTTEN;
            }
//            default -> throw new JMemcachedException("Unsupported command: "
//                    + request.getCommand());
        };
        return new Response(status, data);
    }
}
