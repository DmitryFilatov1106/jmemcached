package ru.fildv.jmemcached.protocol.impl;

import org.junit.jupiter.api.Test;
import ru.fildv.jmemcached.protocol.model.Response;
import ru.fildv.jmemcached.protocol.model.Status;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ResponseConverterImplTest {
    private final ResponseConverterImpl rci = new ResponseConverterImpl();
    private final byte[] DATA = new byte[]{1, 2, 3};

    // version(1), status(1), flag(1)
    private final byte[] TEST_ARRAY_ADDED = new byte[]{16, 0, 0};
    private final byte[] TEST_ARRAY_GOTTEN = new byte[]{16, 2, 0};


    // version(1), status(1), flag(1), length(4), byte array(3)
    private final byte[] TEST_ARRAY_WITH_DATA = new byte[]{16, 0, 1, 0, 0, 0, 3, 1, 2, 3};

    @Test
    void readResponseWithoutData() throws IOException {
        Response response = rci.readResponse(new ByteArrayInputStream(TEST_ARRAY_ADDED));
        assertAll(() -> {
            assertEquals(Status.ADDED, response.getStatus());
            assertFalse(response.hasData());
        });
    }

    @Test
    void readResponseWithData() throws IOException {
        Response response = rci.readResponse(new ByteArrayInputStream(TEST_ARRAY_WITH_DATA));
        assertAll(() -> {
            assertEquals(Status.ADDED, response.getStatus());
            assertTrue(response.hasData());
            assertArrayEquals(DATA, response.getData());
        });
    }

    @Test
    void writeResponseWithoutData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Response response = new Response(Status.GOTTEN);
        rci.writeResponse(baos, response);
        assertArrayEquals(TEST_ARRAY_GOTTEN, baos.toByteArray());
    }

    @Test
    void writeResponseWithData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Response response = new Response(Status.ADDED, DATA);
        rci.writeResponse(baos, response);
        assertArrayEquals(TEST_ARRAY_WITH_DATA, baos.toByteArray());
    }
}
