const API_BASE_URL = 'http://localhost:8080/api';

export interface RegistrationData {
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  confirmPassword: string;
}

export interface ApiResponse {
  success: boolean;
  message: string;
  user?: any;
  errors?: { [key: string]: string[] };
}

class AuthService {
  
  /**
   * Inscription d'un nouvel utilisateur
   */
  async register(userData: RegistrationData): Promise<ApiResponse> {
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
  async checkEmailExists(email: string): Promise<boolean> {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/check-email?email=${encodeURIComponent(email)}`);
      const data = await response.json();
      return data.exists;
      
    } catch (error) {
      console.error('Erreur lors de la vérification de l\'email:', error);
      return false;
    }
  }
}

export const authService = new AuthService();