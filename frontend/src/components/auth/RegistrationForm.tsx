import React, { useState } from 'react';
import { validatePassword, PasswordValidationResult } from '../../utils/passwordValidator';
import { authService } from '../../services/authService';
import './RegistrationForm.css';

interface FormData {
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  confirmPassword: string;
}

interface FormErrors {
  [key: string]: string[];
}

const RegistrationForm: React.FC = () => {
  const [formData, setFormData] = useState<FormData>({
    email: '',
    firstName: '',
    lastName: '',
    password: '',
    confirmPassword: ''
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [passwordValidation, setPasswordValidation] = useState<PasswordValidationResult>({
    isValid: false,
    errors: []
  });
  const [isLoading, setIsLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Validation en temps réel du mot de passe
    if (name === 'password') {
      const validation = validatePassword(value);
      setPasswordValidation(validation);
    }

    // Effacer les erreurs pour ce champ
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: []
      }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    // Validation email
    if (!formData.email) {
      newErrors.email = ['Email requis'];
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = ['Format d\'email invalide'];
    }

    // Validation prénom
    if (!formData.firstName) {
      newErrors.firstName = ['Prénom requis'];
    } else if (formData.firstName.length < 2) {
      newErrors.firstName = ['Le prénom doit contenir au moins 2 caractères'];
    }

    // Validation nom
    if (!formData.lastName) {
      newErrors.lastName = ['Nom requis'];
    } else if (formData.lastName.length < 2) {
      newErrors.lastName = ['Le nom doit contenir au moins 2 caractères'];
    }

    // Validation mot de passe
    if (!formData.password) {
      newErrors.password = ['Mot de passe requis'];
    } else if (!passwordValidation.isValid) {
      newErrors.password = passwordValidation.errors;
    }

    // Validation confirmation mot de passe
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = ['Confirmation du mot de passe requise'];
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = ['Les mots de passe ne correspondent pas'];
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    setSuccessMessage('');

    try {
      const response = await authService.register(formData);
      
      if (response.success) {
        setSuccessMessage('Inscription réussie ! Vous pouvez maintenant vous connecter.');
        setFormData({
          email: '',
          firstName: '',
          lastName: '',
          password: '',
          confirmPassword: ''
        });
        setPasswordValidation({ isValid: false, errors: [] });
      } else {
        if (response.errors) {
          setErrors(response.errors);
        } else {
          setErrors({ general: [response.message || 'Erreur lors de l\'inscription'] });
        }
      }
    } catch (error) {
      setErrors({ general: ['Erreur de connexion au serveur'] });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="registration-form-container">
      <form onSubmit={handleSubmit} className="registration-form">
        <h2>Créer un compte</h2>
        
        {successMessage && (
          <div className="success-message">{successMessage}</div>
        )}
        
        {errors.general && (
          <div className="error-message">
            {errors.general.map((error, index) => (
              <div key={index}>{error}</div>
            ))}
          </div>
        )}

        <div className="form-group">
          <label htmlFor="email">Email *</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleInputChange}
            className={errors.email ? 'error' : ''}
            required
          />
          {errors.email && (
            <div className="field-errors">
              {errors.email.map((error, index) => (
                <div key={index} className="field-error">{error}</div>
              ))}
            </div>
          )}
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="firstName">Prénom *</label>
            <input
              type="text"
              id="firstName"
              name="firstName"
              value={formData.firstName}
              onChange={handleInputChange}
              className={errors.firstName ? 'error' : ''}
              required
            />
            {errors.firstName && (
              <div className="field-errors">
                {errors.firstName.map((error, index) => (
                  <div key={index} className="field-error">{error}</div>
                ))}
              </div>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="lastName">Nom *</label>
            <input
              type="text"
              id="lastName"
              name="lastName"
              value={formData.lastName}
              onChange={handleInputChange}
              className={errors.lastName ? 'error' : ''}
              required
            />
            {errors.lastName && (
              <div className="field-errors">
                {errors.lastName.map((error, index) => (
                  <div key={index} className="field-error">{error}</div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="password">Mot de passe *</label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleInputChange}
            className={errors.password ? 'error' : ''}
            required
          />
          
          {/* Indicateur de force du mot de passe */}
          {formData.password && (
            <div className="password-strength">
              <div className="strength-indicators">
                <div className={`strength-indicator ${passwordValidation.isValid ? 'valid' : 'invalid'}`}>
                  {passwordValidation.isValid ? '✓' : '✗'} Mot de passe fort
                </div>
              </div>
              
              {!passwordValidation.isValid && passwordValidation.errors.length > 0 && (
                <div className="password-requirements">
                  <p>Le mot de passe doit contenir :</p>
                  <ul>
                    {passwordValidation.errors.map((error, index) => (
                      <li key={index}>{error}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}
          
          {errors.password && (
            <div className="field-errors">
              {errors.password.map((error, index) => (
                <div key={index} className="field-error">{error}</div>
              ))}
            </div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="confirmPassword">Confirmer le mot de passe *</label>
          <input
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleInputChange}
            className={errors.confirmPassword ? 'error' : ''}
            required
          />
          {errors.confirmPassword && (
            <div className="field-errors">
              {errors.confirmPassword.map((error, index) => (
                <div key={index} className="field-error">{error}</div>
              ))}
            </div>
          )}
        </div>

        <button 
          type="submit" 
          className="submit-button"
          disabled={isLoading}
        >
          {isLoading ? 'Inscription en cours...' : 'S\'inscrire'}
        </button>
      </form>
    </div>
  );
};

export default RegistrationForm;