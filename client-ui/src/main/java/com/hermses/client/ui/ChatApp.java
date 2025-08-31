package com.hermses.client.ui;

import com.hermses.client.ChatClient;
import com.hermses.protocol.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ChatApp extends Application {
    private ChatClient client;
    private String username;

    private final TextArea chatArea = new TextArea();
    private final TextField input = new TextField();
    private final TextField userField = new TextField();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Hermses Chat");
        chatArea.setEditable(false);
        userField.setPromptText("Pseudo");
        input.setPromptText("Message...");
        Button connectBtn = new Button("Connect");
        Button sendBtn = new Button("Send");

        connectBtn.setOnAction(e -> connect());
        sendBtn.setOnAction(e -> send());
        input.setOnAction(e -> send());

        HBox top = new HBox(8, userField, connectBtn);
        top.setPadding(new Insets(8));
        HBox bottom = new HBox(8, input, sendBtn);
        bottom.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(chatArea);
        root.setBottom(bottom);
        stage.setScene(new Scene(root, 600, 400));
        stage.show();
    }

    private void connect() {
        if (client != null) return;
        username = userField.getText().isBlank() ? "user" + System.currentTimeMillis()%1000 : userField.getText();
        client = new ChatClient("localhost", 5050);
        try {
            client.connect(username, this::onMessage, raw -> append("RAW: " + raw));
            append("Connecté en tant que " + username);
        } catch (Exception e) {
            append("Erreur: " + e.getMessage());
        }
    }

    private void send() {
        if (client == null) return;
        String text = input.getText();
        if (text.isBlank()) return;
        client.sendChat(username, text);
        input.clear();
    }

    private void onMessage(Message m) {
        append(String.format("[%s] %s: %s", m.getType(), m.getSender(), m.getContent()));
    }

    private void append(String line) {
        Platform.runLater(() -> chatArea.appendText(line + "\n"));
    }

    @Override
    public void stop() throws Exception {
        if (client != null) client.close();
    }

    public static void main(String[] args) { launch(args); }
}
