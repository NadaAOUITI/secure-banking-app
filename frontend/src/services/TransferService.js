const API_BASE_URL = 'http://localhost:8080/api';

class TransferService {

    /**
     * Initiate a new transfer (Step 1)
     */
    async initiateTransfer(transferData) {
        try {
            const response = await fetch(`${API_BASE_URL}/transfers/initiate`, {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(transferData)
            });
            return await response.json();
        } catch (error) {
            console.error('Erreur:', error);
            return { success: false, message: 'Erreur de connexion au serveur' };
        }
    }

    /**
     * Confirm transfer with OTP (Step 2)
     */
    async confirmTransfer(reference, otpCode) {
        try {
            const response = await fetch(`${API_BASE_URL}/transfers/confirm`, {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON. stringify({ reference, otpCode })
            });
            return await response.json();
        } catch (error) {
            console.error('Erreur:', error);
            return { success: false, message: 'Erreur de connexion au serveur' };
        }
    }

    /**
     * Cancel a pending transfer
     */
    async cancelTransfer(reference) {
        try {
            const response = await fetch(`${API_BASE_URL}/transfers/${reference}/cancel`, {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' }
            });
            return await response.json();
        } catch (error) {
            console.error('Erreur:', error);
            return { success: false, message: 'Erreur de connexion au serveur' };
        }
    }

    /**
     * Get transfer history
     */
    async getTransferHistory(filters = {}) {
        try {
            const params = new URLSearchParams();
            if (filters.startDate) params.append('startDate', filters.startDate);
            if (filters.endDate) params.append('endDate', filters.endDate);
            if (filters.minAmount) params.append('minAmount', filters. minAmount);
            if (filters.maxAmount) params.append('maxAmount', filters.maxAmount);
            if (filters. beneficiaryId) params.append('beneficiaryId', filters.beneficiaryId);

            const url = `${API_BASE_URL}/transfers/history${params.toString() ?  '?' + params. toString() : ''}`;

            const response = await fetch(url, {
                method: 'GET',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' }
            });
            return await response.json();
        } catch (error) {
            console.error('Erreur:', error);
            return { success: false, message: 'Erreur de connexion au serveur' };
        }
    }

    /**
     * Get single transfer details
     */
    async getTransfer(reference) {
        try {
            const response = await fetch(`${API_BASE_URL}/transfers/${reference}`, {
                method: 'GET',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' }
            });
            return await response.json();
        } catch (error) {
            console.error('Erreur:', error);
            return { success: false, message: 'Erreur de connexion au serveur' };
        }
    }


}

const transferService = new TransferService();
export default transferService;