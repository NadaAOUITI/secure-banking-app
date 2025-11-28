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
- **Frontend**: React 18 + JavaScript
- **Base de données**: PostgreSQL
- **Authentification**: sessions + Spring Security + OTP Email
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

## 📋 État d'Avancement des Tâches

### ✅ **Tâches Complétées**

#### 1. User Authentication & Session Management
- ✅ **Implement user registration with strong password validation**
- ✅ **Implement login with multi-factor authentication (OTP via email)**
- ✅ **Securely store passwords (hashing with salt, e.g., bcrypt)**

#### 2. Database Setup
- ✅ **PostgreSQL database configuration and connection**

### 🔄 **Tâches En Cours**

#### 1. User Authentication & Session Management
- [ ] **Implement session management (timeout after inactivity)**
- [ ] **Log failed login attempts and lock account after repeated failures**

#### 2. Security & Encryption
- [ ] **Implement TLS/SSL encryption for all client-server communication**
- [ ] **Use a secure backend framework (Spring Boot + Spring Security)**
- [ ] **Implement input validation and sanitization to prevent injection attacks**

### 📋 **Tâches Restantes**

#### 2. Account Management
- [ ] View account details (balance, account number, personal info)
- [ ] Update personal info (phone number, email)
- [ ] Change password securely
- [ ] Ensure data validation to prevent injection attacks

#### 3. Beneficiary Management
- [ ] Add a beneficiary (same bank, national bank, international bank)
- [ ] Edit or remove beneficiary details
- [ ] Validate beneficiary account numbers before adding

#### 4. Fund Transfers
- [ ] Implement transfer to a beneficiary with OTP confirmation
- [ ] Implement transfer history logging
- [ ] Send confirmation email for every transfer
- [ ] Validate input to prevent SQL/command injection

#### 5. Transaction History
- [ ] Retrieve transaction history from a secure database
- [ ] Filter transactions by date, amount, or beneficiary
- [ ] Display securely on the app interface without exposing sensitive info

#### 6. Security Testing
- [ ] Perform penetration test for SQL injection / XSS
- [ ] Authentication bypass attempt
- [ ] Session hijacking simulation
- [ ] Document results and mitigation measures

#### 7. Optional / Bonus Features
- [ ] Admin panel to monitor user activity and security alerts
- [ ] Additional input validation, e.g., regex for account numbers
- [ ] Implement logging for all critical operations for auditing

### 🎯 **Prochaines Étapes Recommandées**

1. **Session Management** : Implémentation des timeouts et gestion des sessions
2. **Account Locking** : Verrouillage après tentatives de connexion échouées
3. **Account Management** : Interface de gestion des informations utilisateur
4. **Beneficiary System** : Gestion des bénéficiaires pour les transferts
5. **Transfer System** : Implémentation des virements sécurisés
6. **Security Testing** : Tests de pénétration et validation sécuritaire

## 🔐 Sécurité Implémentée

- **MFA/2FA**: Authentification à deux facteurs avec OTP email
- **Password Policy**: Mots de passe forts (12+ caractères, complexité)
- **BCrypt Hashing**: Chiffrement des mots de passe (coût 12)
- **Input Validation**: Sanitisation côté client et serveur
- **TLS/SSL**: Communications chiffrées obligatoires
- **DTO Protection**: Isolation des données sensibles

## 📚 Documentation

- [📧 Guide Configuration Email](docs/EMAIL-SETUP-GUIDE.md) - **OBLIGATOIRE pour l'équipe**
- [📊 Rapport Technique Complet](docs/TECHNICAL-REPORT.md)
- [🏗️ Architecture Technique](docs/ARCHITECTURE.md)
- [🔒 Exigences de Sécurité](docs/SECURITY-REQUIREMENTS.md)

## 🛠️ Installation

### Prérequis
- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+

### 🗄️ Configuration Base de Données PostgreSQL

#### Étape 1: Installation PostgreSQL
1. **Télécharger PostgreSQL 14+** : https://www.postgresql.org/download/
2. **Installer avec les paramètres par défaut**
3. **Noter le mot de passe du superutilisateur `postgres`**

#### Étape 2: Installation pgAdmin (Interface graphique)
1. **Télécharger pgAdmin** : https://www.pgadmin.org/download/
2. **Installer et lancer pgAdmin**
3. **Se connecter avec** :
   - Host: `localhost`
   - Port: `5432`
   - Username: `postgres`
   - Password: `[votre mot de passe]`

#### Étape 3: Créer la Base de Données
**Via pgAdmin :**
1. Clic droit sur "Databases" → "Create" → "Database"
2. Nom: `secure_banking_db`
3. Cliquer "Save"

**Ou via SQL :**
```sql
CREATE DATABASE secure_banking_db;
```

#### Étape 4: Configuration application.properties
```properties
# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/secure_banking_db
spring.datasource.username=postgres
spring.datasource.password=VOTRE_MOT_DE_PASSE_POSTGRES
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

#### ✅ Vérification de la Connexion
1. **Démarrer l'application** : `mvn spring-boot:run`
2. **Vérifier les logs** : Rechercher "HikariPool-1 - Start completed"
3. **Dans pgAdmin** : Actualiser → voir les tables `user` et `otp_token` créées automatiquement

### Backend

1. **Configurer l'email (OBLIGATOIRE)** :
   - Suivre le [Guide Configuration Email](docs/EMAIL-SETUP-GUIDE.md)
   - Créer `application-secret.properties` avec vos credentials Gmail

2. **Lancer l'application :**
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

**Interface accessible sur :** http://localhost:3000

## 🧪 Tests

```bash
# Backend
cd backend && mvn test

# Frontend
cd frontend && npm test
```

## 🔒 Sécurité pour l'Équipe

### ⚠️ IMPORTANT - Configuration Email
Chaque développeur DOIT :
1. Lire le [Guide Configuration Email](docs/EMAIL-SETUP-GUIDE.md)
2. Créer son propre `application-secret.properties`
3. Ne JAMAIS commiter ce fichier sur Git
4. Utiliser son propre Gmail App Password

### 🛡️ Bonnes Pratiques
- Toujours valider les entrées utilisateur
- Utiliser HTTPS en production
- Respecter les principes OWASP Top 10
- Documenter les mesures de sécurité implémentées

## 📄 Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👥 Équipe

Développé dans le cadre du cours de Sécurité Informatique - ING2

---

## 🆘 Support

- **Configuration Email** : Voir [EMAIL-SETUP-GUIDE.md](docs/EMAIL-SETUP-GUIDE.md)
- **Problèmes techniques** : Consulter [TECHNICAL-REPORT.md](docs/TECHNICAL-REPORT.md)
- **Architecture** : Voir la documentation dans `/docs`
