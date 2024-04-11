package ru.fildv.jmemcached.server.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import ru.fildv.jmemcached.protocol.RequestConverter;
import ru.fildv.jmemcached.protocol.ResponseConverter;
import ru.fildv.jmemcached.protocol.model.Request;
import ru.fildv.jmemcached.protocol.model.Response;
import ru.fildv.jmemcached.server.CommandHandler;
import ru.fildv.jmemcached.server.ServerConfig;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientSocketHandlerImplTest {
    private Socket socket;
    private ServerConfig serverConfig;
    private RequestConverter requestConverter;
    private ResponseConverter responseConverter;
    private CommandHandler commandHandler;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ClientSocketHandlerImpl defaultClientSocketHandler;
    private Request request;
    private Response response;

    @BeforeEach
    public void before() throws Exception {
        socket = mock(Socket.class);
        SocketAddress socketAddress = mock(SocketAddress.class);
        when(socketAddress.toString()).thenReturn("localhost");
        when(socket.getRemoteSocketAddress()).thenReturn(socketAddress);
        serverConfig = mock(ServerConfig.class);
        requestConverter = mock(RequestConverter.class);
        when(serverConfig.getRequestConverter()).thenReturn(requestConverter);
        responseConverter = mock(ResponseConverter.class);
        when(serverConfig.getResponseConverter()).thenReturn(responseConverter);
        commandHandler = mock(CommandHandler.class);
        when(serverConfig.getCommandHandler()).thenReturn(commandHandler);
        inputStream = mock(InputStream.class);
        when(socket.getInputStream()).thenReturn(inputStream);
        outputStream = mock(OutputStream.class);
        when(socket.getOutputStream()).thenReturn(outputStream);
        request = mock(Request.class);
        response = mock(Response.class);
        defaultClientSocketHandler = spy(new ClientSocketHandlerImpl(socket, serverConfig) {
            private boolean stop = true;

            @Override
            protected boolean interrupted() {
                stop = !stop;
                // interrupted should return false and then true
                return stop;
            }
        });
    }

    private void verifyCommonRequiredOperations() throws IOException {
        assertAll(() -> {
            verify(serverConfig).getRequestConverter();
            verify(serverConfig).getResponseConverter();
            verify(serverConfig).getCommandHandler();
            verify(socket).getInputStream();
            verify(socket).getOutputStream();
            verify(socket).close();
        });
    }

    @Test
    public void successRun() throws IOException {
        when(requestConverter.readRequest(inputStream)).thenReturn(request);
        when(commandHandler.handle(request)).thenReturn(response);

        defaultClientSocketHandler.run();
        assertAll(() -> {
            verifyCommonRequiredOperations();
            verify(requestConverter).readRequest(inputStream);
            verify(commandHandler).handle(request);
            verify(responseConverter).writeResponse(outputStream, response);
            verify(defaultClientSocketHandler, times(2)).interrupted();
        });
    }

    static Stream<Exception> testDataForRunWithExceptionsMethod() {
        return Stream.of(new RuntimeException("Test"),
                new EOFException("Test"),
                new SocketException("Test"),
                new IOException("Test"));
    }

    @ParameterizedTest
    @MethodSource("testDataForRunWithExceptionsMethod")
    public void runWithExceptions(Exception exception) throws Exception {
        when(requestConverter.readRequest(inputStream)).thenThrow(exception);

        defaultClientSocketHandler.run();
        assertAll(() -> {
            verifyCommonRequiredOperations();
            verify(requestConverter).readRequest(inputStream);
            verify(commandHandler, never()).handle(request);
            verify(responseConverter, never()).writeResponse(outputStream, response);
            verify(defaultClientSocketHandler).interrupted();
        });
    }

    @Test
    public void runtimeExceptionLoggerMessage() throws IOException {
        RuntimeException ex = new RuntimeException("RuntimeException");
        when(requestConverter.readRequest(inputStream)).thenThrow(ex);
        defaultClientSocketHandler.run();
    }

    @Test
    public void eofExceptionLoggerMessage() throws IOException {
        when(requestConverter.readRequest(inputStream)).thenThrow(new EOFException("EOFException"));
        defaultClientSocketHandler.run();
    }

    @Test
    public void socketExceptionLoggerMessage() throws IOException {
        when(requestConverter.readRequest(inputStream)).thenThrow(new SocketException("SocketException"));
        defaultClientSocketHandler.run();
    }

    @Test
    public void ioExceptionLoggerMessage() throws IOException {
        when(socket.isClosed()).thenReturn(false);
        IOException ex = new IOException("IOException");
        when(requestConverter.readRequest(inputStream)).thenThrow(ex);
        defaultClientSocketHandler.run();
    }

    @Test
    public void ioExceptionWithoutLoggerMessage() throws IOException {
        when(socket.isClosed()).thenReturn(true);
        IOException ex = new IOException("IOException");
        when(requestConverter.readRequest(inputStream)).thenThrow(ex);
        defaultClientSocketHandler.run();
    }

    @Test
    public void socketCloseErrorLoggerMessage() throws IOException {
        IOException ex = new IOException("IOException");
        doThrow(ex).when(socket).close();
        defaultClientSocketHandler.run();
    }

    @Test
    public void interrupted() {
        defaultClientSocketHandler = new ClientSocketHandlerImpl(socket, serverConfig);
        assertFalse(defaultClientSocketHandler.interrupted());

        Thread.currentThread().interrupt();
        assertTrue(defaultClientSocketHandler.interrupted());
    }
}
