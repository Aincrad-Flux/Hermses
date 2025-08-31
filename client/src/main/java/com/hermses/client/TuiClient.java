package com.hermses.client;

import com.hermses.protocol.Message;
import com.hermses.protocol.MessageType;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Full screen TUI chat using Lanterna.
 * Usage: java -jar hermses-client.jar --tui [host] [port] [username]
 */
public class TuiClient {
    private final String host;
    private final int port;
    private final String username;
    private ChatClient client;
    private Screen screen;
    private MultiWindowTextGUI gui;
    private final BlockingQueue<Runnable> uiQueue = new LinkedBlockingQueue<>();
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault());

    public TuiClient(String host, int port, String username) {
        this.host = host; this.port = port; this.username = username;
    }

    public void run() throws IOException {
        try {
            screen = new DefaultTerminalFactory().createScreen();
            screen.startScreen();
            gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
        } catch (IOException ioe) {
            throw new IOException("Impossible d'initialiser le terminal: " + ioe.getMessage(), ioe);
        }

        Window window = new BasicWindow("Hermses Chat - " + username);
        window.setHints(java.util.List.of(Window.Hint.FULL_SCREEN));

        Panel root = new Panel();
        root.setLayoutManager(new BorderLayout());

        // Chat log
    TextBox chatLog = new TextBox(new TerminalSize(80, 24), TextBox.Style.MULTI_LINE)
        .setReadOnly(true);

        // Input box
        TextBox input = new TextBox().setLayoutData(BorderLayout.Location.BOTTOM);

        // Status bar
        Label status = new Label("⌛ Connexion...");

        root.addComponent(chatLog, BorderLayout.Location.CENTER);
        root.addComponent(input, BorderLayout.Location.BOTTOM);
        root.addComponent(status, BorderLayout.Location.TOP);

        window.setComponent(root);
        gui.addWindow(window);

        // Start connection
        client = new ChatClient(host, port);
        try {
            client.connect(username, m -> enqueue(() -> appendMessage(chatLog, m)), raw -> enqueue(() -> appendSystem(chatLog, raw)));
            enqueue(() -> status.setText("Connecté à " + host + ":" + port));
        } catch (IOException e) {
            appendDirect(chatLog, "[ERREUR] Serveur injoignable: " + e.getMessage());
            status.setText("Echec connexion");
        }

        input.takeFocus();
        input.setCaretPosition(0);
        input.setText("");

        input.setValidationPattern(null);

        // Handle enter key
        input.setInputFilter((interactable, keyStroke) -> {
            if (keyStroke.getKeyType() == KeyType.Enter) {
                String text = input.getText();
                if (!text.isBlank() && client != null) {
                    client.sendChat(username, text);
                }
                input.setText("");
                return false; // consume
            }
            return true;
        });

        // UI loop pump for queued updates
        Thread pump = new Thread(() -> {
            while (window.isVisible()) {
                try {
                    Runnable r = uiQueue.take();
                    r.run();
                } catch (InterruptedException ignored) { break; }
            }
        }, "ui-pump");
        pump.setDaemon(true);
        pump.start();

        // Global key listener for quitting
        gui.getGUIThread().invokeLater(() -> appendDirect(chatLog, "Tape /quit ou Ctrl+C pour quitter."));
        screen.doResizeIfNecessary();

        // Main processing loop
        while (window.isVisible()) {
            KeyStroke ks = screen.pollInput();
            if (ks != null) {
                if (ks.getKeyType() == KeyType.EOF || (ks.getKeyType() == KeyType.Character && ks.getCharacter() != null && ks.getCharacter() == 'c' && ks.isCtrlDown())) {
                    break;
                }
            }
            try { Thread.sleep(25); } catch (InterruptedException ignored) {}
        }

        close();
    }

    private void enqueue(Runnable r) { uiQueue.offer(r); }

    private void appendMessage(TextBox log, Message m) {
        String ts = timeFmt.format(Instant.ofEpochMilli(m.getTimestamp()));
        String line;
        if (m.getType() == MessageType.JOIN) {
            line = ts + " * " + m.getSender() + " a rejoint";
        } else if (m.getType() == MessageType.LEAVE) {
            line = ts + " * " + m.getSender() + " est parti";
        } else {
            line = ts + " " + m.getSender() + ": " + m.getContent();
        }
        appendDirect(log, line);
    }

    private void appendSystem(TextBox log, String raw) { appendDirect(log, "[SYS] " + raw); }

    private void appendDirect(TextBox log, String line) {
        log.setText(log.getText() + (log.getText().isEmpty()? "" : "\n") + line);
        log.setCaretPosition(log.getText().length());
    }

    private void close() {
        try { if (client != null) client.close(); } catch (IOException ignored) {}
        try { if (screen != null) screen.stopScreen(); } catch (IOException ignored) {}
    }

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5050;
        String user = args.length > 2 ? args[2] : "user" + System.currentTimeMillis()%1000;
        new TuiClient(host, port, user).run();
    }
}
