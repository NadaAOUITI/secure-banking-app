import React, { useState, useEffect } from 'react';
import InputValidator from '../../utils/inputValidator';

const SecureInput = ({ 
  type = 'text', 
  value, 
  onChange, 
  validationType, 
  placeholder, 
  required = false,
  className = '',
  ...props 
}) => {
  const [validationResult, setValidationResult] = useState({ valid: true, message: '' });
  const [sanitizedValue, setSanitizedValue] = useState(value || '');

  const handleChange = (e) => {
    const inputValue = e.target.value;
    let validation = { valid: true, message: '', sanitized: inputValue };

    switch (validationType) {
      case 'email':
        validation = InputValidator.validateEmail(inputValue);
        break;
      case 'password':
        validation = InputValidator.validatePassword(inputValue);
        break;
      case 'phone':
        validation = InputValidator.validatePhone(inputValue);
        break;
      case 'pin':
        validation = InputValidator.validatePin(inputValue);
        break;
      case 'text':
        validation = InputValidator.validateText(inputValue);
        break;
      default:
        validation.sanitized = InputValidator.sanitizeInput(inputValue);
    }

    setValidationResult(validation);
    
    if (validation.valid && validation.sanitized !== undefined) {
      setSanitizedValue(validation.sanitized);
      onChange && onChange({
        target: {
          value: validation.sanitized,
          name: e.target.name
        }
      });
    }
  };

  return (
    <div className="w-full">
      <input
        type={type}
        value={sanitizedValue}
        onChange={handleChange}
        placeholder={placeholder}
        required={required}
        className={className}
        autoComplete="off"
        spellCheck="false"
        {...props}
      />
      {!validationResult.valid && (
        <div className="text-red-500 text-sm mt-1">
          {validationResult.message}
        </div>
      )}
    </div>
  );
};

export default SecureInput;
