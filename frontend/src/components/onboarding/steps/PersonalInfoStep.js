import React, { useState } from 'react';
import { validateEmail, validateName, validatePhone, validateDate, checkEmailExists } from '../../../utils/validationUtils';
import './StepStyles.css';

const PersonalInfoStep = ({ data, onNext }) => {
  const [formData, setFormData] = useState({
    country: data.country || '',
    email: data.email || '',
    firstName: data.firstName || '',
    lastName: data.lastName || '',
    birthDate: data.birthDate || '',
    phone: data.phone || '',
    ...data
  });

  const [errors, setErrors] = useState({});

  const countries = [
    'France', 'Belgique', 'Suisse', 'Luxembourg', 'Canada', 'Maroc', 'Tunisie', 'Algérie'
  ];

  const validateForm = async () => {
    const newErrors = {};
    
    // Country validation
    if (!formData.country) {
      newErrors.country = 'Pays requis';
    }
    
    // Email validation
    const emailValidation = validateEmail(formData.email);
    if (!emailValidation.isValid) {
      newErrors.email = emailValidation.message;
    } else {
      // Check if email already exists
      const emailExists = await checkEmailExists(emailValidation.sanitized);
      if (emailExists) {
        newErrors.email = 'Un compte avec cet email existe déjà';
      }
    }
    
    // Name validations
    const firstNameValidation = validateName(formData.firstName, 'Prénom');
    if (!firstNameValidation.isValid) {
      newErrors.firstName = firstNameValidation.message;
    }
    
    const lastNameValidation = validateName(formData.lastName, 'Nom');
    if (!lastNameValidation.isValid) {
      newErrors.lastName = lastNameValidation.message;
    }
    
    // Phone validation
    const phoneValidation = validatePhone(formData.phone);
    if (!phoneValidation.isValid) {
      newErrors.phone = phoneValidation.message;
    }
    
    // Birth date validation
    const dateValidation = validateDate(formData.birthDate);
    if (!dateValidation.isValid) {
      newErrors.birthDate = dateValidation.message;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const isValid = await validateForm();
    if (isValid) {
      // Sanitize data before sending
      const sanitizedData = {
        country: formData.country,
        email: validateEmail(formData.email).sanitized,
        firstName: validateName(formData.firstName, 'Prénom').sanitized,
        lastName: validateName(formData.lastName, 'Nom').sanitized,
        phone: validatePhone(formData.phone).sanitized,
        birthDate: validateDate(formData.birthDate).sanitized
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

  return (
    <div className="step-container">
      <h2>Informations personnelles</h2>
      <p>Commençons par vos informations de base</p>

      <form onSubmit={handleSubmit} className="step-form">
        <div className="form-group">
          <label>Pays de résidence *</label>
          <select 
            name="country" 
            value={formData.country} 
            onChange={handleChange}
            className={errors.country ? 'error' : ''}
          >
            <option value="">Sélectionnez votre pays</option>
            {countries.map(country => (
              <option key={country} value={country}>{country}</option>
            ))}
          </select>
          {errors.country && <span className="error-text">{errors.country}</span>}
        </div>

        <div className="form-group">
          <label>Adresse email *</label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="votre.email@exemple.com"
            className={errors.email ? 'error' : ''}
          />
          {errors.email && <span className="error-text">{errors.email}</span>}
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Prénom *</label>
            <input
              type="text"
              name="firstName"
              value={formData.firstName}
              onChange={handleChange}
              className={errors.firstName ? 'error' : ''}
            />
            {errors.firstName && <span className="error-text">{errors.firstName}</span>}
          </div>

          <div className="form-group">
            <label>Nom *</label>
            <input
              type="text"
              name="lastName"
              value={formData.lastName}
              onChange={handleChange}
              className={errors.lastName ? 'error' : ''}
            />
            {errors.lastName && <span className="error-text">{errors.lastName}</span>}
          </div>
        </div>

        <div className="form-group">
          <label>Date de naissance *</label>
          <input
            type="date"
            name="birthDate"
            value={formData.birthDate}
            onChange={handleChange}
            className={errors.birthDate ? 'error' : ''}
          />
          {errors.birthDate && <span className="error-text">{errors.birthDate}</span>}
        </div>

        <div className="form-group">
          <label>Numéro de téléphone *</label>
          <input
            type="tel"
            name="phone"
            value={formData.phone}
            onChange={handleChange}
            placeholder="+33 1 23 45 67 89"
            className={errors.phone ? 'error' : ''}
          />
          {errors.phone && <span className="error-text">{errors.phone}</span>}
        </div>

        <div className="step-actions">
          <button type="submit" className="btn-primary">
            Continuer
          </button>
        </div>
      </form>
    </div>
  );
};

export default PersonalInfoStep;
