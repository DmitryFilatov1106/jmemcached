package ru.fildv.jmemcached.server.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.fildv.jmemcached.exception.JMemcachedException;
import ru.fildv.jmemcached.protocol.model.Command;
import ru.fildv.jmemcached.protocol.model.Request;
import ru.fildv.jmemcached.protocol.model.Response;
import ru.fildv.jmemcached.protocol.model.Status;
import ru.fildv.jmemcached.server.ServerConfig;
import ru.fildv.jmemcached.server.Storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommandHandlerImplTest {
    private Storage storage;
    private ServerConfig serverConfig;
    private CommandHandlerImpl commandHandler;

    @BeforeEach
    public void before() {
        storage = mock(Storage.class);
        serverConfig = mock(ServerConfig.class);
        when(serverConfig.getStorage()).thenReturn(storage);
        commandHandler = new CommandHandlerImpl(storage);
    }

    @Test
    public void handleClear() {
        when(storage.clear()).thenReturn(Status.CLEARED);
        Response response = commandHandler.handle(new Request(Command.CLEAR));
        assertAll(() -> {
            assertEquals(Status.CLEARED, response.getStatus());
            assertNull(response.getData());
            verify(storage).clear();
        });
    }

    @Test
    public void handlePut() {
        String key = "key";
        Long ttl = System.currentTimeMillis();
        byte[] data = {1, 2, 3};
        when(storage.put(key, ttl, data)).thenReturn(Status.ADDED);
        Response response = commandHandler.handle(new Request(Command.PUT, key, ttl, data));
        assertEquals(Status.ADDED, response.getStatus());
        assertNull(response.getData());
        verify(storage).put(key, ttl, data);
    }

    @Test
    public void handleRemove() {
        String key = "key";
        when(storage.remove(key)).thenReturn(Status.REMOVED);
        Response response = commandHandler.handle(new Request(Command.REMOVE, key));
        assertAll(() -> {
            assertEquals(Status.REMOVED, response.getStatus());
            assertNull(response.getData());
            verify(storage).remove(key);
        });
    }

    @Test
    public void handleGetNotFound() {
        String key = "key";
        when(storage.get(key)).thenReturn(null);
        Response response = commandHandler.handle(new Request(Command.GET, key));
        assertAll(() -> {
            assertEquals(Status.NOT_FOUND, response.getStatus());
            assertNull(response.getData());
            verify(storage).get(key);
        });
    }

    @Test
    public void handleGetFound() {
        String key = "key";
        byte[] data = {1, 2, 3};
        when(storage.get(key)).thenReturn(data);
        Response response = commandHandler.handle(new Request(Command.GET, key));
        assertAll(() -> {
            assertEquals(Status.GOTTEN, response.getStatus());
            assertArrayEquals(data, response.getData());
            verify(storage).get(key);
        });
    }

    @Test
    public void handleUnsupportedCommand() {
        JMemcachedException e = assertThrows(JMemcachedException.class, () ->
                commandHandler.handle(null)
        );
        assertEquals("Unsupported command with: request = null", e.getMessage());
    }
}
