package ru.fildv.jmemcached.protocol.model;

import org.junit.jupiter.api.Test;
import ru.fildv.jmemcached.exception.JMemcachedException;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {
    @Test
    void valueOf() {
        assertAll(() -> {
            assertEquals(Version.VERSION_0_0, Version.valueOf((byte) 0));
            assertEquals(Version.VERSION_1_0, Version.valueOf((byte) 16));
            assertEquals(Version.VERSION_7_15, Version.valueOf((byte) 127));
        });
    }

    @Test
    void valueOfThrowException() {
        JMemcachedException e = assertThrows(JMemcachedException.class, () -> {
            Version.valueOf(Byte.MIN_VALUE);
        });
        assertEquals("Unsupported byteCode for Version: -128", e.getMessage());
    }

    @Test
    void getByteCode() {
        assertAll(() -> {
            assertEquals((byte) 0, Version.VERSION_0_0.getByteCode());
            assertEquals((byte) 16, Version.VERSION_1_0.getByteCode());
            assertEquals((byte) 127, Version.VERSION_7_15.getByteCode());
        });
    }

    @Test
    void testToString() {
        assertAll(() -> {
            assertEquals("0.0", Version.VERSION_0_0.toString());
            assertEquals("1.0", Version.VERSION_1_0.toString());
            assertEquals("7.15", Version.VERSION_7_15.toString());
        });
    }
}
