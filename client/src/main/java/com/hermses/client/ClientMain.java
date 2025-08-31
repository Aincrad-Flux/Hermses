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
        try {
            client.connect(user, ClientMain::printMessage, s -> System.out.println("SYSTEM RAW: " + s));
        } catch (Exception e) {
            System.err.printf("Impossible de se connecter au serveur %s:%d (%s). Assurez-vous que le serveur est lancé.%n", host, port, e.getMessage());
            return;
        }
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = console.readLine()) != null) {
            if (line.equalsIgnoreCase("/quit")) break;
            client.sendChat(user, line);
        }
        client.close();
    }

    private static void printMessage(Message m) {
        // Afficher seulement l'expéditeur et le contenu comme demandé
        switch (m.getType()) {
            case JOIN -> System.out.printf("* %s a rejoint *%n", m.getSender());
            case LEAVE -> System.out.printf("* %s est parti *%n", m.getSender());
            default -> System.out.printf("%s: %s%n", m.getSender(), m.getContent());
        }
    }
}
