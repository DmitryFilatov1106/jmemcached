package ru.fildv.jmemcached.protocol.model;

import org.junit.jupiter.api.Test;
import ru.fildv.jmemcached.exception.JMemcachedException;

import static org.junit.jupiter.api.Assertions.*;

class StatusTest {

    @Test
    void valueOf() {
        assertAll(
                () -> {
                    assertEquals(Status.ADDED, Status.valueOf((byte) 0));
                    assertEquals(Status.REPLACED, Status.valueOf((byte) 1));
                    assertEquals(Status.GOTTEN, Status.valueOf((byte) 2));
                    assertEquals(Status.NOT_FOUND, Status.valueOf((byte) 3));
                    assertEquals(Status.REMOVED, Status.valueOf((byte) 4));
                    assertEquals(Status.CLEARED, Status.valueOf((byte) 5));
                }
        );
    }

    @Test
    void valueOfThrowException() {
        JMemcachedException e = assertThrows(JMemcachedException.class, () -> {
            Status.valueOf(Byte.MAX_VALUE);
        });
        assertEquals("Unsupported byteCode for Status: 127", e.getMessage());
    }

    @Test
    void getByteCode() {
        assertAll(
                () -> {
                    assertEquals(0, Status.ADDED.getByteCode());
                    assertEquals(1, Status.REPLACED.getByteCode());
                    assertEquals(2, Status.GOTTEN.getByteCode());
                    assertEquals(3, Status.NOT_FOUND.getByteCode());
                    assertEquals(4, Status.REMOVED.getByteCode());
                    assertEquals(5, Status.CLEARED.getByteCode());
                }
        );
    }
}