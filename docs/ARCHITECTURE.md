# Architecture Technique - Application Bancaire Sécurisée

## Vue d'ensemble

### Architecture 3-tiers
```
┌─────────────────┐    HTTPS/TLS    ┌─────────────────┐    JPA/Hibernate    ┌─────────────────┐
│   Frontend      │ ◄──────────────► │    Backend      │ ◄──────────────────► │   Database      │
│   React + TS    │                  │  Spring Boot    │                      │  PostgreSQL     │
└─────────────────┘                  └─────────────────┘                      └─────────────────┘
```

## Backend - Spring Boot

### Structure des Packages
```
com.securebank/
├── controller/          # Contrôleurs REST
│   ├── AuthController
│   ├── UserController
│   └── AccountController
├── service/            # Logique métier
│   ├── AuthService
│   ├── UserService
│   └── PasswordValidationService
├── repository/         # Accès aux données
│   ├── UserRepository
│   └── SessionRepository
├── model/             # Entités JPA
│   ├── User
│   ├── Role
│   └── UserSession
├── dto/               # Data Transfer Objects
│   ├── LoginRequest
│   ├── RegisterRequest
│   └── AuthResponse
├── security/          # Configuration sécurité
│   ├── JwtAuthenticationFilter
│   ├── JwtTokenProvider
│   └── SecurityConfig
├── config/            # Configuration Spring
│   ├── DatabaseConfig
│   └── CorsConfig
├── exception/         # Gestion des erreurs
│   ├── GlobalExceptionHandler
│   └── SecurityException
└── util/              # Utilitaires
    ├── PasswordValidator
    └── SecurityUtils
```

### Sécurité Backend
- **Spring Security 6** pour l'authentification/autorisation
- **JWT** pour la gestion des sessions stateless
- **BCrypt** pour le hachage des mots de passe
- **Rate Limiting** pour prévenir les attaques brute force
- **Input Validation** avec Bean Validation
- **CORS** configuré strictement

## Frontend - React

### Structure des Composants
```
src/
├── components/
│   ├── auth/
│   │   ├── LoginForm
│   │   ├── RegisterForm
│   │   └── PasswordStrengthIndicator
│   ├── common/
│   │   ├── Header
│   │   ├── Footer
│   │   └── LoadingSpinner
│   └── forms/
│       ├── SecureInput
│       └── ValidationMessage
├── pages/
│   ├── auth/
│   │   ├── LoginPage
│   │   └── RegisterPage
│   ├── dashboard/
│   │   └── DashboardPage
│   └── account/
│       └── AccountPage
├── services/
│   ├── authService.ts
│   ├── apiService.ts
│   └── tokenService.ts
├── hooks/
│   ├── useAuth.ts
│   └── useSecureForm.ts
├── context/
│   └── AuthContext.tsx
└── utils/
    ├── passwordValidator.ts
    ├── securityUtils.ts
    └── constants.ts
```

### Sécurité Frontend
- **HTTPS** obligatoire en production
- **Content Security Policy** (CSP)
- **Validation côté client** + côté serveur
- **Stockage sécurisé** des tokens (httpOnly cookies)
- **Protection XSS** avec sanitisation des inputs
- **Timeout automatique** des sessions

## Base de Données

### Schéma Principal (Phase 1)
```sql
-- Table des utilisateurs
users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT true,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Table des rôles
roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Table de liaison utilisateur-rôle
user_roles (
    user_id BIGINT REFERENCES users(id),
    role_id BIGINT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- Table des sessions (pour audit)
user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    token_id VARCHAR(255) UNIQUE NOT NULL,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true
);
```

## Flux d'Authentification

### Inscription
```
1. Client → POST /api/auth/register
2. Validation mot de passe fort
3. Hachage BCrypt
4. Sauvegarde en base
5. Envoi email de confirmation
```

### Connexion
```
1. Client → POST /api/auth/login
2. Vérification credentials
3. Génération JWT (access + refresh)
4. Stockage session en base
5. Retour tokens sécurisés
```

### Validation Continue
```
1. Middleware JWT sur chaque requête
2. Vérification token validity
3. Contrôle des permissions
4. Audit des actions
```