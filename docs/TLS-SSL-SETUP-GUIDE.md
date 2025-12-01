# 🔒 Guide Configuration TLS/SSL - Setup Rapide

> Guide simple pour configurer HTTPS sur l'application bancaire

## 📋 Prérequis
- Application backend et frontend fonctionnels
- Accès terminal/PowerShell

## 🚀 Étapes Rapides (5 minutes)

### 1️⃣ Installer mkcert
```bash
# Windows (avec Chocolatey)
choco install mkcert

# Ou télécharger depuis : https://github.com/FiloSottile/mkcert/releases
```

### 2️⃣ Initialiser mkcert
```bash
mkcert -install
```

### 3️⃣ Générer certificats Backend
```bash
# Aller dans le dossier backend
cd backend/src/main/resources

# Créer dossier ssl
mkdir ssl
cd ssl

# Générer certificat
mkcert localhost 127.0.0.1 ::1
```

### 4️⃣ Générer certificats Frontend
```bash
# Aller dans le dossier frontend
cd frontend

# Créer dossier ssl (si pas déjà fait)
mkdir ssl
cd ssl

# Générer certificat pour React
mkcert localhost 127.0.0.1 ::1
```

### 5️⃣ Configuration Backend
Dans `backend/src/main/resources/application.properties` :
```properties
# TLS/SSL Configuration
server.ssl.enabled=true
server.ssl.key-store=classpath:ssl/localhost+2.pem
server.ssl.key-store-password=
server.ssl.key-store-type=PEM
server.ssl.key-alias=localhost
```

### 6️⃣ Configuration Frontend
Créer/vérifier `frontend/.env` :
```
HTTPS=true
SSL_CRT_FILE=./ssl/localhost+2.pem
SSL_KEY_FILE=./ssl/localhost+2-key.pem
```

### 7️⃣ Démarrer les applications
```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend (nouveau terminal)
cd frontend
npm start
```

## ✅ Vérification

### URLs à tester :
- **Frontend** : https://localhost:3000
- **Backend** : https://localhost:8080
- **Test API** : https://localhost:8080/

### Signes de succès :
- ✅ Cadenas vert dans le navigateur
- ✅ Pas d'erreur "connexion non sécurisée"
- ✅ Login fonctionne sans "Erreur de connexion au serveur"

## 🔧 Dépannage Rapide

### Problème : "Certificat non trouvé"
```bash
# Vérifier que les fichiers existent
ls backend/src/main/resources/ssl/
ls frontend/ssl/
```

### Problème : "Erreur de connexion au serveur"
1. Vérifier que les URLs sont en HTTPS partout
2. Vérifier CORS dans SecurityConfig.java : `https://localhost:3000`

### Problème : "Port déjà utilisé"
```bash
# Tuer les processus
taskkill /f /im java.exe
taskkill /f /im node.exe
```

## 📁 Structure finale
```
backend/src/main/resources/ssl/
├── localhost+2.pem
└── localhost+2-key.pem

frontend/ssl/
├── localhost+2.pem
└── localhost+2-key.pem

frontend/.env (contient HTTPS=true)
```

## 🎯 Résultat Final
- **Communication chiffrée** entre frontend et backend
- **Certificats valides** reconnus par le navigateur
- **Sécurité bancaire** conforme aux standards

---

**⏱️ Temps total : ~5 minutes**  
**✅ Une fois configuré, ça marche pour toujours !**