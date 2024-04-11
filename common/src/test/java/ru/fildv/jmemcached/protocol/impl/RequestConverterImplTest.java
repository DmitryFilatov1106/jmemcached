package ru.fildv.jmemcached.protocol.impl;

import org.junit.jupiter.api.Test;
import ru.fildv.jmemcached.protocol.model.Command;
import ru.fildv.jmemcached.protocol.model.Request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RequestConverterImplTest {
    private final RequestConverterImpl rci = new RequestConverterImpl();
    private final byte[] DATA = new byte[]{1, 2, 3};

    // version(1), status(1), flag(1)
    private final byte[] TEST_ARRAY_CLEAR = new byte[]{16, 0, 0};

    // version(1), status(1), flag(1), key length(3), key bytes(3), ttl(8), data length(4), data bytes(3)
    private final byte[] TEST_ARRAY_PUT = new byte[]{16, 1, 7, 3, 49, 50, 51, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 3, 1, 2, 3};


    @Test
    void readRequestSimple() throws IOException {
        Request request = rci.readRequest(new ByteArrayInputStream(TEST_ARRAY_CLEAR));
        assertAll(() -> {
            assertEquals(Command.CLEAR, request.getCommand());
            assertFalse(request.hasKey());
            assertFalse(request.hasTtl());
            assertFalse(request.hasData());
        });
    }

    @Test
    void readRequestFull() throws IOException {
        Request request = rci.readRequest(new ByteArrayInputStream(TEST_ARRAY_PUT));
        assertAll(() -> {
            assertEquals(Command.PUT, request.getCommand());
            assertEquals("123", request.getKey());

            assertTrue(request.hasKey());
            assertEquals(Long.valueOf(5L), request.getTtl());

            assertTrue(request.hasData());
            assertArrayEquals(DATA, request.getData());
        });
    }

    @Test
    void writeRequest() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rci.writeRequest(baos, new Request(Command.CLEAR));
        assertArrayEquals(TEST_ARRAY_CLEAR, baos.toByteArray());
    }
}
