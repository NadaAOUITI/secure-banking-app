import React, { useState } from 'react';
import { authService } from '../../services/authService';
import './LoginForm.css';

const LoginForm = ({ onLoginSuccess }) => {
  const [step, setStep] = useState(1); // 1: Login, 2: OTP
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    otpCode: ''
  });
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Effacer les erreurs pour ce champ
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: []
      }));
    }
  };

  const validateLoginForm = () => {
    const newErrors = {};

    // Validation email
    if (!formData.email) {
      newErrors.email = ['Email requis'];
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = ['Format d\'email invalide'];
    }

    // Validation mot de passe
    if (!formData.password) {
      newErrors.password = ['Mot de passe requis'];
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validateOtpForm = () => {
    const newErrors = {};

    // Validation OTP
    if (!formData.otpCode) {
      newErrors.otpCode = ['Code OTP requis'];
    } else if (!/^\d{6}$/.test(formData.otpCode)) {
      newErrors.otpCode = ['Le code OTP doit contenir 6 chiffres'];
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateLoginForm()) {
      return;
    }

    setIsLoading(true);
    setSuccessMessage('');

    try {
      const response = await authService.login({
        email: formData.email,
        password: formData.password
      });
      
      if (response.success && response.requiresOtp) {
        setStep(2); // Passer à l'étape OTP
        setSuccessMessage('Code de vérification envoyé par email');
      } else {
        setErrors({ general: [response.message || 'Erreur lors de la connexion'] });
      }
    } catch (error) {
      setErrors({ general: ['Erreur de connexion au serveur'] });
    } finally {
      setIsLoading(false);
    }
  };

  const handleOtpSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateOtpForm()) {
      return;
    }

    setIsLoading(true);
    setSuccessMessage('');

    try {
      const response = await authService.verifyOtp({
        email: formData.email,
        otpCode: formData.otpCode
      });
      
      if (response.success) {
        setSuccessMessage('Connexion réussie ! Bienvenue ' + response.user.firstName);
        // Rediriger vers le dashboard
        setTimeout(() => {
          onLoginSuccess(response.user);
        }, 1500);
      } else {
        setErrors({ general: [response.message || 'Code OTP invalide'] });
      }
    } catch (error) {
      setErrors({ general: ['Erreur de connexion au serveur'] });
    } finally {
      setIsLoading(false);
    }
  };

  const handleBackToLogin = () => {
    setStep(1);
    setFormData(prev => ({ ...prev, otpCode: '' }));
    setErrors({});
    setSuccessMessage('');
  };

  return (
    <div className="login-form-container">
      <form onSubmit={step === 1 ? handleLoginSubmit : handleOtpSubmit} className="login-form">
        <h2>{step === 1 ? 'Se connecter' : 'Vérification OTP'}</h2>
        
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

        {step === 1 && (
          <>
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
              {errors.password && (
                <div className="field-errors">
                  {errors.password.map((error, index) => (
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
              {isLoading ? 'Connexion en cours...' : 'Se connecter'}
            </button>
          </>
        )}

        {step === 2 && (
          <>
            <div className="otp-info">
              <p>Un code de vérification à 6 chiffres a été envoyé à :</p>
              <strong>{formData.email}</strong>
            </div>

            <div className="form-group">
              <label htmlFor="otpCode">Code de vérification *</label>
              <input
                type="text"
                id="otpCode"
                name="otpCode"
                value={formData.otpCode}
                onChange={handleInputChange}
                className={errors.otpCode ? 'error' : ''}
                placeholder="123456"
                maxLength="6"
                required
              />
              {errors.otpCode && (
                <div className="field-errors">
                  {errors.otpCode.map((error, index) => (
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
              {isLoading ? 'Vérification en cours...' : 'Vérifier le code'}
            </button>

            <button 
              type="button" 
              className="back-button"
              onClick={handleBackToLogin}
            >
              Retour à la connexion
            </button>
          </>
        )}
      </form>
    </div>
  );
};

export default LoginForm;