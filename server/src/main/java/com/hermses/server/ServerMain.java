package com.hermses.server;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5050;
        ChatServer server = new ChatServer(port);
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { server.close(); } catch (Exception ignored) {}
        }));
    }
}
