const API_BASE_URL = 'https://localhost:8080/api';

class AccountService {
  
  getAuthHeaders() {
    return {
      'Content-Type': 'application/json'
    };
  }

  async getAccountDetails() {
    try {
      const response = await fetch(`${API_BASE_URL}/account/details`, {
        method: 'GET',
        headers: this.getAuthHeaders(),
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Erreur lors de la récupération des détails:', error);
      throw new Error('Erreur de connexion au serveur');
    }
  }

  async getUserCards() {
    try {
      const response = await fetch(`${API_BASE_URL}/cards/user-cards`, {
        method: 'GET',
        headers: this.getAuthHeaders(),
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Erreur lors de la récupération des cartes:', error);
      throw new Error('Erreur de connexion au serveur');
    }
  }

  async testCards() {
    try {
      const response = await fetch(`${API_BASE_URL}/cards/test-cards`, {
        method: 'GET',
        headers: this.getAuthHeaders(),
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Erreur lors du test des cartes:', error);
      throw new Error('Erreur de connexion au serveur');
    }
  }
}

export const accountService = new AccountService();
