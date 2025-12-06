const API_BASE_URL = 'https://localhost:8080/api';

class TransactionService {

    // ==================== TRANSACTIONS ====================

    /**
     * Récupérer toutes les transactions de l'utilisateur
     */
    async getTransactions() {
        try {
            const response = await fetch(`${API_BASE_URL}/transaction`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include'
            });

            if (!response.ok) {
                if (response.status === 401) throw new Error('Session expirée');
                if (response.status === 403) throw new Error('Accès refusé');
                throw new Error(`Erreur serveur (${response.status})`);
            }

            return await response.json();
        } catch (error) {
            console.error('Erreur récupération transactions:', error);
            throw error;
        }
    }

    /**
     * Récupérer les transactions d'un compte spécifique
     */
    async getTransactionsByAccount(accountId) {
        try {
            const response = await fetch(`${API_BASE_URL}/transaction/account/${accountId}`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include'
            });

            if (!response.ok) throw new Error(`Erreur serveur (${response.status})`);
            return await response. json();
        } catch (error) {
            console.error('Erreur récupération transactions compte:', error);
            throw error;
        }
    }

    /**
     * Récupérer les statistiques des transactions
     */
    async getStatistics() {
        try {
            const response = await fetch(`${API_BASE_URL}/transaction/statistics`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include'
            });

            if (!response.ok) throw new Error(`Erreur serveur (${response.status})`);
            return await response.json();
        } catch (error) {
            console.error('Erreur récupération statistiques:', error);
            throw error;
        }
    }

    // ==================== COMPTE ====================

    /**
     * Récupérer les détails du compte (avec les comptes bancaires)
     */
    async getAccountDetails() {
        try {
            const response = await fetch(`${API_BASE_URL}/account/details`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include'
            });

            if (!response.ok) {
                if (response.status === 401) throw new Error('Session expirée');
                if (response.status === 403) throw new Error('Accès refusé - Veuillez vous reconnecter');
                throw new Error(`Erreur serveur (${response.status})`);
            }

            return await response.json();
        } catch (error) {
            console. error('Erreur récupération compte:', error);
            throw error;
        }
    }

    // ==================== CARTES ====================

    /**
     * Récupérer les cartes de l'utilisateur
     */
    async getUserCards() {
        try {
            const response = await fetch(`${API_BASE_URL}/cards/user-cards`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include'
            });

            if (!response.ok) {
                if (response.status === 401) throw new Error('Session expirée');
                throw new Error(`Erreur serveur (${response.status})`);
            }

            return await response. json();
        } catch (error) {
            console.error('Erreur récupération cartes:', error);
            // Retourner un objet vide au lieu de lancer une erreur
            return { success: false, cards: [], totalCards: 0 };
        }
    }

    /**
     * Vérifier le statut d'une carte
     */
    async getCardStatus(cardId) {
        try {
            const response = await fetch(`${API_BASE_URL}/cards/${cardId}/status`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include'
            });

            if (! response.ok) {
                const errorData = await response. json(). catch(() => ({}));
                return {
                    success: false,
                    message: errorData.message || `Erreur serveur (${response.status})`,
                    blocked: false,
                    active: false
                };
            }

            return await response.json();
        } catch (error) {
            console.error('Erreur statut carte:', error);
            return {
                success: false,
                message: 'Erreur de connexion',
                blocked: false,
                active: false
            };
        }
    }

    // ==================== SÉCURITÉ - PIN ====================

    /**
     * ÉTAPE 1: Vérifier le code PIN de la carte
     */
    async verifyPin(cardId, pin) {
        try {
            const response = await fetch(`${API_BASE_URL}/cards/verify-pin`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({
                    cardId: parseInt(cardId),
                    pin: pin. toString()
                })
            });

            const data = await response. json();

            return {
                success: data.success || false,
                message: data.message || 'Erreur inconnue',
                remainingAttempts: data.remainingAttempts,
                cardBlocked: data.cardBlocked || false,
                reason: data.reason || data.blockReason
            };
        } catch (error) {
            console.error('Erreur vérification PIN:', error);
            return {
                success: false,
                message: 'Erreur de connexion au serveur',
                remainingAttempts: null,
                cardBlocked: false
            };
        }
    }

    // ==================== SÉCURITÉ - OTP ====================

    /**
     * ÉTAPE 2: Envoyer le code OTP par email
     */
    async sendOtp(email) {
        try {
            const response = await fetch(`${API_BASE_URL}/otp/send`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ email })
            });

            const data = await response.json();

            return {
                success: data. success || false,
                message: data. message || 'Erreur lors de l\'envoi'
            };
        } catch (error) {
            console. error('Erreur envoi OTP:', error);
            return {
                success: false,
                message: 'Erreur de connexion - Impossible d\'envoyer le code'
            };
        }
    }

    /**
     * ÉTAPE 3: Vérifier le code OTP
     */
    async verifyOtp(email, otpCode) {
        try {
            const response = await fetch(`${API_BASE_URL}/otp/verify`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({
                    email,
                    otpCode: otpCode.toString()
                })
            });

            const data = await response.json();

            return {
                success: data. success || false,
                message: data. message || 'Erreur de vérification'
            };
        } catch (error) {
            console.error('Erreur vérification OTP:', error);
            return {
                success: false,
                message: 'Erreur de connexion'
            };
        }
    }

    // ==================== TRANSACTION SÉCURISÉE ====================

    /**
     * ÉTAPE 4: Effectuer la transaction sécurisée (après PIN + OTP validés)
     * Envoie les données au format attendu par CardTransactionRequest
     */
    async processSecureTransaction(transactionData) {
        try {
            // ✅ Formater les données selon CardTransactionRequest. java
            const requestBody = {
                cardId: parseInt(transactionData. cardId),
                pin: transactionData. pin. toString(),
                amount: parseFloat(transactionData.amount),
                type: transactionData. type. toLowerCase(),  // "credit" ou "debit"
                description: transactionData.description?. trim() || 'Transaction',
                reference: transactionData.reference?. trim() || null,
                beneficiary: transactionData.beneficiary?.trim() || null
            };

            console.log('📤 Envoi transaction:', {
                ... requestBody,
                pin: '****' // Masquer le PIN dans les logs
            });

            const response = await fetch(`${API_BASE_URL}/cards/transaction`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(requestBody)
            });

            const data = await response. json();

            console.log('📥 Réponse transaction:', data);

            return {
                success: data.success || false,
                message: data.message || 'Erreur inconnue',
                errorType: data. errorType,
                referenceNumber: data.referenceNumber,
                amount: data.amount,
                type: data.type,
                newBalance: data.newBalance,
                transactionDate: data. transactionDate,
                remainingDailyLimit: data. remainingDailyLimit,
                remainingDailyTransactions: data.remainingDailyTransactions,
                remainingAttempts: data.remainingAttempts,
                cardBlocked: data.cardBlocked || false,
                limitType: data.limitType,
                requested: data.requested,
                limit: data.limit
            };
        } catch (error) {
            console.error('❌ Erreur transaction:', error);
            return {
                success: false,
                message: 'Erreur de connexion lors de la transaction'
            };
        }
    }

    // ==================== TRANSACTION SIMPLE ====================

    /**
     * Créer une transaction simple (via TransactionController - sans carte)
     */
    async createTransaction(accountId, type, amount, description, reference = null) {
        try {
            const response = await fetch(`${API_BASE_URL}/transaction`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({
                    accountId: parseInt(accountId),
                    type: type.toLowerCase(),
                    amount: parseFloat(amount),
                    description: description?.trim() || 'Transaction',
                    reference: reference?.trim() || null
                })
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('Erreur création transaction:', error);
            return { success: false, message: 'Erreur lors de la création' };
        }
    }

    // ==================== GESTION DES CARTES ====================

    /**
     * Bloquer une carte
     */
    async blockCard(cardId, reason = null) {
        try {
            const response = await fetch(`${API_BASE_URL}/cards/${cardId}/block`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ reason: reason || 'Bloquée par l\'utilisateur' })
            });

            return await response.json();
        } catch (error) {
            console. error('Erreur blocage carte:', error);
            return { success: false, message: 'Erreur lors du blocage' };
        }
    }

    /**
     * Débloquer une carte
     */
    async unblockCard(cardId) {
        try {
            const response = await fetch(`${API_BASE_URL}/cards/${cardId}/unblock`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include'
            });

            return await response.json();
        } catch (error) {
            console.error('Erreur déblocage carte:', error);
            return { success: false, message: 'Erreur lors du déblocage' };
        }
    }

    // ==================== UTILITAIRES ====================

    /**
     * Masquer l'email pour l'affichage (ex: te***@gmail.com)
     */
    maskEmail(email) {
        if (! email) return '';
        const atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        return email.substring(0, 2) + '***' + email.substring(atIndex);
    }

    /**
     * Formater un numéro de carte (masqué)
     */
    maskCardNumber(cardNumber) {
        if (!cardNumber) return '**** **** **** ****';
        const last4 = cardNumber.slice(-4);
        return `**** **** **** ${last4}`;
    }

    /**
     * Formater un montant en MAD
     */
    formatAmount(amount, type = null) {
        if (amount === null || amount === undefined) return '0. 00 MAD';
        const formatted = parseFloat(amount).toFixed(2);
        const prefix = type === 'credit' ?  '+' : (type === 'debit' ? '-' : '');
        return `${prefix}${formatted} MAD`;
    }

    /**
     * Vérifier si la session est valide
     */
    async checkSession() {
        try {
            const response = await fetch(`${API_BASE_URL}/session/status`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include'
            });

            const data = await response. json();
            return {
                isValid: data.isValid || false,
                userEmail: data.userEmail,
                message: data.message
            };
        } catch (error) {
            console.error('Erreur vérification session:', error);
            return { isValid: false, message: 'Erreur de connexion' };
        }
    }
}

// Exporter une instance unique
export const transactionService = new TransactionService();