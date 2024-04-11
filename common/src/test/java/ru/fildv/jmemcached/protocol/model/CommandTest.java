package ru.fildv.jmemcached.protocol.model;

import org.junit.jupiter.api.Test;
import ru.fildv.jmemcached.exception.JMemcachedException;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {
    @Test
    void getByteCode() {
        assertAll(() -> {
            assertEquals((byte) 0, Command.CLEAR.getByteCode());
            assertEquals((byte) 1, Command.PUT.getByteCode());
            assertEquals((byte) 2, Command.GET.getByteCode());
            assertEquals((byte) 3, Command.REMOVE.getByteCode());
        });
    }

    @Test
    void valueOf() {
        assertAll(() -> {
            assertEquals(Command.CLEAR, Command.valueOf((byte) 0));
            assertEquals(Command.PUT, Command.valueOf((byte) 1));
            assertEquals(Command.GET, Command.valueOf((byte) 2));
            assertEquals(Command.REMOVE, Command.valueOf((byte) 3));
        });

    }

    @Test
    void valueOfException() {
        JMemcachedException e = assertThrows(JMemcachedException.class, () -> {
            Command.valueOf(Byte.MAX_VALUE);
        });
        assertEquals("Unsupported byteCode for Command: 127", e.getMessage());
    }
}