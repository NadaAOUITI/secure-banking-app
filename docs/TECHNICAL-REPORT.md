# 📊 Rapport Technique - Secure Banking App

## 🏗️ Architecture

### Stack Technologique
- **Backend** : Spring Boot 3.2 + Spring Security 6.x
- **Frontend** : React 18 + JavaScript
- **Base de données** : PostgreSQL 14+
- **Authentification** : JWT + Spring Security + OTP
- **Email** : Gmail SMTP avec App Password
- **Architecture** : 3-tiers MVC (Model-View-Controller)

---

## 🗄️ Base de Données

### Table `User`

| Nom du champ | Type | Contraintes | Description |
|--------------|------|-------------|-------------|
| `id` | BIGINT | PK, Auto-incrément | Identifiant unique de l'utilisateur |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Adresse email servant d'identifiant |
| `firstName` | VARCHAR(50) | NOT NULL, 2–50 caractères | Prénom de l'utilisateur |
| `lastName` | VARCHAR(50) | NOT NULL, 2–50 caractères | Nom de l'utilisateur |
| `password` | VARCHAR(255) | NOT NULL | Mot de passe hashé avec BCrypt |
| `enabled` | BOOLEAN | NOT NULL, défaut = TRUE | Statut du compte (activé/désactivé) |
| `accountNonExpired` | BOOLEAN | NOT NULL, défaut = TRUE | Indique si le compte n'est pas expiré |
| `accountNonLocked` | BOOLEAN | NOT NULL, défaut = TRUE | Indique si le compte est verrouillé |
| `credentialsNonExpired` | BOOLEAN | NOT NULL, défaut = TRUE | Indique si les identifiants sont valides |
| `createdAt` | TIMESTAMP | NOT NULL | Date de création du compte |
| `lastLoginAt` | TIMESTAMP | NULLABLE | Date de dernière connexion |

**Nouveauté** : Utilisation des flags Spring Security pour contrôler l'accès utilisateur.

### Table `OtpToken`

| Nom du champ | Type | Contraintes | Description |
|--------------|------|-------------|-------------|
| `id` | BIGINT | PK, Auto-incrément | Identifiant unique du token |
| `email` | VARCHAR(255) | NOT NULL | Email associé au token |
| `otpCode` | VARCHAR(6) | NOT NULL | Code OTP à 6 chiffres |
| `expiresAt` | TIMESTAMP | NOT NULL | Date d'expiration (5 minutes) |
| `used` | BOOLEAN | NOT NULL, défaut = FALSE | Indique si le token a été utilisé |
| `createdAt` | TIMESTAMP | NOT NULL | Date de création du token |

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

## 📚 Références Sécurité

- **OWASP Top 10** : Protection contre les vulnérabilités web courantes
- **Spring Security** : Framework de sécurité enterprise-grade
- **BCrypt** : Algorithme de hachage adaptatif sécurisé
- **TLS/SSL** : Chiffrement des communications
- **MFA/2FA** : Authentification multi-facteurs