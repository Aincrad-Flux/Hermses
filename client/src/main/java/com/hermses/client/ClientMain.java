package com.hermses.client;

import com.hermses.protocol.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5050;
        String user = args.length > 2 ? args[2] : "user" + System.currentTimeMillis()%1000;
        ChatClient client = new ChatClient(host, port);
        client.connect(user, ClientMain::printMessage, s -> System.out.println("SYSTEM RAW: " + s));
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = console.readLine()) != null) {
            if (line.equalsIgnoreCase("/quit")) break;
            client.sendChat(user, line);
        }
        client.close();
    }

    private static void printMessage(Message m) {
        System.out.printf("[%s] %s: %s%n", m.getType(), m.getSender(), m.getContent());
    }
}
