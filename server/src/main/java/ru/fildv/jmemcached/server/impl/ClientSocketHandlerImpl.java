package ru.fildv.jmemcached.server.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.fildv.jmemcached.protocol.RequestConverter;
import ru.fildv.jmemcached.protocol.ResponseConverter;
import ru.fildv.jmemcached.protocol.model.Request;
import ru.fildv.jmemcached.protocol.model.Response;
import ru.fildv.jmemcached.server.ClientSocketHandler;
import ru.fildv.jmemcached.server.CommandHandler;
import ru.fildv.jmemcached.server.ServerConfig;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class ClientSocketHandlerImpl implements ClientSocketHandler {
    private final Socket socket;
    private final ServerConfig serverConfig;

    protected boolean interrupted() {
        return Thread.interrupted();
    }

    @Override
    public void run() {
        try {
            RequestConverter requestConverter =
                    serverConfig.getRequestConverter();
            ResponseConverter responseConverter =
                    serverConfig.getResponseConverter();
            CommandHandler commandHandler = serverConfig.getCommandHandler();
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            while (!interrupted()) {
                try {
                    Request request = requestConverter.readRequest(inputStream);
                    Response response = commandHandler.handle(request);
                    responseConverter.writeResponse(outputStream, response);
                    log.debug("Command {} -> {}", request, response);
                } catch (RuntimeException e) {
                    log.error("Handle request failed {}", e.getMessage());
                    break;
                }
            }
        } catch (EOFException | SocketException e) {
            log.info("Remote client connection closed: {}",
                    socket.getRemoteSocketAddress().toString());
        } catch (IOException e) {
            if (!socket.isClosed()) {
                log.error("Error IOException {}", e.getMessage());
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Close socket error {}", e.getMessage());
            }
        }
    }
}
