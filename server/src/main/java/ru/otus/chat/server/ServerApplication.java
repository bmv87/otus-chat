package ru.otus.chat.server;

public class ServerApplication {
    public static void main(String[] args) throws Exception {
        try (var server = new Server(8189)) {
            server.start();
        }
    }
}
