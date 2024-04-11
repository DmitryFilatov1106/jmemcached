package ru.fildv.jmemcached.protocol;

public interface ObjectSerializer {
    byte[] toByteArray(Object object);

    Object toObject(byte[] bytes);
}
