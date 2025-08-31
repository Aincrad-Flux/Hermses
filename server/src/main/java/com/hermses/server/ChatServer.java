package com.hermses.server;

import com.hermses.protocol.JsonMessageSerializer;
import com.hermses.protocol.Message;
import com.hermses.protocol.MessageSerializer;
import com.hermses.protocol.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer implements AutoCloseable {
    private final int port;
    private final MessageSerializer serializer = new JsonMessageSerializer();
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        System.out.println("[SERVER] Listening on port " + port);
        pool.submit(() -> {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(socket);
                    clients.add(handler);
                    pool.submit(handler);
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void broadcast(Message message, ClientHandler exclude) {
        String payload;
        try {
            payload = serializer.serialize(message);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        for (ClientHandler client : clients) {
            if (client != exclude) {
                client.send(payload);
            }
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        for (ClientHandler c : clients) {
            c.close();
        }
        if (serverSocket != null) serverSocket.close();
        pool.shutdownNow();
    }

    private class ClientHandler implements Runnable, AutoCloseable {
        private final Socket socket;
        private PrintWriter out;
        private String name = "?";

        ClientHandler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                this.out = writer;
                sendSystem("Bienvenue ! Entre ton pseudo:");
                this.name = in.readLine();
                broadcast(new Message(MessageType.JOIN, name, name + " a rejoint", Instant.now().toEpochMilli()), this);
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("/quit")) break;
                    Message msg = Message.chat(name, line);
                    broadcast(msg, this);
                }
            } catch (IOException e) {
                // ignore
            } finally {
                clients.remove(this);
                broadcast(new Message(MessageType.LEAVE, name, name + " est parti", Instant.now().toEpochMilli()), this);
                try { close(); } catch (IOException ignored) {}
            }
        }

        void send(String payload) { if (out != null) out.println(payload); }
        void sendSystem(String text) { send("{\"type\":\"SYSTEM\",\"sender\":\"server\",\"content\":\"" + text + "\"}"); }

        @Override
        public void close() throws IOException { socket.close(); }
    }
}
