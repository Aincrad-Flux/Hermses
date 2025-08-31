package com.hermses.protocol;

public enum MessageType {
    JOIN,
    CHAT,
    LEAVE,
    SYSTEM,
    USERS // liste initiale des utilisateurs connectés (contenu: noms séparés par des virgules)
}
