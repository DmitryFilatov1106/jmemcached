package ru.fildv.jmemcached.exception;

public class JMemcachedException extends RuntimeException {
    public JMemcachedException(final String message) {
        super(message);
    }

    public JMemcachedException(final Throwable cause) {
        super(cause);
    }

    public JMemcachedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
