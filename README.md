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
Après build, un JAR autonome est créé : `server/target/hermses-server.jar`.
```
mvn -pl server -am package
java -jar server/target/hermses-server.jar 5050
```

## Client CLI
Après build, JAR autonome : `client/target/hermses-client.jar`.
```
mvn -pl client -am package
java -jar client/target/hermses-client.jar localhost 5050 pseudo
```

## Client UI (JavaFX)
```
mvn -pl client-ui -am javafx:run
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
