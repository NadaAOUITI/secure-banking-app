# 📊 Rapport Technique - Secure Banking App

## 🏗️ Architecture

### Stack Technologique
- **Backend** : Spring Boot 3.2 + Spring Security 6.x
- **Frontend** : React 18 + JavaScript + Hooks personnalisés
- **Base de données** : PostgreSQL 14+
- **Authentification** : Sessions + Spring Security + OTP Email
- **Chiffrement** : AES-GCM pour données sensibles + BCrypt pour mots de passe
- **Email** : Gmail SMTP avec App Password
- **Architecture** : 3-tiers MVC + Services modulaires

---

## 🗄️ Base de Données

### Table `users`

| Nom du champ | Type | Contraintes | Description |
|--------------|------|-------------|-------------|
| `id` | BIGINT | PK, Auto-incrément | Identifiant unique de l'utilisateur |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Adresse email servant d'identifiant |
| `first_name` | VARCHAR(50) | NOT NULL, 2–50 caractères | Prénom de l'utilisateur |
| `last_name` | VARCHAR(50) | NOT NULL, 2–50 caractères | Nom de l'utilisateur |
| `password` | VARCHAR(255) | NOT NULL | Mot de passe hashé avec BCrypt |
| `enabled` | BOOLEAN | NOT NULL, défaut = TRUE | Statut du compte (activé/désactivé) |
| `account_non_expired` | BOOLEAN | NOT NULL, défaut = TRUE | Indique si le compte n'est pas expiré |
| `account_non_locked` | BOOLEAN | NOT NULL, défaut = TRUE | Indique si le compte est verrouillé |
| `credentials_non_expired` | BOOLEAN | NOT NULL, défaut = TRUE | Indique si les identifiants sont valides |
| `created_at` | TIMESTAMP | NOT NULL | Date de création du compte |
| `last_login_at` | TIMESTAMP | NULLABLE | Date de dernière connexion |
| `country_encrypted` | TEXT | NULLABLE | Pays chiffré AES-GCM |
| `phone_encrypted` | TEXT | NULLABLE | Téléphone chiffré AES-GCM |
| `birth_date_encrypted` | TEXT | NULLABLE | Date de naissance chiffrée AES-GCM |
| `address_encrypted` | TEXT | NULLABLE | Adresse complète chiffrée AES-GCM |
| `document_type_encrypted` | TEXT | NULLABLE | Type de document chiffré AES-GCM |
| `document_number_encrypted` | TEXT | NULLABLE | Numéro de document chiffré AES-GCM |

**Sécurité renforcée** :
- **Chiffrement AES-GCM** : Toutes les données sensibles sont chiffrées
- **Hachage BCrypt** : Mots de passe et codes PIN sécurisés
- **Isolation des données** : Séparation User/BankAccount/BankCard
- **Flags Spring Security** : Contrôle granulaire de l'accès utilisateur

### Table `otp_tokens`

| Nom du champ | Type | Contraintes | Description |
|--------------|------|-------------|-------------|
| `id` | BIGINT | PK, Auto-incrément | Identifiant unique du token |
| `email` | VARCHAR(255) | NOT NULL | Email associé au token |
| `otp_code` | VARCHAR(255) | NOT NULL | Code OTP hashé avec BCrypt |
| `expires_at` | TIMESTAMP | NOT NULL | Date d'expiration (5 min login, 1 min onboarding) |
| `used` | BOOLEAN | NOT NULL, défaut = FALSE | Indique si le token a été utilisé |
| `created_at` | TIMESTAMP | NOT NULL | Date de création du token |

### Table `bank_accounts`

