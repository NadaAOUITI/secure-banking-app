const API_BASE_URL = 'http://localhost:8080/api';

class AuthService {
  
  /**
   * Inscription d'un nouvel utilisateur
   */
  async register(userData) {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData),
      });

      const data = await response.json();
      return data;
      
    } catch (error) {
      console.error('Erreur lors de l\'inscription:', error);
      return {
        success: false,
        message: 'Erreur de connexion au serveur'
      };
    }
  }

  /**
   * Vérification de l'existence d'un email
   */
  async checkEmailExists(email) {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/check-email?email=${encodeURIComponent(email)}`);
      const data = await response.json();
      return data.exists;
      
    } catch (error) {
      console.error('Erreur lors de la vérification de l\'email:', error);
      return false;
    }
  }

  /**
   * Connexion - Étape 1: Vérification email/mot de passe
   */
  async login(loginData) {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(loginData),
      });

      const data = await response.json();
      return data;
      
    } catch (error) {
      console.error('Erreur lors de la connexion:', error);
      return {
        success: false,
        message: 'Erreur de connexion au serveur'
      };
    }
  }

  /**
   * Vérification OTP - Étape 2: Vérification du code
   */
  async verifyOtp(otpData) {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/verify-otp`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(otpData),
      });

      const data = await response.json();
      return data;
      
    } catch (error) {
      console.error('Erreur lors de la vérification OTP:', error);
      return {
        success: false,
        message: 'Erreur de connexion au serveur'
      };
    }
  }
}

export const authService = new AuthService();