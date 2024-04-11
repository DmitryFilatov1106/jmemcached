package ru.fildv.jmemcached.protocol.impl;

import org.apache.commons.io.IOUtils;
import ru.fildv.jmemcached.protocol.ResponseConverter;
import ru.fildv.jmemcached.protocol.model.Response;
import ru.fildv.jmemcached.protocol.model.Status;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResponseConverterImpl extends AbstractPackageConverter
        implements ResponseConverter {
    @Override
    public Response readResponse(final InputStream inputStream)
            throws IOException {
        DataInputStream dis = new DataInputStream(inputStream);
        checkProtocolVersion(dis.readByte());
        byte status = dis.readByte();
        Response response = new Response(Status.valueOf(status));
        byte flagData = dis.readByte();
        if (flagData == 1) {
            int dataLength = dis.readInt();
            response.setData(IOUtils.readFully(dis, dataLength));
        }
        return response;
    }

    @Override
    public void writeResponse(final OutputStream outputStream,
                              final Response response) throws IOException {
        DataOutputStream dos = new DataOutputStream(outputStream);
        dos.writeByte(getVersionByte());
        dos.writeByte(response.getStatus().getByteCode());
        dos.writeByte(response.hasData() ? 1 : 0);
        if (response.hasData()) {
            dos.writeInt(response.getData().length);
            dos.write(response.getData());
        }
        dos.flush();
    }
}
