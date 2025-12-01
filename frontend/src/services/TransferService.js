const API_BASE_URL = "https://localhost:8080/api"

class TransferService {
    // Helper method to handle API responses
    async handleResponse(response) {
        if (!response. ok) {
            const errorData = await response.json(). catch(() => ({}));
            return {
                success: false,
                message: errorData.message || `Erreur ${response. status}: ${response.statusText}`
            };
        }
        return await response.json();
    }

    async initiateTransfer(transferData) {
        try {
            const response = await fetch(`${API_BASE_URL}/transfers/initiate`, {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(transferData),
            });
            return await this.handleResponse(response);
        } catch (error) {
            console. error("Transfer initiation error:", error);
            return { success: false, message: "Erreur de connexion au serveur" };
        }
    }

    async confirmTransfer(reference, otpCode) {
        try {
            const response = await fetch(`${API_BASE_URL}/transfers/confirm`, {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ reference, otpCode }),
            });
            return await this. handleResponse(response);
        } catch (error) {
            console.error("OTP confirmation error:", error);
            return { success: false, message: "Erreur de connexion au serveur" };
        }
    }

    async cancelTransfer(reference) {
        try {
            const response = await fetch(`${API_BASE_URL}/transfers/${reference}/cancel`, {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error("Cancel transfer error:", error);
            return { success: false, message: "Erreur de connexion au serveur" };
        }
    }

    async getTransferHistory(filters = {}) {
        try {
            const params = new URLSearchParams();
            if (filters.startDate) params.append("startDate", filters.startDate);
            if (filters. endDate) params. append("endDate", filters.endDate);
            if (filters.minAmount) params.append("minAmount", filters.minAmount);
            if (filters. maxAmount) params. append("maxAmount", filters.maxAmount);
            if (filters.beneficiaryId) params.append("beneficiaryId", filters. beneficiaryId);

            const queryString = params.toString();
            const url = `${API_BASE_URL}/transfers/history${queryString ? "?" + queryString : ""}`;

            const response = await fetch(url, {
                method: "GET",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error("History fetch error:", error);
            return { success: false, message: "Erreur de connexion au serveur", transfers: [] };
        }
    }

    async getTransfer(reference) {
        try {
            const response = await fetch(`${API_BASE_URL}/transfers/${reference}`, {
                method: "GET",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
            });
            return await this.handleResponse(response);
        } catch (error) {
            console. error("Transfer fetch error:", error);
            return { success: false, message: "Erreur de connexion au serveur" };
        }
    }

    // Get user accounts using the existing /api/account/details endpoint
    async getUserAccounts() {
        try {
            const response = await fetch(`${API_BASE_URL}/account/details`, {
                method: "GET",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
            });

            if (!response. ok) {
                console.error("Account fetch failed with status:", response.status);
                return { success: false, message: "Erreur de chargement des comptes", accounts: [] };
            }

            const data = await response. json();
            console.log("getUserAccounts response:", data); // Debug log

            // Handle AccountDetailsDto response format
            // The backend returns AccountDetailsDto which should have accounts array
            let accounts = [];

            if (data. accounts && Array.isArray(data.accounts)) {
                accounts = data.accounts;
            } else if (Array.isArray(data)) {
                accounts = data;
            } else if (data. account) {
                accounts = [data. account];
            } else if (data.id && (data.accountNumber || data. rib)) {
                // Single account object
                accounts = [data];
            }

            return { success: true, accounts };
        } catch (error) {
            console.error("Account fetch error:", error);
            return { success: false, message: "Erreur de connexion au serveur", accounts: [] };
        }
    }
}

export default new TransferService();