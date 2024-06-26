package ru.fildv.jmemcached.protocol;

import ru.fildv.jmemcached.protocol.model.Request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RequestConverter {
    Request readRequest(InputStream inputStream) throws IOException;

    void writeRequest(OutputStream outputStream, Request request)
            throws IOException;
}
