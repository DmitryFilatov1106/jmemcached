package ru.fildv.jmemcached.protocol.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {
    private Request request;

    @BeforeEach
    public void before() {
        request = new Request(Command.CLEAR);
    }

    @Test
    void getCommand() {
        assertEquals(Command.CLEAR, request.getCommand());
    }

    @Test
    void getKey() {
        String key = "key";
        request.setKey(key);
        assertEquals(key, request.getKey());
    }

    @Test
    void getTtl() {
        Long ttl = 100L;
        request.setTtl(ttl);
        assertEquals(ttl, request.getTtl());
    }

    @Test
    void hasKeyTrue() {
        String key = "key";
        request.setKey(key);
        assertTrue(request.hasKey());
    }

    @Test
    void hasKeyFalse() {
        assertFalse(request.hasKey());
    }

    @Test
    void hasTtlTrue() {
        Long ttl = 100L;
        request.setTtl(ttl);
        assertTrue(request.hasTtl());
    }

    @Test
    void hasTtlFalse() {
        assertFalse(request.hasTtl());
    }

    @Test
    void testToString() {
        assertAll(() -> {
            assertEquals("CLEAR", request.toString());

            request.setKey("key");
            assertEquals("CLEAR[key]", request.toString());

            request.setTtl(100L);
            assertEquals("CLEAR[key] (Thu Jan 01 03:00:00 GMT+03:00 1970)", request.toString());

            request.setData("Test".getBytes());
            assertEquals("CLEAR[key]=4 bytes (Thu Jan 01 03:00:00 GMT+03:00 1970)", request.toString());
        });
    }
}