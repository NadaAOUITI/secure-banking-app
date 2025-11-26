# 🏦 Secure Banking Application

> Application bancaire sécurisée développée dans le cadre du cours de Sécurité Informatique

[![Security](https://img.shields.io/badge/Security-OWASP%20Top%2010-green)](https://owasp.org/www-project-top-ten/)
[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot%203.2-brightgreen)](https://spring.io/projects/spring-boot)
[![Frontend](https://img.shields.io/badge/Frontend-React%2018-blue)](https://reactjs.org/)
[![Database](https://img.shields.io/badge/Database-PostgreSQL-blue)](https://www.postgresql.org/)

## 🎯 Objectif du Projet

Développement d'une application bancaire Web sécurisée avec focus sur :
- **Authentification forte** et gestion de sessions
- **Chiffrement des communications** TLS/SSL
- **Protection contre les attaques** courantes (OWASP Top 10)
- **Validation robuste** des données utilisateur

## 🏗️ Architecture

### Stack Technologique
- **Backend**: Spring Boot 3.x + Spring Security 6.x
- **Frontend**: React 18 + TypeScript
- **Base de données**: PostgreSQL
- **Authentification**: JWT + Spring Security
- **Communication**: HTTPS/TLS 1.3
- **Tests**: JUnit 5, React Testing Library

### Structure du Projet

```
secure-banking-app/
├── backend/                    # API REST Spring Boot
├── frontend/                   # Application React
├── docs/                      # Documentation technique
└── README.md
```

## 🚀 Phase 1: User Authentication & Session Management

### ✅ Fonctionnalités Implémentées
- [x] Architecture du projet
- [ ] Inscription avec validation de mot de passe fort
- [ ] Connexion sécurisée avec JWT
- [ ] Protection contre brute force
- [ ] Gestion des sessions sécurisées

### 🔐 Sécurité
- **Rate Limiting**: Protection contre les attaques par force brute
- **Password Policy**: Mots de passe forts (12+ caractères)
- **JWT Security**: Tokens avec expiration courte + refresh
- **Input Validation**: Sanitisation côté client et serveur
- **HTTPS Only**: Communications chiffrées obligatoires

## 📚 Documentation

- [Architecture Technique](docs/ARCHITECTURE.md)
- [Exigences de Sécurité](docs/SECURITY-REQUIREMENTS.md)
- [Plan de Développement](docs/DEVELOPMENT-PLAN.md)

## 🛠️ Installation

### Prérequis
- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+

### Configuration Base de Données

1. **Installer PostgreSQL 14+**
2. **Créer la base de données :**
```sql
CREATE DATABASE secure_banking_db;
```
3. **Configurer les paramètres (voir section Backend ci-dessous)**

### Backend

1. **Configurer les propriétés de l'application :**
```bash
cd backend/src/main/resources
cp application.properties.example application.properties
```

2. **Éditer `application.properties` avec vos paramètres :**
   - Remplacer `YOUR_DATABASE_PASSWORD_HERE` par votre mot de passe PostgreSQL
   - Remplacer `YOUR_ADMIN_PASSWORD_HERE` par un mot de passe admin sécurisé

3. **Lancer l'application :**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Application accessible sur :** http://localhost:8080

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## 🧪 Tests

```bash
# Backend
cd backend && mvn test

# Frontend
cd frontend && npm test
```

## 📄 Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👥 Auteur

Développé dans le cadre du cours de Sécurité Informatique - ING2