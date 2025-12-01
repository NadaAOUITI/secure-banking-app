const API_BASE_URL = 'https://localhost:8080/api';

class BeneficiaryService {

    /**
     * Get all beneficiaries for the authenticated user
     */
    async getBeneficiaries() {
        try {
            const response = await fetch(`${API_BASE_URL}/beneficiaries`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            return await response.json();
        } catch (error) {
            console.error('Erreur lors de la récupération des bénéficiaires:', error);
            return { success: false, message: 'Erreur de connexion au serveur' };
        }
    }

    /**
     * Add a new beneficiary
     */
    async addBeneficiary(beneficiaryData) {
        try {
            const response = await fetch(`${API_BASE_URL}/beneficiaries`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(beneficiaryData)
            });

            return await response.json();
        } catch (error) {
            console.error('Erreur lors de l\'ajout du bénéficiaire:', error);
            return { success: false, message: 'Erreur de connexion au serveur' };
        }
    }

    /**
     * Update a beneficiary
     */
    async updateBeneficiary(id, beneficiaryData) {
        try {
            const response = await fetch(`${API_BASE_URL}/beneficiaries/${id}`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(beneficiaryData)
            });

            return await response. json();
        } catch (error) {
            console.error('Erreur lors de la mise à jour du bénéficiaire:', error);
            return { success: false, message: 'Erreur de connexion au serveur' };
        }
    }

    /**
     * Delete a beneficiary
     */
    async deleteBeneficiary(id) {
        try {
            const response = await fetch(`${API_BASE_URL}/beneficiaries/${id}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            return await response.json();
        } catch (error) {
            console.error('Erreur lors de la suppression du bénéficiaire:', error);
            return { success: false, message: 'Erreur de connexion au serveur' };
        }
    }

    /**
     * Validate account number format on client side
     */
    validateAccountNumber(accountNumber, bankType) {
        const cleanAccount = accountNumber. replace(/\s/g, '').toUpperCase();

        switch (bankType) {
            case 'SAME_BANK':
                return /^[0-9]{16,20}$/.test(cleanAccount);
            case 'NATIONAL':
                return /^[A-Z0-9]{10,24}$/.test(cleanAccount);
            case 'INTERNATIONAL':
                return /^[A-Z]{2}[0-9]{2}[A-Z0-9]{4,30}$/. test(cleanAccount);
            default:
                return false;
        }
    }

    /**
     * Validate IBAN format
     */
    validateIban(iban) {
        if (!iban) return false;
        const cleanIban = iban.replace(/\s/g, '').toUpperCase();
        return /^[A-Z]{2}[0-9]{2}[A-Z0-9]{4,30}$/. test(cleanIban);
    }

    /**
     * Validate SWIFT/BIC code
     */
    validateSwiftCode(swiftCode) {
        if (!swiftCode) return true; // Optional
        return /^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$/.test(swiftCode. toUpperCase());
    }
}

const beneficiaryService = new BeneficiaryService();
export default beneficiaryService;