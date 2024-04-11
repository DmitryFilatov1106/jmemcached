package ru.fildv.jmemcached.protocol.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractPackedTest {
    private static AbstractPacked newInstance(byte[] array) {
        return new AbstractPacked(array) {
        };
    }

    @Test
    void hasDataNull() {
        AbstractPacked ap = newInstance(null);
        assertFalse(ap.hasData());
    }

    @Test
    void hasDataEmpty() {
        AbstractPacked ap = newInstance(new byte[0]);
        assertFalse(ap.hasData());
    }

    @Test
    void hasData() {
        AbstractPacked ap = newInstance(new byte[]{1, 2, 3});
        assertTrue(ap.hasData());
    }
}