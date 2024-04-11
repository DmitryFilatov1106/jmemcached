package ru.fildv.jmemcached.protocol.model;

import lombok.Getter;
import ru.fildv.jmemcached.exception.JMemcachedException;

@Getter
public enum Status {
    ADDED(0),
    REPLACED(1),
    GOTTEN(2),
    NOT_FOUND(3),
    REMOVED(4),
    CLEARED(5);

    private final byte byteCode;

    Status(final int byteCode) {
        this.byteCode = (byte) byteCode;
    }

    public static Status valueOf(final byte byteCode) {
        for (Status s : Status.values()) {
            if (s.getByteCode() == byteCode) {
                return s;
            }
        }
        throw new JMemcachedException("Unsupported byteCode for Status: "
                + byteCode);
    }
}
