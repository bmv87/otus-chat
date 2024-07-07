package ru.otus.chat.server;

import ru.otus.chat.server.db.entities.IdentityRepository;
import ru.otus.chat.server.db.entities.RoleEnum;
import ru.otus.chat.server.db.entities.User;

import java.util.UUID;

public class DBAuthenticationProvider implements AuthenticationProvider {


    private final Server server;
    private final IdentityRepository identityRepository;

    public DBAuthenticationProvider(Server server) {
        this.server = server;
        this.identityRepository = new IdentityRepository();
    }

    @Override
    public void initialize() {
        System.out.println("Сервис аутентификации запущен: In-DB режим");
    }

    private String getUsernameByLoginAndPassword(String login, String password) {
        var user = identityRepository.getUsersByLogin(login);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        return user.getUsername();
    }

    private boolean isLoginAlreadyExist(String login) {
        var user = identityRepository.getUsersByLogin(login);
        return user != null;
    }

    private boolean isUsernameAlreadyExist(String username) {
        var user = identityRepository.getUsersByUserName(username);
        return user != null;
    }

    @Override
    public synchronized boolean authenticate(ClientHandler clientHandler, String login, String password) {
        String authUsername = getUsernameByLoginAndPassword(login, password);
        if (authUsername == null) {
            clientHandler.sendMessage("Некорретный логин/пароль");
            return false;
        }
        if (server.isUsernameBusy(authUsername)) {
            clientHandler.sendMessage("Указанная учетная запись уже занята");
            return false;
        }
        clientHandler.setUsername(authUsername);
        server.subscribe(clientHandler);
        clientHandler.sendMessage("/authok " + authUsername);
        return true;
    }

    @Override
    public boolean isAdmin(ClientHandler clientHandler) {
        var uName = clientHandler.getUsername();
        if (uName == null || uName.isBlank()) {
            return false;
        }
        return identityRepository.isInRole(uName, RoleEnum.ADMIN);
    }


    @Override
    public boolean registration(ClientHandler clientHandler, String login, String password, String username, RoleEnum roleEnum) {
        if (login.trim().length() < 3 || password.trim().length() < 6 || username.trim().isEmpty()) {
            clientHandler.sendMessage("Логин 3+ символа, Пароль 6+ символов, Имя пользователя 1+ символ");
            return false;
        }
        if (isLoginAlreadyExist(login)) {
            clientHandler.sendMessage("Указанный логин уже занят");
            return false;
        }
        if (isUsernameAlreadyExist(username)) {
            clientHandler.sendMessage("Указанное имя пользователя уже занято");
            return false;
        }
        var isAdmin = isAdmin(clientHandler);
        if (!isAdmin(clientHandler) && roleEnum == RoleEnum.ADMIN) {
            clientHandler.sendMessage("У вас нет прав регистрировать администратора!");
            return false;
        }
        var roleId = identityRepository.getRoleId(roleEnum);
        identityRepository.saveUser(new User(UUID.randomUUID(), roleId, login, password, username));

        if (isAdmin) {
            clientHandler.sendMessage("Новый администратор зарегистрирован: " + username);
            return true;
        }
        clientHandler.setUsername(username);
        server.subscribe(clientHandler);
        clientHandler.sendMessage("/regok " + username);
        return true;
    }

    public void close() {
        identityRepository.close();
    }

}
