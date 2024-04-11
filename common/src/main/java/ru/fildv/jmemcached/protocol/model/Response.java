package ru.fildv.jmemcached.protocol.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Response extends AbstractPacked {
    private final Status status;

    public Response(final Status status, final byte[] data) {
        super(data);
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(getStatus().name());
        if (hasData()) {
            s.append("=").append(getData().length).append(" bytes");
        }
        return s.toString();
    }
}
