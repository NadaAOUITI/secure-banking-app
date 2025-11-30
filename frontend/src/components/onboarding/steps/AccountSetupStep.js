import React, { useState } from 'react';
import './StepStyles.css';

const AccountSetupStep = ({ data, onNext, onBack }) => {
  const [formData, setFormData] = useState({
    password: data.password || '',
    confirmPassword: data.confirmPassword || '',
    acceptTerms: data.acceptTerms || false,
    ...data
  });

  const [errors, setErrors] = useState({});
  const [showPassword, setShowPassword] = useState(false);



  const validatePassword = (password) => {
    const minLength = password.length >= 12;
    const hasUpper = /[A-Z]/.test(password);
    const hasLower = /[a-z]/.test(password);
    const hasNumber = /\d/.test(password);
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);
    
    return minLength && hasUpper && hasLower && hasNumber && hasSpecial;
  };

  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.password) {
      newErrors.password = 'Mot de passe requis';
    } else if (!validatePassword(formData.password)) {
      newErrors.password = 'Le mot de passe doit contenir au moins 12 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial';
    }
    
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Les mots de passe ne correspondent pas';
    }
    
    if (!formData.acceptTerms) newErrors.acceptTerms = 'Vous devez accepter les conditions';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validateForm()) {
      onNext(formData);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value
    });
  };

  return (
    <div className="step-container">
      <h2>Configuration de votre compte</h2>
      <p>Créez votre mot de passe et configurez vos questions de sécurité</p>

      <form onSubmit={handleSubmit} className="step-form">
        <h3>Mot de passe</h3>
        
        <div className="form-group">
          <label>Mot de passe *</label>
          <div className="password-input">
            <input
              type={showPassword ? 'text' : 'password'}
              name="password"
              value={formData.password}
              onChange={handleChange}
              className={errors.password ? 'error' : ''}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="password-toggle"
            >
              {showPassword ? '👁️' : '👁️‍🗨️'}
            </button>
          </div>
          {errors.password && <span className="error-text">{errors.password}</span>}
          <div className="password-requirements">
            <small>
              • Au moins 12 caractères<br/>
              • Une majuscule et une minuscule<br/>
              • Un chiffre et un caractère spécial
            </small>
          </div>
        </div>

        <div className="form-group">
          <label>Confirmer le mot de passe *</label>
          <input
            type="password"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            className={errors.confirmPassword ? 'error' : ''}
          />
          {errors.confirmPassword && <span className="error-text">{errors.confirmPassword}</span>}
        </div>



        <div className="form-group checkbox-group">
          <label className="checkbox-label">
            <input
              type="checkbox"
              name="acceptTerms"
              checked={formData.acceptTerms}
              onChange={handleChange}
            />
            J'accepte les <a href="#" target="_blank">conditions générales</a> et la <a href="#" target="_blank">politique de confidentialité</a> *
          </label>
          {errors.acceptTerms && <span className="error-text">{errors.acceptTerms}</span>}
        </div>

        <div className="step-actions">
          <button type="button" onClick={onBack} className="btn-secondary">
            Retour
          </button>
          <button type="submit" className="btn-primary">
            Continuer
          </button>
        </div>
      </form>
    </div>
  );
};

export default AccountSetupStep;
