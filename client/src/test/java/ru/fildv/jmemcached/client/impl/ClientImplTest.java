package ru.fildv.jmemcached.client.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
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
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ClientImplTest {
    private ClientImpl client;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ClientConfig clientConfig;
    private RequestConverter requestConverter;
    private ResponseConverter responseConverter;
    private ObjectSerializer objectSerializer;

    @BeforeEach
    public void init() throws IOException {
        socket = mock(Socket.class);
        outputStream = mock(OutputStream.class);
        inputStream = mock(InputStream.class);
        when(socket.getInputStream()).thenReturn(inputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);

        clientConfig = mock(ClientConfig.class);
        requestConverter = mock(RequestConverter.class);
        responseConverter = mock(ResponseConverter.class);
        objectSerializer = mock(ObjectSerializer.class);
        when((clientConfig.getRequestConverter())).thenReturn(requestConverter);
        when((clientConfig.getResponseConverter())).thenReturn(responseConverter);
        when(clientConfig.getObjectSerializer()).thenReturn(objectSerializer);

        client = new ClientImpl(clientConfig) {
            @Override
            protected Socket createSocket(ClientConfig clientConfig) throws IOException {
                return socket;
            }
        };
    }

    @Test
    void makeRequest() throws IOException {
        Request request = new Request(Command.CLEAR);
        when(responseConverter.readResponse(inputStream)).thenReturn(new Response(Status.CLEARED));

        Response response = client.makeRequest(request);
        assertAll(() -> {
            assertEquals(Status.CLEARED, response.getStatus());
            verify(requestConverter).writeRequest(outputStream, request);
            verify(responseConverter).readResponse(inputStream);
        });
    }

    @Test
    void put() throws IOException {
        String key = "key";
        Object value = "value";
        byte[] array = {1, 2, 3};

        when(objectSerializer.toByteArray(value)).thenReturn(array);
        when(responseConverter.readResponse(inputStream)).thenReturn(new Response(Status.ADDED));

        Status status = client.put(key, value);
        assertAll(() -> {
            assertEquals(Status.ADDED, status);
            verify(objectSerializer).toByteArray(value);
            verify(requestConverter).writeRequest(same(outputStream), equalTo(new Request(Command.PUT, key, null, array)));
        });
    }

    @Test
    public void putFull() throws IOException {
        String key = "key";
        Object value = "value";
        byte[] array = {1, 2, 3};
        when(responseConverter.readResponse(inputStream)).thenReturn(new Response(Status.REPLACED));
        when(objectSerializer.toByteArray(value)).thenReturn(array);
        Status status = client.put(key, value, 1, TimeUnit.MILLISECONDS);

        assertAll(() -> {
            assertEquals(Status.REPLACED, status);
            verify(objectSerializer).toByteArray(value);
            verify(requestConverter).writeRequest(same(outputStream), equalTo(new Request(Command.PUT, key, 1L, array)));
        });
    }

    @Test
    public void putFullInvalidTtl() throws IOException {
        String key = "key";
        Object value = "value";
        byte[] array = {1, 2, 3};
        when(responseConverter.readResponse(inputStream)).thenReturn(new Response(Status.REPLACED));
        when(objectSerializer.toByteArray(value)).thenReturn(array);
        Status status = client.put(key, value, 1, null);

        assertAll(() -> {
            assertEquals(Status.REPLACED, status);
            verify(objectSerializer).toByteArray(value);
            verify(requestConverter).writeRequest(same(outputStream), equalTo(new Request(Command.PUT, key, null, array)));
        });
    }

    @Test
    public void get() throws IOException {
        String key = "key";
        Object value = "value";
        byte[] array = {1, 2, 3};
        when(responseConverter.readResponse(inputStream)).thenReturn(new Response(Status.GOTTEN, array));
        when(objectSerializer.toObject(array)).thenReturn(value);

        String result = client.get(key);
        assertAll(() -> {
            assertEquals(value, result);
            verify(objectSerializer).toObject(array);
            verify(requestConverter).writeRequest(same(outputStream), equalTo(new Request(Command.GET, key)));
        });
    }

    @Test
    public void remove() throws IOException {
        String key = "key";
        when(responseConverter.readResponse(inputStream)).thenReturn(new Response(Status.REMOVED));

        Status status = client.remove(key);
        assertAll(() -> {
            assertEquals(Status.REMOVED, status);
            verify(requestConverter).writeRequest(same(outputStream), equalTo(new Request(Command.REMOVE, key)));
        });
    }

    @Test
    public void clear() throws IOException {
        when(responseConverter.readResponse(inputStream)).thenReturn(new Response(Status.CLEARED));

        Status status = client.clear();
        assertAll(() -> {
            assertEquals(Status.CLEARED, status);
            verify(requestConverter).writeRequest(same(outputStream), equalTo(new Request(Command.CLEAR)));
        });
    }

    @Test
    public void close() throws IOException {
        client.close();
        verify(socket).close();
    }


    private Request equalTo(Request request) {
        return argThat(new ArgumentMatcher<Request>() {
            @Override
            public boolean matches(Request arg) {
                return Objects.equals(arg.getCommand(), request.getCommand())
                        && Objects.equals(arg.getKey(), request.getKey())
                        && Objects.equals(arg.getTtl(), request.getTtl())
                        && Arrays.equals(arg.getData(), request.getData());
            }
        });
    }
}