import React, { useState } from 'react';
import { validateAmount, sanitizeInput } from '../utils/validationUtils';
import './AddAccountModal.css';

const AddAccountModal = ({ isOpen, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    accountType: '',
    initialDeposit: ''
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const accountTypes = [
    { value: 'CURRENT', label: 'Compte Courant', description: 'Pour vos opérations quotidiennes' },
    { value: 'SAVINGS', label: 'Compte Épargne', description: 'Pour épargner avec intérêts' },
    { value: 'PREMIUM', label: 'Compte Premium', description: 'Services bancaires premium' }
  ];

  const validateForm = () => {
    const newErrors = {};

    if (!formData.accountType) {
      newErrors.accountType = 'Veuillez sélectionner un type de compte';
    }

    const amountValidation = validateAmount(formData.initialDeposit, 300, 1000000);
    if (!amountValidation.isValid) {
      newErrors.initialDeposit = amountValidation.message;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);
    
    try {
      const response = await fetch('http://localhost:8080/api/accounts/add', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          accountType: sanitizeInput(formData.accountType),
          initialDeposit: parseFloat(formData.initialDeposit)
        })
      });

      const data = await response.json();

      if (data.success) {
        onSuccess(data);
        onClose();
        setFormData({ accountType: '', initialDeposit: '' });
      } else {
        setErrors({ submit: data.message });
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
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Ajouter un nouveau compte</h2>
          <button className="close-btn" onClick={onClose}>&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="add-account-form">
          <div className="form-group">
            <label>Type de compte *</label>
            <div className="account-type-options">
              {accountTypes.map(type => (
                <div 
                  key={type.value}
                  className={`account-type-card ${formData.accountType === type.value ? 'selected' : ''}`}
                  onClick={() => handleInputChange('accountType', type.value)}
                >
                  <h4>{type.label}</h4>
                  <p>{type.description}</p>
                </div>
              ))}
            </div>
            {errors.accountType && <span className="error">{errors.accountType}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="initialDeposit">Dépôt initial (TND) *</label>
            <input
              type="number"
              id="initialDeposit"
              min="300"
              max="1000000"
              step="0.001"
              value={formData.initialDeposit}
              onChange={(e) => handleInputChange('initialDeposit', e.target.value)}
              placeholder="Minimum 300 TND"
              className={errors.initialDeposit ? 'error' : ''}
            />
            {errors.initialDeposit && <span className="error">{errors.initialDeposit}</span>}
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

export default AddAccountModal;