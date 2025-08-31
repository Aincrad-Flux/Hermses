# Architecture initiale Hermses

Objectif : Base propre et extensible pour une application de messagerie temps réel.

## Découpage
- `protocol` : DTO et sérialisation (actuellement JSON / Jackson). Peut évoluer vers un schéma (JSON Schema / Protobuf) plus tard.
- `server` : Gestion des connexions TCP, broadcast simple. À remplacer ensuite par un event loop NIO / Netty / WebSocket.
- `client` : API client + CLI + UI JavaFX (fusion).

## Flux réseau (prototype)
1. Client se connecte (Socket TCP) -> serveur.
2. Serveur demande pseudo (ligne texte).
3. Client envoie pseudo.
4. Messages ensuite échangés sous forme de lignes JSON.

## Evolutions prévues
- Passage à un framing binaire (longueur + payload) pour robustesse.
- Ajout d'un champ `id` et `room` dans `Message`.
- Ping/pong keep-alive & détection déconnexions.
- Sécurité : TLS (SSLServerSocket ou Netty + certificat autosigné), puis chiffrement E2E.
- Persistance : stockage messages (PostgreSQL / SQLite) + historique.
- Tests d'intégration automatisés (JUnit + sockets).

## Principes
- Séparation claire : protocole indépendant des transports.
- Simplicité initiale pour itération rapide.
- Code prêt à être remplacé par une implémentation plus performante.

## TODO techniques rapides
- Logger (SLF4J + Logback) au lieu de System.out.
- Validation d'input & limite taille messages.
- Gestion erreurs côté client (reconnexions).
