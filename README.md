# Portail des applications — Auchan Sénégal

Portail interne Angular + Spring Boot pour que les collaborateurs retrouvent toutes les applications métier au même endroit : SIRH, caisse, supply, finance, DSI, etc.

## Démarrer le projet

Deux terminaux sont nécessaires.

### 1. API Spring Boot

```powershell
cd C:\Users\ElHadjiAliouneGueye\Projects\auchan-portail\backend
.\mvnw.cmd spring-boot:run
```

API disponible sur [http://localhost:8081](http://localhost:8081).

### 2. Interface Angular

```powershell
cd C:\Users\ElHadjiAliouneGueye\Projects\auchan-portail\frontend
npm start
```

Interface disponible sur [http://localhost:4200](http://localhost:4200). Les appels `/api` sont proxifiés vers le backend.

## Comptes de démonstration

| Profil | Email | Mot de passe | Accès |
| --- | --- | --- | --- |
| Administrateur DSI | `admin@auchan.sn` | `Auchan@2026` | Tout le catalogue + administration |
| Responsable magasin | `responsable@auchan.sn` | `Auchan@2026` | Catalogue complet + gestion des accès de son équipe |
| Collaborateur magasin | `collaborateur@auchan.sn` | `Auchan@2026` | Catalogue complet |
| Stagiaire magasin (compte Bird de démo) | `stagiaire@auchan.sn` | `Auchan@2026` | Uniquement les applications autorisées par son responsable |
| RH | `rh@auchan.sn` | `Auchan@2026` | Catalogue complet |

## Fonctionnalités

- Connexion JWT pour les utilisateurs internes (en production : authentification Bird)
- Catalogue d’applications par métier (RH, Finance, Supply, Magasin, Commercial, IT)
- Recherche, filtres et favoris
- Ouverture de chaque application dans un nouvel onglet
- Rôles : administrateur, responsable, collaborateur
- Accès applicatifs : un responsable ne crée pas de compte (Bird s’en charge) ; il limite les sites visibles pour un collaborateur déjà présent
- Administration DSI : catalogue d’applications et paramétrage des accès

## Structure

```
auchan-portail/
  backend/     Spring Boot 4, JPA, Security, H2
  frontend/    Angular 22
```

La base H2 est en mémoire pour le démarrage rapide. Pour la production, brancher PostgreSQL dans `application.properties` et remplacer les mots de passe / secret JWT.