| Nom du champ | Type | Contraintes | Description |
|--------------|------|-------------|-------------|
| `id` | BIGINT | PK, Auto-incrément | Identifiant unique du compte |
| `user_id` | BIGINT | FK vers users, NOT NULL | Propriétaire du compte |
| `account_number` | VARCHAR(20) | UNIQUE, NOT NULL | Numéro de compte (format TN59...) |
| `account_type` | VARCHAR(20) | NOT NULL | Type: CURRENT, SAVINGS, PREMIUM |
| `balance` | DECIMAL(15,3) | NOT NULL, défaut = 0 | Solde du compte en TND |
| `initial_deposit` | DECIMAL(15,3) | NULLABLE | Dépôt initial |
| `is_active` | BOOLEAN | NOT NULL, défaut = TRUE | Statut du compte |
| `created_at` | TIMESTAMP | NOT NULL | Date de création |

### Table `bank_cards`

| Nom du champ | Type | Contraintes | Description |
|--------------|------|-------------|-------------|
| `id` | BIGINT | PK, Auto-incrément | Identifiant unique de la carte |
| `account_id` | BIGINT | FK vers bank_accounts, NOT NULL | Compte associé |
| `card_type` | VARCHAR(20) | NOT NULL | Type: CLASSIC, GOLD, PLATINUM |
| `card_number_encrypted` | TEXT | NOT NULL | Numéro de carte chiffré AES-GCM |
| `expiry_date_encrypted` | TEXT | NOT NULL | Date d'expiration chiffrée AES-GCM |
| `cvv_encrypted` | TEXT | NOT NULL | CVV chiffré AES-GCM |
| `pin_hash` | VARCHAR(255) | NOT NULL | Code PIN hashé avec BCrypt |
| `is_active` | BOOLEAN | NOT NULL, défaut = TRUE | Statut de la carte |
| `created_at` | TIMESTAMP | NOT NULL | Date de création |

---

## 🔐 Sécurité Backend

### 1. Spring Security Configuration

**SecurityConfig.java** : Configuration de la chaîne de filtres (FilterChain)
- Contrôle d'accès basé sur les rôles selon les endpoints
- Protection des API nécessitant une authentification
- Configuration CORS pour le frontend React

### 2. Chiffrement des Mots de Passe

**BCrypt avec coût 12** :
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

**Processus de sécurisation** :
1. **Hachage + Salage** : `"MonMotDePasse123!"` → `"$2a$12$xyz...abc"`
2. **Coût 12** = 4,096 itérations (plus sécurisé que le standard 10)
3. **Protection contre** :
   - Attaques Rainbow Tables
   - Attaques par force brute
   - Vol de base de données

### 3. Validation avec DTO

**Utilisation des Data Transfer Objects** :
- **UserRegistrationDto** : Validation des données d'inscription
- **OtpVerificationDto** : Validation des codes OTP
- **LoginRequestDto** : Validation des données de connexion

**Avantages sécurité** :
- Validation systématique des entrées (`@Email`, `@NotBlank`, `@Size`)
- Protection contre les injections de champs
- Découplage View ↔ Model (principe Zero-Trust)
- Empêche l'exposition de champs sensibles

### 4. Validateur Personnalisé

**@ValidPassword** : Validation de mot de passe fort
- Minimum 12 caractères
- Au moins 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial
- Protection contre les mots de passe faibles

### 5. Protection contre les Injections SQL

- **Requêtes paramétrées** via Spring Data JPA
- **Query binding** automatique
- **Validation DTO** côté serveur
- **Principe de moindre privilège** en base de données

---

## 🔑 Authentification Multi-Facteurs (MFA)

### 1. Système OTP (One-Time Password)

**Génération sécurisée** :
```java
SecureRandom secureRandom = new SecureRandom();
String otpCode = String.format("%06d", secureRandom.nextInt(1000000));
```

**Pourquoi SecureRandom ?**
- `Random` classique = prévisible si on connaît la seed
- `SecureRandom` = imprévisible, utilise l'entropie système
- Empêche la prédiction des codes OTP

### 2. Sécurité Temporelle

**Expiration 5 minutes** :
- Limite la fenêtre d'attaque
- Empêche l'utilisation de codes interceptés
- Protection contre les attaques replay

### 3. Usage Unique

**Flag `used`** :
- Un OTP ne peut être utilisé qu'une seule fois
- Empêche la réutilisation malicieuse
- Maintient l'intégrité des opérations

