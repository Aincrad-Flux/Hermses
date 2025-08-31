# Hermses

Application de messagerie (prototype) en Java multi-modules : serveur, client CLI et client UI (JavaFX) pour macOS & Linux.

## Modules

- `protocol` : Modèles de messages & sérialisation JSON.
- `server` : Serveur de chat TCP simple (broadcast).
- `client` : Client unifié (CLI + JavaFX UI).

## Prérequis

- JDK 21+ (actuellement JDK 24 installé fonctionne, build en mode `--release 21` pour stabilité).
- Maven 3.9+

## Construire

```
mvn clean install
```

## Lancer le serveur
Après build, un JAR autonome est créé : `server/target/hermses-server.jar`.
```
mvn -pl server -am package
java -jar server/target/hermses-server.jar 5050
```

## Client (CLI & GUI)
Après build, JAR autonome : `client/target/hermses-client.jar` (contient CLI + UI).
```
mvn -pl client -am package
java -jar client/target/hermses-client.jar localhost 5050 pseudo   # CLI
java -cp client/target/hermses-client.jar com.hermses.client.ui.ChatApp    # GUI
```
Ou via scripts:
```
bin/client tui [host] [port] [pseudo]
bin/client gui
```

## Scripts binaires pratiques

Un dossier `bin/` contient des scripts pour lancer rapidement (Linux/macOS) :

```
bin/server [port]
bin/client [host] [port] [pseudo]
```

Si nécessaire : `chmod +x bin/server bin/client`.


## Roadmap (suggestion)
1. Authentification & gestion utilisateurs.
2. Chiffrement (TLS puis end-to-end).
3. Découplage protocole via WebSocket / HTTP2.
4. Packaging natif (.dmg / .AppImage) avec jlink / jpackage.
5. Tests d'intégration réseau.

## Structure
Voir `docs/architecture.md` pour les choix d'architecture initiaux.
