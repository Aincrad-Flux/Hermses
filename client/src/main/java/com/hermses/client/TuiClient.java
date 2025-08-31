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
    private String username; // défini après login si null
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
            gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLACK));
        } catch (IOException ioe) {
            throw new IOException("Impossible d'initialiser le terminal: " + ioe.getMessage(), ioe);
        }

        Window window = new BasicWindow("Hermses");
        window.setHints(java.util.List.of(Window.Hint.FULL_SCREEN));
        // If username absent -> login screen
        if (username == null || username.isBlank()) {
            Panel login = new Panel(new LinearLayout(Direction.VERTICAL));
            Label title = new Label("Hermes - Messagerie Sécurisée").addStyle(SGR.BOLD);
            TextBox pseudo = new TextBox(new TerminalSize(20,1));
            pseudo.setText("");
            login.addComponent(title);
            login.addComponent(new Label("Entrez votre pseudo puis Entrée:"));
            login.addComponent(pseudo);
            window.setComponent(login);
            gui.addWindow(window);

            pseudo.setInputFilter((i,k)->{
                if (k.getKeyType()==KeyType.Enter) {
                    String entered = pseudo.getText().trim();
                    if (!entered.isEmpty()) {
                        this.username = entered;
                        buildChatUI(window);
                    }
                    return false;
                }
                return true;
            });
        } else {
            buildChatUI(window);
            gui.addWindow(window);
        }

        Thread pump = new Thread(() -> {
            while (window.isVisible()) {
                try { uiQueue.take().run(); } catch (InterruptedException ignored) { break; }
            }
        }, "ui-pump");
        pump.setDaemon(true);
        pump.start();

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

    private void updateUsers(TextBox contacts, String csv) {
        if (csv == null || csv.isBlank()) {
            contacts.setText("(aucun)");
            return;
        }
        contacts.setText(String.join("\n", csv.split(",")) + "\n");
    }

    private void buildChatUI(Window window) {
        Panel root = new Panel(new BorderLayout());
        // Left contacts
        TextBox contacts = new TextBox(new TerminalSize(18, 10), TextBox.Style.MULTI_LINE).setReadOnly(true);
        contacts.setText("(aucun)\n");
        Panel contactsPanel = new Panel(new BorderLayout());
        contactsPanel.addComponent(new Label("Contacts"), BorderLayout.Location.TOP);
        contactsPanel.addComponent(contacts, BorderLayout.Location.CENTER);
        // Chat area
        TextBox chatLog = new TextBox(new TerminalSize(1,1)).setReadOnly(true);
        Panel chatPanel = new Panel(new BorderLayout());
        chatPanel.addComponent(new Label("Messages"), BorderLayout.Location.TOP);
        chatPanel.addComponent(chatLog, BorderLayout.Location.CENTER);
        Panel center = new Panel(new LinearLayout(Direction.HORIZONTAL));
        contactsPanel.setPreferredSize(new TerminalSize(20,1));
        center.addComponent(contactsPanel);
        center.addComponent(chatPanel);
        TextBox input = new TextBox("");
        Panel inputPanel = new Panel(new BorderLayout());
        inputPanel.addComponent(new Label("> Saisir message :"), BorderLayout.Location.TOP);
        inputPanel.addComponent(input, BorderLayout.Location.CENTER);
        Label status = new Label("⌛ Connexion...");
        root.addComponent(status, BorderLayout.Location.TOP);
        root.addComponent(center, BorderLayout.Location.CENTER);
        root.addComponent(inputPanel, BorderLayout.Location.BOTTOM);
        window.setComponent(root);

        client = new ChatClient(host, port);
        try {
            client.connect(username, m -> enqueue(() -> {
                if (m.getType() == MessageType.USERS) {
                    updateUsers(contacts, m.getContent());
                } else {
                    appendMessage(chatLog, m);
                }
            }), raw -> enqueue(() -> appendSystem(chatLog, raw)));
            enqueue(() -> status.setText("Connecté en tant que " + username + " @" + host + ":" + port));
        } catch (IOException e) {
            appendDirect(chatLog, "[ERREUR] Serveur injoignable: " + e.getMessage());
            status.setText("Echec connexion");
        }
        input.setInputFilter((interactable, keyStroke) -> {
            if (keyStroke.getKeyType() == KeyType.Enter) {
                String text = input.getText();
                if (!text.isBlank() && client != null) {
                    client.sendChat(username, text);
                    // Echo local (le serveur ne reboucle pas le message à l'expéditeur)
                    appendMessage(chatLog, Message.chat(username, text));
                }
                input.setText("");
                return false;
            }
            return true;
        });
        gui.getGUIThread().invokeLater(() -> appendDirect(chatLog, "Tape /quit ou Ctrl+C pour quitter."));
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && ("--help".equalsIgnoreCase(args[0]) || "-h".equalsIgnoreCase(args[0]))) {
            System.out.println("Usage: bin/client --tui [host] [port] [username]\n" +
                    "Si username omis: un écran de login est affiché.");
            return;
        }
        String host = "localhost";
        int port = 5050;
        String user = null; // déclenche écran login si null
        if (args.length > 0) host = args[0];
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                // L'utilisateur a probablement fourni directement un username en 2ᵉ position
                user = args[1];
            }
        }
        if (args.length > 2) {
            user = args[2];
        }
        new TuiClient(host, port, user).run();
    }
}