---

## 📧 Service Email Sécurisé

### Configuration Gmail App Password

**Avantages sécuritaires** :
- Pas d'exposition du mot de passe principal Gmail
- Accès limité au protocole SMTP uniquement
- Révocation facile en un clic
- Chiffrement TLS/SSL des communications

**Configuration** :
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Gestion des Secrets

**Fichier séparé** : `application-secret.properties`
- Exclusion du contrôle de version (`.gitignore`)
- Import conditionnel : `spring.config.import=optional:application-secret.properties`
- Protection des credentials sensibles

---

## 🛡️ Mesures de Sécurité Implémentées

### 1. Contrôle d'Accès (CIA - Confidentialité)
- Authentification forte (email + mot de passe + OTP)
- Chiffrement des mots de passe (BCrypt)
- Sessions sécurisées avec Spring Security

### 2. Intégrité des Données (CIA - Intégrité)
- Validation stricte des entrées utilisateur
- Protection contre les injections SQL
- Codes OTP à usage unique
- Horodatage des opérations

### 3. Disponibilité (CIA - Disponibilité)
- Gestion des comptes (verrouillage, expiration)
- Fallback console pour les OTP en cas d'échec email
- Timeouts configurés pour les connexions

### 4. Traçabilité et Audit
- Journalisation des tentatives de connexion
- Horodatage de création des comptes (`createdAt`)
- Suivi des dernières connexions (`lastLoginAt`)
- Logs de sécurité configurés



---

## 🏦 Fonctionnalités Bancaires Sécurisées

### 1. Onboarding Complet (6 Étapes)
- **Étape 1** : Informations personnelles avec validation
- **Étape 2** : Vérification email avec OTP (1 min)
- **Étape 3** : Vérification identité et adresse
- **Étape 4** : Configuration mot de passe sécurisé
- **Étape 5** : Sélection produits + création codes PIN
- **Étape 6** : Confirmation et création compte

### 2. Gestion de Compte Sécurisée
- **Consultation** : Détails de compte avec masquage des données
- **Chiffrement** : Toutes les données sensibles chiffrées AES-GCM
- **Sessions** : Authentification persistante avec timeout
- **API modulaire** : Services, hooks et utilitaires réutilisables

### 3. Produits Bancaires
- **Comptes** : CURRENT, SAVINGS, PREMIUM avec soldes réels
- **Cartes** : CLASSIC, GOLD, PLATINUM avec codes PIN sécurisés
- **Génération** : Numéros de compte TN59, cartes, CVV automatiques
- **Chiffrement** : Toutes les données de carte chiffrées

## 🎨 Architecture Frontend Modulaire

### Services Layer
- **accountService.js** : API calls avec gestion d'authentification
- **authService.js** : Authentification et gestion des sessions
- **Centralisation** : Tous les appels API dans des services dédiés

### Custom Hooks
- **useAccountDetails.js** : Hook réutilisable pour les données de compte
- **Gestion d'état** : Loading, error, data, refetch
- **Séparation des préoccupations** : Logique métier séparée de l'UI

### Utilities
- **formatters.js** : Formatage et masquage des données sensibles
- **validationUtils.js** : Validation et sanitisation côté client
- **Réutilisabilité** : Fonctions utilitaires partagées

### Composants Sécurisés
- **AccountSummary** : Affichage sécurisé des détails de compte
- **Onboarding Flow** : Processus d'inscription en 6 étapes
- **Validation temps réel** : Contrôles de sécurité immédiats

## 📚 Références Sécurité

- **OWASP Top 10** : Protection contre les vulnérabilités web courantes
- **Spring Security** : Framework de sécurité enterprise-grade
- **AES-GCM** : Chiffrement symétrique avec authentification intégrée
- **BCrypt** : Algorithme de hachage adaptatif sécurisé
- **TLS/SSL** : Chiffrement des communications
- **MFA/2FA** : Authentification multi-facteurs
- **React Security** : Bonnes pratiques de sécurité frontend