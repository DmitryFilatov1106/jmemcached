package ru.fildv.jmemcached.server;

import lombok.extern.slf4j.Slf4j;
import ru.fildv.jmemcached.server.impl.JMemcachedServerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

// Command line interface
@Slf4j
public class CLI {
    private static final List<String> QUIT_LIST = List.of("q", "quit", "exit");

    public static void main(final String[] args) {
        Thread.currentThread().setName("CLI-main thread");
        try {
            Server server = JMemcachedServerFactory.buildNewServer(null);
            server.start();
            waitForStopCommand(server);
        } catch (Exception e) {
            log.error("Can't execute cmd: {}", e.getMessage());
        }
    }

    private static void waitForStopCommand(final Server server) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                String cmd = scanner.nextLine();
                if (QUIT_LIST.contains(cmd.toLowerCase())) {
                    server.stop();
                    break;
                } else {
                    log.error("Undefined command: {}!"
                            + " To shutdown server please type: q", cmd);
                }
            }
        }
    }
}
