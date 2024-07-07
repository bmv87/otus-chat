package ru.otus.chat.server;

import ru.otus.chat.server.db.entities.RoleEnum;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ClientHandler {
    private final Server server;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                System.out.println("Подключился новый клиент");
                while (true) {
                    String message = in.readUTF();
                    if (message.equals("/exit")) {
                        sendMessage("/exitok");
                        return;
                    }
                    if (message.startsWith("/auth ")) {
                        String[] elements = message.split("\\s+");
                        if (elements.length != 3) {
                            sendMessage("Неверный формат команды /auth");
                            continue;
                        }
                        if (server.getAuthenticationProvider().authenticate(this, elements[1], elements[2])) {
                            break;
                        }
                        continue;
                    }
                    if (message.startsWith("/register ")) {
                        String[] elements = message.split("\\s+");
                        if (elements.length != 4) {
                            sendMessage("Неверный формат команды /register");
                            continue;
                        }
                        if (server.getAuthenticationProvider().registration(this, elements[1], elements[2], elements[3], RoleEnum.USER)) {
                            break;
                        }
                        continue;
                    }
                    sendMessage("Перед работой с чатом необходимо выполнить аутентификацию '/auth login password' или регистрацию '/register login password username'");
                }
                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith("/")) {
                        if (message.equals("/exit")) {
                            sendMessage("/exitok");
                            break;
                        }
                        // new
                        if (message.startsWith("/w ")) {
                            var args = message.split("\\s+");
                            var to = args[1];
                            var text = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
                            server.sendMessageTo(this, to, text);
                            continue;
                        }
                        if (message.startsWith("/register ")) {
                            String[] elements = message.split("\\s+");
                            if (elements.length != 4) {
                                sendMessage("Неверный формат команды /register");
                                continue;
                            }
                            server.getAuthenticationProvider().registration(this, elements[1], elements[2], elements[3], RoleEnum.ADMIN);
                            continue;
                        }
                        if (message.startsWith("/kick ")) {
                            String[] elements = message.split("\\s+");
                            if (!server.getAuthenticationProvider().isAdmin(this)) {
                                sendMessage("У вас нет прав для отключения пользователей!");
                                continue;
                            }
                            if (elements.length != 2) {
                                sendMessage("Неверный формат команды /kick");
                                continue;
                            }
                            server.kickClient(this, elements[1]);
                            continue;
                        }
                    }
                    server.broadcastMessage("От " + username + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
