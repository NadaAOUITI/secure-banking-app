import React, { useState } from 'react';
import { validateAddress, validateName, validatePostalCode, validateDocumentNumber, sanitizeInput } from '../../../utils/validationUtils';
import './StepStyles.css';

const IdentityVerificationStep = ({ data, onNext, onBack }) => {
  const [formData, setFormData] = useState({
    documentType: data.documentType || '',
    documentNumber: data.documentNumber || '',
    documentFile: data.documentFile || null,
    address: data.address || '',
    city: data.city || '',
    postalCode: data.postalCode || '',
    ...data
  });

  const [errors, setErrors] = useState({});

  const documentTypes = [
    { value: 'passport', label: 'Passeport' },
    { value: 'id_card', label: 'Carte d\'identité' }
  ];

  const validateForm = () => {
    const newErrors = {};
    
    // Document type validation
    if (!formData.documentType) {
      newErrors.documentType = 'Type de document requis';
    }
    
    // Document number validation
    const docValidation = validateDocumentNumber(formData.documentNumber);
    if (!docValidation.isValid) {
      newErrors.documentNumber = docValidation.message;
    }
    
    // Address validation
    const addressValidation = validateAddress(formData.address);
    if (!addressValidation.isValid) {
      newErrors.address = addressValidation.message;
    }
    
    // City validation
    const cityValidation = validateName(formData.city, 'Ville');
    if (!cityValidation.isValid) {
      newErrors.city = cityValidation.message;
    }
    
    // Postal code validation
    const postalValidation = validatePostalCode(formData.postalCode);
    if (!postalValidation.isValid) {
      newErrors.postalCode = postalValidation.message;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validateForm()) {
      // Sanitize data before sending
      const sanitizedData = {
        documentType: sanitizeInput(formData.documentType),
        documentNumber: validateDocumentNumber(formData.documentNumber).sanitized,
        address: validateAddress(formData.address).sanitized,
        city: validateName(formData.city, 'Ville').sanitized,
        postalCode: validatePostalCode(formData.postalCode).sanitized
      };
      
      onNext(sanitizedData);
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleFileChange = (e) => {
    setFormData({
      ...formData,
      documentFile: e.target.files[0]
    });
  };

  return (
    <div className="step-container">
      <h2>Vérification d'identité</h2>
      <p>Pour votre sécurité, nous devons vérifier votre identité</p>

      <form onSubmit={handleSubmit} className="step-form">
        <div className="form-group">
          <label>Type de document d'identité *</label>
          <select 
            name="documentType" 
            value={formData.documentType} 
            onChange={handleChange}
            className={errors.documentType ? 'error' : ''}
          >
            <option value="">Sélectionnez un document</option>
            {documentTypes.map(doc => (
              <option key={doc.value} value={doc.value}>{doc.label}</option>
            ))}
          </select>
          {errors.documentType && <span className="error-text">{errors.documentType}</span>}
        </div>

        <div className="form-group">
          <label>Numéro du document *</label>
          <input
            type="text"
            name="documentNumber"
            value={formData.documentNumber}
            onChange={handleChange}
            placeholder="Numéro de votre document"
            className={errors.documentNumber ? 'error' : ''}
          />
          {errors.documentNumber && <span className="error-text">{errors.documentNumber}</span>}
        </div>



        <h3>Adresse de résidence</h3>
        
        <div className="form-group">
          <label>Adresse complète *</label>
          <input
            type="text"
            name="address"
            value={formData.address}
            onChange={handleChange}
            placeholder="123 Rue de la Paix"
            className={errors.address ? 'error' : ''}
          />
          {errors.address && <span className="error-text">{errors.address}</span>}
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Ville *</label>
            <input
              type="text"
              name="city"
              value={formData.city}
              onChange={handleChange}
              className={errors.city ? 'error' : ''}
            />
            {errors.city && <span className="error-text">{errors.city}</span>}
          </div>

          <div className="form-group">
            <label>Code postal *</label>
            <input
              type="text"
              name="postalCode"
              value={formData.postalCode}
              onChange={handleChange}
              className={errors.postalCode ? 'error' : ''}
            />
            {errors.postalCode && <span className="error-text">{errors.postalCode}</span>}
          </div>
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

export default IdentityVerificationStep;
