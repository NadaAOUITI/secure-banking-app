import React, { useState } from 'react';
import './AddAccountModal.css';

const ProfileUpdateModal = ({ isOpen, onClose, onSuccess }) => {
  const [step, setStep] = useState('initiate'); // initiate, update
  const [formData, setFormData] = useState({
    otp: '',
    currentPassword: '',
    newPassword: '',
    confirmNewPassword: '',
    newEmail: '',
    newPhone: '',
    newAddress: '',
    newCountry: ''
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [otpTimer, setOtpTimer] = useState(0);

  const initiateUpdate = async () => {
    setLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/profile/initiate-update', {
        method: 'POST',
        credentials: 'include'
      });

      const data = await response.json();
      if (data.success) {
        setStep('update');
        setOtpTimer(60);
        const timer = setInterval(() => {
          setOtpTimer(prev => {
            if (prev <= 1) {
              clearInterval(timer);
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      } else {
        setErrors({ submit: data.message });
      }
    } catch (error) {
      setErrors({ submit: 'Erreur de connexion' });
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/profile/update', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(formData)
      });

      const data = await response.json();
      if (data.success) {
        onSuccess(data);
        if (data.logoutRequired) {
          // Redirection vers login après changement de mot de passe
          window.location.href = '/login';
        }
      } else {
        setErrors({ submit: data.message });
      }
    } catch (error) {
      setErrors({ submit: 'Erreur de connexion' });
    } finally {
      setLoading(false);
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.otp || !/^\d{6}$/.test(formData.otp)) {
      newErrors.otp = 'Code OTP requis (6 chiffres)';
    }

    // Validation changement de mot de passe
    if (formData.currentPassword || formData.newPassword) {
      if (!formData.currentPassword) {
        newErrors.currentPassword = 'Mot de passe actuel requis';
      }
      if (!formData.newPassword || formData.newPassword.length < 12) {
        newErrors.newPassword = 'Nouveau mot de passe requis (12+ caractères)';
      }
      if (formData.newPassword !== formData.confirmNewPassword) {
        newErrors.confirmNewPassword = 'Les mots de passe ne correspondent pas';
      }
    }

    // Validation email
    if (formData.newEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.newEmail)) {
      newErrors.newEmail = 'Format d\'email invalide';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const handleClose = () => {
    setFormData({
      otp: '', currentPassword: '', newPassword: '', confirmNewPassword: '',
      newEmail: '', newPhone: '', newAddress: '', newCountry: ''
    });
    setErrors({});
    setStep('initiate');
    setOtpTimer(0);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Modifier le profil</h2>
          <button className="close-btn" onClick={handleClose}>&times;</button>
        </div>

        {step === 'initiate' && (
          <div className="initiate-step">
            <p>Pour des raisons de sécurité, nous devons vérifier votre identité avant toute modification.</p>
            <p>Un code de sécurité sera envoyé à votre email.</p>
            
            {errors.submit && <div className="error-message">{errors.submit}</div>}
            
            <div className="modal-actions">
              <button type="button" onClick={handleClose} className="btn-secondary">
                Annuler
              </button>
              <button onClick={initiateUpdate} disabled={loading} className="btn-primary">
                {loading ? 'Envoi...' : 'Envoyer le code'}
              </button>
            </div>
          </div>
        )}

        {step === 'update' && (
          <form onSubmit={handleSubmit} className="add-account-form">
            <div className="form-group">
              <label htmlFor="otp">Code de sécurité *</label>
              <input
                type="text"
                id="otp"
                maxLength="6"
                value={formData.otp}
                onChange={(e) => handleInputChange('otp', e.target.value.replace(/\D/g, ''))}
                placeholder="123456"
                className={errors.otp ? 'error' : ''}
              />
              {errors.otp && <span className="error">{errors.otp}</span>}
              {otpTimer > 0 && (
                <small className="timer">Code expire dans {otpTimer}s</small>
              )}
            </div>

            <h3>Changement de mot de passe</h3>
            <div className="form-group">
              <label htmlFor="currentPassword">Mot de passe actuel</label>
              <input
                type="password"
                id="currentPassword"
                value={formData.currentPassword}
                onChange={(e) => handleInputChange('currentPassword', e.target.value)}
                className={errors.currentPassword ? 'error' : ''}
              />
              {errors.currentPassword && <span className="error">{errors.currentPassword}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="newPassword">Nouveau mot de passe</label>
              <input
                type="password"
                id="newPassword"
                value={formData.newPassword}
                onChange={(e) => handleInputChange('newPassword', e.target.value)}
                className={errors.newPassword ? 'error' : ''}
              />
              {errors.newPassword && <span className="error">{errors.newPassword}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="confirmNewPassword">Confirmer nouveau mot de passe</label>
              <input
                type="password"
                id="confirmNewPassword"
                value={formData.confirmNewPassword}
                onChange={(e) => handleInputChange('confirmNewPassword', e.target.value)}
                className={errors.confirmNewPassword ? 'error' : ''}
              />
              {errors.confirmNewPassword && <span className="error">{errors.confirmNewPassword}</span>}
            </div>

            <h3>Informations personnelles</h3>
            <div className="form-group">
              <label htmlFor="newEmail">Nouvel email</label>
              <input
                type="email"
                id="newEmail"
                value={formData.newEmail}
                onChange={(e) => handleInputChange('newEmail', e.target.value)}
                className={errors.newEmail ? 'error' : ''}
              />
              {errors.newEmail && <span className="error">{errors.newEmail}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="newPhone">Nouveau téléphone</label>
              <input
                type="tel"
                id="newPhone"
                value={formData.newPhone}
                onChange={(e) => handleInputChange('newPhone', e.target.value)}
                className={errors.newPhone ? 'error' : ''}
              />
              {errors.newPhone && <span className="error">{errors.newPhone}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="newAddress">Nouvelle adresse</label>
              <input
                type="text"
                id="newAddress"
                value={formData.newAddress}
                onChange={(e) => handleInputChange('newAddress', e.target.value)}
                className={errors.newAddress ? 'error' : ''}
              />
            </div>

            <div className="form-group">
              <label htmlFor="newCountry">Nouveau pays</label>
              <input
                type="text"
                id="newCountry"
                value={formData.newCountry}
                onChange={(e) => handleInputChange('newCountry', e.target.value)}
                className={errors.newCountry ? 'error' : ''}
              />
            </div>

            {errors.submit && <div className="error-message">{errors.submit}</div>}

            <div className="modal-actions">
              <button type="button" onClick={handleClose} className="btn-secondary">
                Annuler
              </button>
              <button type="submit" disabled={loading || otpTimer === 0} className="btn-primary">
                {loading ? 'Mise à jour...' : 'Mettre à jour'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default ProfileUpdateModal;