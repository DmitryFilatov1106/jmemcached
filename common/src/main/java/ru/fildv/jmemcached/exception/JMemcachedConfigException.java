package ru.fildv.jmemcached.exception;

public class JMemcachedConfigException extends JMemcachedException {
    public JMemcachedConfigException(final String message) {
        super(message);
    }

    public JMemcachedConfigException(final String message,
                                     final Throwable cause) {
        super(message, cause);
    }
}
