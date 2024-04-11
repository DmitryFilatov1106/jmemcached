package ru.fildv.jmemcached.protocol.impl;

import org.junit.jupiter.api.Test;
import ru.fildv.jmemcached.exception.JMemcachedException;
import ru.fildv.jmemcached.protocol.test.SerializableFiledClass;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ObjectSerializerImplTest {
    private final ObjectSerializerImpl os = new ObjectSerializerImpl();
    private final Object object = "Test";
    private final byte[] testByteArray = {-84, -19, 0, 5, 116,
            0, 4, 84, 101, 115, 116};
    private final byte[] testByteArray2 = {-84, -19, 0, 5, 115,
            114, 0, 56, 114, 117, 46, 102, 105, 108, 100, 118,
            46, 106, 109, 101, 109, 99, 97, 99, 104, 101, 100,
            46, 112, 114, 111, 116, 111, 99, 111, 108, 46, 116,
            101, 115, 116, 46, 83, 101, 114, 105, 97, 108, 105,
            122, 97, 98, 108, 101, 70, 105, 108, 101, 100, 67,
            108, 97, 115, 115, 27, -6, -94, 94, 95, 76, 110, -89,
            2, 0, 0, 120, 112};


    @Test
    void toByteArray() {
        byte[] actual = os.toByteArray(object);
        assertArrayEquals(testByteArray, actual);
    }

    @Test
    void toByteArrayNull() {
        var actual = os.toByteArray(null);
        assertNull(actual);
    }

    @Test
    void toByteArraySerializableException() {
        JMemcachedException e = assertThrows(JMemcachedException.class, () -> {
            os.toByteArray(new Object());
        });
        assertEquals("Class java.lang.Object doesn't implement java.io.Serializable interface", e.getMessage());
    }

    @Test
    void toByteArrayIOException() {
        JMemcachedException e = assertThrows(JMemcachedException.class, () -> {
            os.toByteArray(new SerializableFiledClass());
        });
        var actualException = e.getCause();
        assertAll(() -> {
            assertEquals(IOException.class, actualException.getClass());
            assertEquals("Can't convert object to byte array: Write IO", e.getMessage());
            assertEquals("Write IO", actualException.getMessage());
        });
    }

    @Test
    void toObject() {
        String actual = (String) os.toObject(testByteArray);
        assertEquals(object, actual);
    }

    @Test
    void toObjectFromNull() {
        var actual = os.toObject(null);
        assertNull(actual);
    }

    @Test
    void toObjectIOException() {
        JMemcachedException e = assertThrows(JMemcachedException.class, () -> {
            os.toObject(testByteArray2);
        });
        var actual = e.getCause();
        assertAll(() -> {
            assertEquals(IOException.class, actual.getClass());
            assertEquals("Can't convert byte array to object: Read IO", e.getMessage());
            assertEquals("Read IO", actual.getMessage());
        });
    }
}