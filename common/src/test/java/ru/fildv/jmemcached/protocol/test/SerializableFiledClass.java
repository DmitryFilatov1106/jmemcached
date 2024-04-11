package ru.fildv.jmemcached.protocol.test;

import java.io.*;

public class SerializableFiledClass implements Serializable {
    @Serial
    private void readObject(ObjectInputStream objectInputStream) throws IOException {
        throw new IOException("Read IO");
    }

    @Serial
    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        throw new IOException("Write IO");
    }
}
