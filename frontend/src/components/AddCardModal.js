import React, { useState } from 'react';
import { sanitizeInput } from '../utils/validationUtils';
import './AddAccountModal.css';

const AddCardModal = ({ isOpen, onClose, onSuccess, accounts }) => {
  const [formData, setFormData] = useState({
    accountId: '',
    cardType: '',
    pin: '',
    password: ''
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [remainingAttempts, setRemainingAttempts] = useState(null);

  const cardTypes = [
    { id: 'CLASSIC', name: 'Carte Classique', description: 'Paiements et retraits', fee: 'Gratuite' },
    { id: 'GOLD', name: 'Carte Gold', description: 'Avantages et assurances', fee: '150 TND/an' },
    { id: 'PLATINUM', name: 'Carte Platinum', description: 'Services premium', fee: '450 TND/an' }
  ];

  const validateForm = () => {
    const newErrors = {};

    if (!formData.accountId) {
      newErrors.accountId = 'Veuillez sélectionner un compte';
    }

    if (!formData.cardType) {
      newErrors.cardType = 'Veuillez sélectionner un type de carte';
    }

    if (!formData.pin || !/^\d{4}$/.test(formData.pin)) {
      newErrors.pin = 'Code PIN doit contenir exactement 4 chiffres';
    } else {
      // Enhanced PIN validation
      if (/^(.)\1{3}$/.test(formData.pin)) {
        newErrors.pin = 'Code PIN trop faible (chiffres répétés)';
      } else if (/^(0123|1234|2345|3456|4567|5678|6789|9876|8765|7654|6543|5432|4321|3210)$/.test(formData.pin)) {
        newErrors.pin = 'Code PIN trop faible (séquence détectée)';
      }
    }

    if (!formData.password) {
      newErrors.password = 'Mot de passe requis pour cette opération';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);
    
    try {
      const response = await fetch('https://localhost:8080/api/cards/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          accountId: parseInt(formData.accountId),
          cardType: sanitizeInput(formData.cardType),
          pin: formData.pin,
          password: formData.password
        })
      });

      const data = await response.json();

      if (data.success) {
        setFormData({ accountId: '', cardType: '', pin: '', password: '' });
        setRemainingAttempts(null);
        onSuccess(data);
      } else {
        setErrors({ submit: data.message });
        if (data.remainingAttempts !== undefined) {
          setRemainingAttempts(data.remainingAttempts);
        }
      }
    } catch (error) {
      setErrors({ submit: 'Erreur de connexion. Veuillez réessayer.' });
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
    // Clear remaining attempts when user starts typing
    if (field === 'password' && remainingAttempts !== null) {
      setRemainingAttempts(null);
    }
  };

  const handleClose = () => {
    setFormData({ accountId: '', cardType: '', pin: '', password: '' });
    setErrors({});
    setRemainingAttempts(null);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Ajouter une nouvelle carte</h2>
          <button className="close-btn" onClick={handleClose}>&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="add-account-form">
          <div className="form-group">
            <label htmlFor="accountId">Compte bancaire *</label>
            <select
              id="accountId"
              value={formData.accountId}
              onChange={(e) => handleInputChange('accountId', e.target.value)}
              className={errors.accountId ? 'error' : ''}
            >
              <option value="">Sélectionner un compte</option>
              {accounts?.map(account => {
                const maskedAccountNumber = account.accountNumber ? 
                  account.accountNumber.slice(0, 4) + '****' + account.accountNumber.slice(-4) : 
                  '****';
                return (
                  <option key={account.accountNumber} value={account.id || account.accountId}>
                    {account.accountType} - {maskedAccountNumber}
                  </option>
                );
              })}
            </select>
            {errors.accountId && <span className="error">{errors.accountId}</span>}
          </div>

          <h3>Type de carte *</h3>
          <div className="product-grid">
            {cardTypes.map(card => (
              <div 
                key={card.id}
                className={`product-card ${formData.cardType === card.id ? 'selected' : ''}`}
                onClick={() => handleInputChange('cardType', card.id)}
              >
                <h4>{card.name}</h4>
                <p>{card.description}</p>
                <div className="price">{card.fee}</div>
              </div>
            ))}
          </div>
          {errors.cardType && <span className="error">{errors.cardType}</span>}

          <div className="form-group">
            <label htmlFor="pin">Code PIN (4 chiffres) *</label>
            <input
              type="password"
              id="pin"
              maxLength="4"
              value={formData.pin}
              onChange={(e) => handleInputChange('pin', e.target.value.replace(/\D/g, ''))}
              placeholder="••••"
              className={errors.pin ? 'error' : ''}
            />
            {errors.pin && <span className="error">{errors.pin}</span>}
            <small className="security-hint">
              Évitez les codes PIN faibles (1234, 0000, etc.)
            </small>
          </div>

          <div className="form-group">
            <label htmlFor="password">Mot de passe (confirmation requise) *</label>
            <input
              type="password"
              id="password"
              value={formData.password}
              onChange={(e) => handleInputChange('password', e.target.value)}
              placeholder="Confirmez votre mot de passe"
              className={errors.password ? 'error' : ''}
            />
            {errors.password && <span className="error">{errors.password}</span>}
          </div>

          {errors.submit && <div className="error-message">{errors.submit}</div>}
          {remainingAttempts !== null && remainingAttempts > 0 && (
            <div className="warning-message">
              ⚠️ Tentatives restantes: {remainingAttempts}
            </div>
          )}
          {remainingAttempts === 0 && (
            <div className="error-message">
              🔒 Compte temporairement verrouillé. Réessayez dans 15 minutes.
            </div>
          )}


          <div className="modal-actions">
            <button type="button" onClick={handleClose} className="btn-secondary">
              Annuler
            </button>
            <button type="submit" disabled={loading || remainingAttempts === 0} className="btn-primary">
              {loading ? 'Ajout...' : 'Ajouter la carte'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddCardModal;
