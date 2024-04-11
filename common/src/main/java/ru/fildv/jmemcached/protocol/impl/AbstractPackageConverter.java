package ru.fildv.jmemcached.protocol.impl;

import ru.fildv.jmemcached.exception.JMemcachedException;
import ru.fildv.jmemcached.protocol.model.Version;

abstract class AbstractPackageConverter {
    protected void checkProtocolVersion(final byte versionByte) {
        Version version = Version.valueOf(versionByte);
        if (version != Version.VERSION_1_0) {
            throw new JMemcachedException("Unsupported protocol version: "
                    + versionByte);
        }
    }

    protected byte getVersionByte() {
        return Version.VERSION_1_0.getByteCode();
    }
}
