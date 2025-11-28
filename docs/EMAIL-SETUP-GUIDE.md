# 📧 Guide de Configuration Email - OTP

## 🎯 Objectif
Pour que l'authentification OTP fonctionne, chaque développeur doit configurer l'envoi d'emails avec Gmail App Password.

## 🔧 Configuration Requise

### Étape 1: Générer un Mot de Passe d'Application Gmail

1. **Aller directement sur** : https://myaccount.google.com/apppasswords
2. **Activer l'authentification à 2 facteurs** (si pas déjà fait)
3. **Générer un mot de passe d'application**
4. **Sélectionner** :
   - Application : `Mail`
   - Appareil : `Other` → écrire `"Spring Boot Banking App"`
5. **Copier le mot de passe de 16 caractères** généré

### Étape 2: Créer le Fichier de Configuration Sécurisé

Créer le fichier : `backend/src/main/resources/application-secret.properties`

```properties
# Configuration Email Sécurisée (ne pas commiter)
spring.mail.username=VOTRE_EMAIL@gmail.com
spring.mail.password=VOTRE_MOT_DE_PASSE_APP_16_CARACTERES
```

### Étape 3: Vérifier le .gitignore

Le fichier `backend/.gitignore` doit contenir :

```
# Fichiers de configuration sécurisés
application-secret.properties
*.secret.properties
```

## ⚠️ Règles de Sécurité

### ✅ À FAIRE
- Utiliser votre propre email Gmail
- Générer un mot de passe d'application unique
- Garder le fichier `application-secret.properties` local uniquement
- Révoquer le mot de passe d'application après le projet

### ❌ NE JAMAIS FAIRE
- Partager votre mot de passe d'application
- Commiter `application-secret.properties` sur Git
- Utiliser votre mot de passe Gmail principal
- Laisser le mot de passe d'application actif après le projet

## 🧪 Test de Configuration

1. Démarrer l'application : `mvn spring-boot:run`
2. Aller sur : http://localhost:3000
3. S'inscrire avec votre email
4. Vérifier la réception de l'OTP par email

## 🔒 Sécurité Technique

- **Protocole** : SMTP avec TLS/SSL
- **Port** : 587 (sécurisé)
- **Authentification** : App Password (non votre mot de passe principal)
- **Chiffrement** : Communications chiffrées end-to-end
- **Limitation** : Accès SMTP uniquement (pas de lecture/suppression d'emails)

## 🆘 Dépannage

**Problème** : Email non reçu
- Vérifier le dossier spam
- Vérifier que l'authentification 2FA est activée
- Régénérer un nouveau mot de passe d'application

**Problème** : Erreur d'authentification
- Vérifier que le mot de passe fait bien 16 caractères
- Pas d'espaces dans le mot de passe
- Utiliser votre email Gmail complet

## 📞 Support

En cas de problème, vérifier les logs de l'application ou contacter l'équipe.