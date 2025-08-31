# Hermses

Application de messagerie (prototype) en Java multi-modules : serveur, client CLI et client UI (JavaFX) pour macOS & Linux.

## Modules

- `protocol` : Modèles de messages & sérialisation JSON.
- `server` : Serveur de chat TCP simple (broadcast).
- `client` : Client CLI minimal.
- `client-ui` : Interface JavaFX (desktop) s'appuyant sur le client.

## Prérequis

- JDK 21+ (actuellement JDK 24 installé fonctionne, build en mode `--release 21` pour stabilité).
- Maven 3.9+

## Construire

```
mvn clean install
```

## Lancer le serveur
```
mvn -pl server -am exec:java -Dexec.mainClass="com.hermses.server.ServerMain"
```
Ou exécuter le jar :
```
java -cp server/target/server-0.1.0-SNAPSHOT.jar:protocol/target/protocol-0.1.0-SNAPSHOT.jar com.hermses.server.ServerMain
```

## Client CLI
```
mvn -q -pl client -am exec:java -Dexec.mainClass="com.hermses.client.ClientMain"
```

## Client UI (JavaFX)
```
mvn -pl client-ui -am javafx:run
```

## Roadmap (suggestion)
1. Authentification & gestion utilisateurs.
2. Chiffrement (TLS puis end-to-end).
3. Découplage protocole via WebSocket / HTTP2.
4. Packaging natif (.dmg / .AppImage) avec jlink / jpackage.
5. Tests d'intégration réseau.

## Structure
Voir `docs/architecture.md` pour les choix d'architecture initiaux.
