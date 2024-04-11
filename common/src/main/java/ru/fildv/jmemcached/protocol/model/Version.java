package ru.fildv.jmemcached.protocol.model;

import ru.fildv.jmemcached.exception.JMemcachedException;

public enum Version {
    VERSION_0_0(0, 0),
    VERSION_1_0(1, 0), // real version
    VERSION_7_15(7, 15); // max possible version!!

    private final byte high;
    private final byte low;

    Version(final int high, final int low) {
        this.high = (byte) (high & 0x7); // 00000111
        this.low = (byte) (low & 0xF);   // 00001111
    }

    public static Version valueOf(final byte byteCode) {
        for (Version v : Version.values()) {
            if (v.getByteCode() == byteCode) {
                return v;
            }
        }
        throw new JMemcachedException("Unsupported byteCode for Version: "
                + byteCode);
    }

    public byte getByteCode() {
        return (byte) ((high << 4) + low);
    }

    @Override
    public String toString() {
        return String.format("%s.%s", high, low);
    }
}
