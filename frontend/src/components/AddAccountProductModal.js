import React, { useState } from 'react';
import { sanitizeInput } from '../utils/validationUtils';
import './AddAccountModal.css';

const AddAccountProductModal = ({ isOpen, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    accountType: '',
    cards: [],
    cardPins: {},
    initialDeposit: '',
    password: ''
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const accountTypes = [
    { id: 'CURRENT', name: 'Compte Courant', description: 'Pour vos opérations quotidiennes', monthlyFee: '15 TND' },
    { id: 'SAVINGS', name: 'Compte Épargne', description: 'Pour faire fructifier votre argent', monthlyFee: 'Gratuit' },
    { id: 'PREMIUM', name: 'Compte Premium', description: 'Tous les avantages inclus', monthlyFee: '45 TND' }
  ];

  const cardOptions = [
    { id: 'CLASSIC', name: 'Carte Classique', description: 'Paiements et retraits', annualFee: 'Gratuite' },
    { id: 'GOLD', name: 'Carte Gold', description: 'Avantages et assurances', annualFee: '150 TND' },
    { id: 'PLATINUM', name: 'Carte Platinum', description: 'Services premium', annualFee: '450 TND' }
  ];

  const handleAccountTypeChange = (accountType) => {
    setFormData(prev => ({ ...prev, accountType }));
    if (errors.accountType) setErrors(prev => ({ ...prev, accountType: '' }));
  };

  const handleCardChange = (cardId) => {
    const updatedCards = formData.cards.includes(cardId)
      ? formData.cards.filter(id => id !== cardId)
      : [...formData.cards, cardId];
    
    const updatedPins = { ...formData.cardPins };
    if (!updatedCards.includes(cardId)) {
      delete updatedPins[cardId];
    }
    
    setFormData(prev => ({ ...prev, cards: updatedCards, cardPins: updatedPins }));
  };

  const handlePinChange = (cardId, pin) => {
    setFormData(prev => ({
      ...prev,
      cardPins: { ...prev.cardPins, [cardId]: pin }
    }));
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.accountType) {
      newErrors.accountType = 'Veuillez sélectionner un type de compte';
    }
    
    if (!formData.password) {
      newErrors.password = 'Mot de passe requis pour cette opération';
    }

    if (formData.initialDeposit && parseFloat(formData.initialDeposit) < 300) {
      newErrors.initialDeposit = 'Le dépôt initial doit être d\'au moins 300 TND';
    }

    for (const cardId of formData.cards) {
      const pin = formData.cardPins[cardId];
      if (!pin || pin.length !== 4 || !/^\d{4}$/.test(pin)) {
        newErrors[`pin_${cardId}`] = 'Code PIN invalide (4 chiffres requis)';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);
    
    try {
      const requestData = {
        accountType: sanitizeInput(formData.accountType),
        initialDeposit: formData.initialDeposit ? parseFloat(formData.initialDeposit) : null,
        selectedCards: formData.cards.length > 0 ? formData.cards : null,
        cardPins: formData.cards.length > 0 ? formData.cards.map(cardId => formData.cardPins[cardId]) : null,
        password: formData.password
      };

      const response = await fetch('https://localhost:8443/api/accounts/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(requestData)
      });

      const data = await response.json();

      if (data.success) {
        onSuccess(data);
        onClose();
        setFormData({ accountType: '', cards: [], cardPins: {}, initialDeposit: '', password: '' });
      } else {
        setErrors({ submit: data.message });
      }
    } catch (error) {
      setErrors({ submit: 'Erreur de connexion. Veuillez réessayer.' });
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content product-modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Ajouter un nouveau compte</h2>
          <button className="close-btn" onClick={onClose}>&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="add-account-form">
          <h3>Type de compte *</h3>
          <div className="product-grid">
            {accountTypes.map(account => (
              <div 
                key={account.id}
                className={`product-card ${formData.accountType === account.id ? 'selected' : ''}`}
                onClick={() => handleAccountTypeChange(account.id)}
              >
                <h4>{account.name}</h4>
                <p>{account.description}</p>
                <div className="price">{account.monthlyFee}/mois</div>
              </div>
            ))}
          </div>
          {errors.accountType && <span className="error">{errors.accountType}</span>}

          <h3>Cartes bancaires (optionnel)</h3>
          <div className="product-grid">
            {cardOptions.map(card => (
              <div key={card.id}>
                <div 
                  className={`product-card ${formData.cards.includes(card.id) ? 'selected' : ''}`}
                  onClick={() => handleCardChange(card.id)}
                >
                  <h4>{card.name}</h4>
                  <p>{card.description}</p>
                  <div className="price">{card.annualFee}/an</div>
                </div>
                
                {formData.cards.includes(card.id) && (
                  <div className="pin-input-section">
                    <label>Code PIN (4 chiffres) *</label>
                    <input
                      type="password"
                      maxLength="4"
                      pattern="\d{4}"
                      value={formData.cardPins[card.id] || ''}
                      onChange={(e) => handlePinChange(card.id, e.target.value)}
                      placeholder="••••"
                      className="pin-input"
                      onClick={(e) => e.stopPropagation()}
                    />
                    {errors[`pin_${card.id}`] && <span className="error">{errors[`pin_${card.id}`]}</span>}
                  </div>
                )}
              </div>
            ))}
          </div>

          <div className="form-group">
            <label htmlFor="initialDeposit">Dépôt initial (optionnel)</label>
            <input
              type="number"
              id="initialDeposit"
              min="300"
              step="0.001"
              value={formData.initialDeposit}
              onChange={(e) => setFormData(prev => ({ ...prev, initialDeposit: e.target.value }))}
              placeholder="Minimum 300 TND"
            />
            {errors.initialDeposit && <span className="error">{errors.initialDeposit}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="password">Mot de passe (confirmation requise) *</label>
            <input
              type="password"
              id="password"
              value={formData.password}
              onChange={(e) => setFormData(prev => ({ ...prev, password: e.target.value }))}
              placeholder="Confirmez votre mot de passe"
              className={errors.password ? 'error' : ''}
            />
            {errors.password && <span className="error">{errors.password}</span>}
          </div>

          {errors.submit && <div className="error-message">{errors.submit}</div>}

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn-secondary">
              Annuler
            </button>
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Création...' : 'Créer le compte'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddAccountProductModal;
