package ru.fildv.jmemcached.protocol.model;

import lombok.Getter;
import ru.fildv.jmemcached.exception.JMemcachedException;

@Getter
public enum Command {
    CLEAR(0), PUT(1), GET(2), REMOVE(3);

    private final byte byteCode;

    Command(final int code) {
        this.byteCode = (byte) code;
    }

    public static Command valueOf(final byte byteCode) {
        for (Command c : Command.values()) {
            if (c.getByteCode() == byteCode) {
                return c;
            }
        }
        throw new JMemcachedException("Unsupported byteCode for Command: "
                + byteCode);
    }
}
