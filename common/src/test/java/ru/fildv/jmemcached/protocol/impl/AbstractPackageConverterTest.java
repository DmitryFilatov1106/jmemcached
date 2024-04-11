package ru.fildv.jmemcached.protocol.impl;

import org.junit.jupiter.api.Test;
import ru.fildv.jmemcached.exception.JMemcachedException;

import static org.junit.jupiter.api.Assertions.*;

class AbstractPackageConverterTest {
    private final AbstractPackageConverter apc = new AbstractPackageConverter() {
    };

    @Test
    void checkProtocolVersion() {
        try {
            apc.checkProtocolVersion((byte) 16);
        } catch (Exception e) {
            fail("Version must be 1.0");
        }
    }

    @Test
    void checkProtocolVersionException() {
        JMemcachedException e = assertThrows(JMemcachedException.class, () -> {
            apc.checkProtocolVersion((byte) 0);
        });
        assertEquals(JMemcachedException.class, e.getClass());
    }

    @Test
    void getVersionByte() {
        assertEquals(16, apc.getVersionByte());
    }
}