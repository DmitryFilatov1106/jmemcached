package ru.fildv.jmemcached.protocol.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
abstract class AbstractPacked {
    private byte[] data;

    public boolean hasData() {
        return data != null && data.length > 0;
    }
}
