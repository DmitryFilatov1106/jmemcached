package ru.fildv.jmemcachedsomeproject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.fildv.jmemcached.client.Client;
import ru.fildv.jmemcached.client.impl.JMemcachedClientFactory;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class ExternalProject {
    public static void main(final String[] args) {
        try (Client client = JMemcachedClientFactory
                .buildNewClient("localhost", 9010)) {

            var key = "test";
            var value = "Hello world";
            var value2 = new Person("Ivan", "Ivanov", 25);
            var value3 = "It`s JMemcached";

            var status = client.put(key, value);
            System.out.println("1: command put with key=" + key
                    + " value=" + value + " -> status=" + status);
            var result = client.get(key);
            System.out.println("   command get with key=" + key
                    + " -> result=" + result);

            status = client.remove(key);
            System.out.println("2: command remove with key=" + key
                    + " -> status=" + status);
            result = client.get(key);
            System.out.println("   command get with key=" + key
                    + " -> result=" + result);

            status = client.put(key, value);
            System.out.println("3: command put with key=" + key
                    + " value=" + value + " -> status=" + status);
            result = client.get(key);
            System.out.println("   command get with key=" + key
                    + " -> result=" + result);
            var status2 = client.put(key, value2);
            System.out.println("   command put with key=" + key
                    + " value=" + value2 + " -> status=" + status2);
            var result2 = client.get(key);
            System.out.println("   command get with key=" + key
                    + " -> result=" + result2);

            status = client.clear();
            System.out.println("4: command clear -> status=" + status);
            result = client.get(key);
            System.out.println("   command get with key=" + key
                    + " -> result=" + result);

            key = "newkey";
            status = client.put(key, value3, 2, TimeUnit.SECONDS);
            System.out.println("5: command put with key=" + key
                    + " value=" + value3 + " ttl=2 seconds -> status="
                    + status);
            TimeUnit.SECONDS.sleep(1);
            System.out.println("   sleep 1 second");
            result = client.get(key);
            System.out.println("   command get with key=" + key
                    + " -> result=" + result);
            TimeUnit.SECONDS.sleep(3);
            System.out.println("   sleep 3 seconds");
            result = client.get(key);
            System.out.println("   command get with key=" + key
                    + " -> result=" + result);
        } catch (Exception ignored) {
        }
    }

    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    private static class Person implements Serializable {
        private String firstname;
        private String lastname;
        private int age;
    }
}


