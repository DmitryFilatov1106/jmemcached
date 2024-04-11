package ru.fildv.jmemcached.protocol.impl;

import org.apache.commons.io.IOUtils;
import ru.fildv.jmemcached.exception.JMemcachedException;
import ru.fildv.jmemcached.protocol.RequestConverter;
import ru.fildv.jmemcached.protocol.model.Command;
import ru.fildv.jmemcached.protocol.model.Request;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class RequestConverterImpl extends AbstractPackageConverter
        implements RequestConverter {
    @Override
    public Request readRequest(final InputStream inputStream)
            throws IOException {
        DataInputStream dis = new DataInputStream(inputStream);
        checkProtocolVersion(dis.readByte());
        byte command = dis.readByte();
        byte flagData = dis.readByte();
        boolean hasKey = (flagData & 1) != 0;
        boolean hasTTL = (flagData & 2) != 0;
        boolean hasData = (flagData & 4) != 0;

        return readRequest(dis, command, hasKey, hasTTL, hasData);
    }

    @Override
    public void writeRequest(final OutputStream outputStream,
                             final Request request) throws IOException {
        DataOutputStream dos = new DataOutputStream(outputStream);
        dos.writeByte(getVersionByte());
        dos.writeByte(request.getCommand().getByteCode());
        dos.writeByte(getFlagByte(request));
        if (request.hasKey()) {
            writeKey(dos, request);
        }
        if (request.hasTtl()) {
            dos.writeLong(request.getTtl());
        }
        if (request.hasData()) {
            dos.writeInt(request.getData().length);
            dos.write(request.getData());
        }
        dos.flush();
    }

    private static Request readRequest(final DataInputStream dis,
                                       final byte command,
                                       final boolean hasKey,
                                       final boolean hasTTL,
                                       final boolean hasData)
            throws IOException {
        Request request = new Request(Command.valueOf(command));
        if (hasKey) {
            byte keyLength = dis.readByte();
            byte[] keyBytes = IOUtils.readFully(dis, keyLength);
            request.setKey(new String(keyBytes, US_ASCII));
        }
        if (hasTTL) {
            request.setTtl(dis.readLong());
        }
        if (hasData) {
            int dataLength = dis.readInt();
            request.setData(IOUtils.readFully(dis, dataLength));
        }
        return request;
    }

    private static byte getFlagByte(final Request request) {
        byte flag = 0;
        if (request.hasKey()) {
            flag |= 1;
        }
        if (request.hasTtl()) {
            flag |= 2;
        }
        if (request.hasData()) {
            flag |= 4;
        }
        return flag;
    }

    private static void writeKey(final DataOutputStream dos,
                                 final Request request) throws IOException {
        byte[] key = request.getKey().getBytes(US_ASCII);
        if (key.length > 127) {
            throw new JMemcachedException(
                    "Key length is more than 127 bytes for key: "
                            + request.getKey());
        }
        dos.writeByte(key.length);
        dos.write(key);
    }
}
