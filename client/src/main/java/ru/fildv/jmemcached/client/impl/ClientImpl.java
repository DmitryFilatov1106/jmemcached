package ru.fildv.jmemcached.client.impl;

import ru.fildv.jmemcached.client.Client;
import ru.fildv.jmemcached.client.ClientConfig;
import ru.fildv.jmemcached.protocol.ObjectSerializer;
import ru.fildv.jmemcached.protocol.RequestConverter;
import ru.fildv.jmemcached.protocol.ResponseConverter;
import ru.fildv.jmemcached.protocol.model.Command;
import ru.fildv.jmemcached.protocol.model.Request;
import ru.fildv.jmemcached.protocol.model.Response;
import ru.fildv.jmemcached.protocol.model.Status;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

class ClientImpl implements Client {
    private final RequestConverter requestConverter;
    private final ResponseConverter responseConverter;
    private final ObjectSerializer objectSerializer;
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    ClientImpl(final ClientConfig clientConfig) throws IOException {
        this.requestConverter = clientConfig.getRequestConverter();
        this.responseConverter = clientConfig.getResponseConverter();
        this.objectSerializer = clientConfig.getObjectSerializer();
        this.socket = createSocket(clientConfig);
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    @Override
    public Status put(final String key, final Object object)
            throws IOException {
        return put(key, object, null, null);
    }

    @Override
    public Status put(final String key, final Object object,
                      final Integer ttl, final TimeUnit timeUnit)
            throws IOException {
        byte[] data = objectSerializer.toByteArray(object);
        Long requestTtl = ttl != null && timeUnit != null
                ? timeUnit.toMillis(ttl) : null;
        Response response = makeRequest(new Request(Command.PUT, key,
                requestTtl, data));
        return response.getStatus();
    }

    @Override
    public <T> T get(final String key) throws IOException {
        Response response = makeRequest(new Request(Command.GET, key));
        return (T) objectSerializer.toObject(response.getData());
    }

    @Override
    public Status remove(final String key) throws IOException {
        Response response = makeRequest(new Request(Command.REMOVE, key));
        return response.getStatus();
    }

    @Override
    public Status clear() throws IOException {
        Response response = makeRequest(new Request(Command.CLEAR));
        return response.getStatus();
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }

    protected Socket createSocket(final ClientConfig clientConfig)
            throws IOException {
        Socket socket = new Socket(clientConfig.getHost(),
                clientConfig.getPort());
        socket.setKeepAlive(true);
        return socket;
    }

    protected Response makeRequest(final Request request) throws IOException {
        requestConverter.writeRequest(outputStream, request);
        return responseConverter.readResponse(inputStream);
    }
}
