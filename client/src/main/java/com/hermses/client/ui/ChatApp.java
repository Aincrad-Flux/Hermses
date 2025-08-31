// Fusion de l'UI JavaFX dans le module client
package com.hermses.client.ui;

import com.hermses.client.ChatClient;
import com.hermses.protocol.Message;
import com.hermses.protocol.MessageType;
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
    private final TextArea usersArea = new TextArea();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Hermses - Messagerie Sécurisée");
        showLogin(stage);
    }

    private void showLogin(Stage stage) {
        userField.setPromptText("Entrez votre pseudo");
        Button connectBtn = new Button("Se connecter");
        connectBtn.setOnAction(e -> connectAndShowChat(stage));
        userField.setOnAction(e -> connectAndShowChat(stage));
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(30));
        pane.setCenter(new HBox(10, userField, connectBtn));
        stage.setScene(new Scene(pane, 500, 160));
        stage.show();
    }

    private void connectAndShowChat(Stage stage) {
        if (client != null) return;
        username = userField.getText().isBlank() ? "user" + System.currentTimeMillis()%1000 : userField.getText();
        client = new ChatClient("localhost", 5050);
        usersArea.setEditable(false);
        chatArea.setEditable(false);
        input.setPromptText("Tapez votre message et Entrée...");
        try {
            client.connect(username, this::onMessage, raw -> append("[SYS] " + raw));
            append("Connecté en tant que " + username);
        } catch (Exception e) {
            append("Erreur: " + e.getMessage());
        }
        Button sendBtn = new Button("Envoyer");
        sendBtn.setOnAction(e -> send());
        input.setOnAction(e -> send());

        usersArea.setPrefWidth(120);
        usersArea.setWrapText(true);

        BorderPane chatLayout = new BorderPane();
        BorderPane right = new BorderPane();
        right.setCenter(chatArea);
        right.setBottom(new HBox(8, input, sendBtn));
        right.setPadding(new Insets(8));
        BorderPane left = new BorderPane();
        left.setPadding(new Insets(8));
        left.setTop(new javafx.scene.control.Label("Contacts"));
        left.setCenter(usersArea);
        chatLayout.setLeft(left);
        chatLayout.setCenter(right);
        chatLayout.setTop(new HBox(8, new javafx.scene.control.Label("Hermses - " + username)));
        stage.setScene(new Scene(chatLayout, 800, 500));
    }

    private void send() {
        if (client == null) return;
        String text = input.getText();
        if (text.isBlank()) return;
        client.sendChat(username, text);
    // Echo local car le serveur ne renvoie pas le message de l'émetteur
    append(username + ": " + text);
        input.clear();
    }

    private void onMessage(Message m) {
        if (m.getType() == MessageType.USERS) {
            Platform.runLater(() -> usersArea.setText(m.getContent().replace(",", "\n")));
            return;
        }
        switch (m.getType()) {
            case JOIN -> append("* " + m.getSender() + " a rejoint");
            case LEAVE -> append("* " + m.getSender() + " est parti");
            default -> append(m.getSender() + ": " + m.getContent());
        }
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
