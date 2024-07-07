package ru.otus.chat.server;

import ru.otus.chat.server.db.entities.RoleEnum;

public interface AuthenticationProvider extends AutoCloseable {
    void initialize();

    boolean authenticate(ClientHandler clientHandler, String login, String password);

    boolean registration(ClientHandler clientHandler, String login, String password, String username, RoleEnum roleEnum);

    boolean isAdmin(ClientHandler clientHandler);
}
