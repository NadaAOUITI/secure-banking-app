const API_BASE_URL = 'https://localhost:8080/api';

class TransactionService {

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

            if (!response. ok) {
                if (response.status === 401) throw new Error('Session expirée');
                throw new Error(`Erreur serveur (${response.status})`);
            }

            return await response. json();
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

            if (!response. ok) throw new Error(`Erreur serveur (${response. status})`);
            return await response. json();
        } catch (error) {
            console.error('Erreur récupération statistiques:', error);
            throw error;
        }
    }

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

            if (!response. ok) throw new Error(`Erreur serveur (${response. status})`);
            return await response. json();
        } catch (error) {
            console.error('Erreur récupération compte:', error);
            throw error;
        }
    }

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

            if (!response.ok) throw new Error(`Erreur serveur (${response.status})`);
            return await response.json();
        } catch (error) {
            console. error('Erreur récupération cartes:', error);
            throw error;
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

            if (! response.ok) throw new Error(`Erreur serveur (${response.status})`);
            return await response.json();
        } catch (error) {
            console.error('Erreur statut carte:', error);
            throw error;
        }
    }

    /**
     * ÉTAPE 1: Vérifier le code PIN de la carte
     */
    async verifyPin(cardId, pin) {
        try {
            const response = await fetch(`${API_BASE_URL}/cards/verify-pin`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ cardId, pin })
            });

            const data = await response.json();
            return {
                success: data.success,
                message: data.message,
                remainingAttempts: data.remainingAttempts,
                cardBlocked: data.cardBlocked,
                reason: data.reason
            };
        } catch (error) {
            console.error('Erreur vérification PIN:', error);
            return { success: false, message: 'Erreur de connexion' };
        }
    }

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
                success: data.success,
                message: data.message
            };
        } catch (error) {
            console.error('Erreur envoi OTP:', error);
            return { success: false, message: 'Erreur lors de l\'envoi du code' };
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
                body: JSON.stringify({ email, otpCode })
            });

            const data = await response.json();
            return {
                success: data. success,
                message: data.message
            };
        } catch (error) {
            console.error('Erreur vérification OTP:', error);
            return { success: false, message: 'Erreur de vérification' };
        }
    }

    /**
     * ÉTAPE 4: Effectuer la transaction sécurisée (après PIN + OTP validés)
     */
    async processSecureTransaction(transactionData) {
        try {
            const response = await fetch(`${API_BASE_URL}/cards/transaction`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(transactionData)
            });

            const data = await response.json();
            return {
                success: data.success,
                message: data.message,
                errorType: data.errorType,
                referenceNumber: data. referenceNumber,
                amount: data.amount,
                transactionDate: data.transactionDate,
                remainingDailyLimit: data.remainingDailyLimit,
                remainingDailyTransactions: data.remainingDailyTransactions,
                remainingAttempts: data. remainingAttempts,
                cardBlocked: data.cardBlocked,
                limitType: data.limitType,
                requested: data.requested,
                limit: data.limit
            };
        } catch (error) {
            console.error('Erreur transaction:', error);
            return { success: false, message: 'Erreur lors de la transaction' };
        }
    }

    /**
     * Créer une transaction simple (sans sécurité carte - pour admin ou tests)
     */
    async createTransaction(accountId, type, amount, description, reference = null) {
        try {
            const response = await fetch(`${API_BASE_URL}/transaction`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON. stringify({ accountId, type, amount, description, reference })
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('Erreur création transaction:', error);
            return { success: false, message: 'Erreur lors de la création' };
        }
    }

    /**
     * Bloquer une carte
     */
    async blockCard(cardId, reason = null) {
        try {
            const response = await fetch(`${API_BASE_URL}/cards/${cardId}/block`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ reason })
            });

            return await response.json();
        } catch (error) {
            console.error('Erreur blocage carte:', error);
            return { success: false, message: 'Erreur lors du blocage' };
        }
    }

    /**
     * Masquer l'email pour l'affichage
     */
    maskEmail(email) {
        if (!email) return '';
        const atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        return email.substring(0, 2) + '***' + email.substring(atIndex);
    }
}

export const transactionService = new TransactionService();