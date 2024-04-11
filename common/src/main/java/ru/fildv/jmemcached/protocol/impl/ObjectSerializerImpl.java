package ru.fildv.jmemcached.protocol.impl;

import ru.fildv.jmemcached.exception.JMemcachedException;
import ru.fildv.jmemcached.protocol.ObjectSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectSerializerImpl implements ObjectSerializer {
    @Override
    public byte[] toByteArray(final Object object) {
        if (object == null) {
            return null;
        }
        if (!Serializable.class.isAssignableFrom(object.getClass())) {
            throw new JMemcachedException("Class "
                    + object.getClass().getName()
                    + " doesn't implement java.io.Serializable interface");
        }
        try {
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            ObjectOutputStream objectOS = new ObjectOutputStream(byteArrayOS);
            objectOS.writeObject(object);
            objectOS.flush();
            return byteArrayOS.toByteArray();
        } catch (IOException e) {
            throw new JMemcachedException("Can't convert object to byte array: "
                    + e.getMessage(), e);
        }
    }

    @Override
    public Object toObject(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            ObjectInputStream objectIS = new ObjectInputStream(
                    new ByteArrayInputStream(bytes));
            return objectIS.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new JMemcachedException(
                    "Can't convert byte array to object: "
                            + e.getMessage(), e);
        }
    }
}
