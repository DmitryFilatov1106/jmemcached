package ru.fildv.jmemcached.protocol.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponseTest {

    @Test
    void testToStringWithData() {
        Response response = new Response(Status.GOTTEN, new byte[]{1, 2, 3});
        assertEquals("GOTTEN=3 bytes", response.toString());
    }

    @Test
    void testToStringWithOutData() {
        Response response = new Response(Status.ADDED);
        assertEquals("ADDED", response.toString());
    }
}