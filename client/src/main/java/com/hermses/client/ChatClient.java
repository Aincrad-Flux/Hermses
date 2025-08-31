package com.hermses.client;

import com.hermses.protocol.JsonMessageSerializer;
import com.hermses.protocol.Message;
import com.hermses.protocol.MessageSerializer;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ChatClient implements Closeable {
    private final String host;
    private final int port;
    private final MessageSerializer serializer = new JsonMessageSerializer();
    private Socket socket;
    private PrintWriter out;
    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    public ChatClient(String host, int port) {
        this.host = host; this.port = port;
    }

    public void connect(String username, Consumer<Message> onMessage, Consumer<String> onRawSystem) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        pool.submit(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    try {
                        Message msg = serializer.deserialize(line);
                        onMessage.accept(msg);
                    } catch (Exception e) {
                        onRawSystem.accept(line);
                    }
                }
            } catch (IOException ignored) {} finally { try { close(); } catch (IOException ignored2) {} }
        });
        // first line expected is prompt, send username after reading one line
        try { if (in.readLine() != null) out.println(username); } catch (IOException ignored) {}
    }

    public void sendChat(String user, String text) {
        try {
            out.println(serializer.serialize(Message.chat(user, text)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        if (socket != null) socket.close();
        pool.shutdownNow();
    }
}
