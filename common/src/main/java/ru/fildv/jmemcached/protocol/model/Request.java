package ru.fildv.jmemcached.protocol.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;

@RequiredArgsConstructor
@Getter
@Setter
public class Request extends AbstractPacked {
    private final Command command;
    private String key;
    private Long ttl;

    public Request(final Command command,
                   final String key,
                   final Long ttl,
                   final byte[] data) {
        super(data);
        this.command = command;
        this.key = key;
        this.ttl = ttl;
    }

    public Request(final Command command, final String key) {
        this.command = command;
        this.key = key;
    }

    public boolean hasKey() {
        return key != null;
    }

    public boolean hasTtl() {
        return ttl != null;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(getCommand().name());
        if (hasKey()) {
            s.append("[").append(getKey()).append("]");
        }
        if (hasData()) {
            s.append("=").append(getData().length).append(" bytes");
        }
        if (hasTtl()) {
            s.append(" (").append(new Date(ttl)).append(")");
        }
        return s.toString();
    }
}
