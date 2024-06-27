package ru.otus.chat.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту: " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                subscribe(new ClientHandler(this, socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        broadcastMessage("В чат зашел: " + clientHandler.getUsername());
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Из чата вышел: " + clientHandler.getUsername());
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    // new
    public synchronized void sendMessageTo(String from, String to, String message) {
        var client = clients.stream().filter(c -> c.getUsername().equalsIgnoreCase(to)).findFirst();
        if (client.isPresent()) {
            client.get().sendMessage("От " + to + ": " + message);
            client = clients.stream().filter(c -> c.getUsername().equalsIgnoreCase(from)).findFirst();
            client.ifPresent(clientHandler -> clientHandler.sendMessage("Для " + to + ": " + message));
            return;
        }
        client = clients.stream().filter(c -> c.getUsername().equalsIgnoreCase(from)).findFirst();
        client.ifPresent(clientHandler -> clientHandler.sendMessage("Сообщение не дошло до получателя " + to));
    }
}
