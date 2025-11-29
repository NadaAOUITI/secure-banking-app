// Input sanitization and validation utilities
export const sanitizeInput = (input) => {
  if (typeof input !== 'string') return input;
  
  return input
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '') // Remove script tags
    .replace(/javascript:/gi, '') // Remove javascript: protocol
    .replace(/on\w+\s*=/gi, '') // Remove event handlers
    .trim();
};

export const validateEmail = (email) => {
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9-]+\.[a-zA-Z]{2,}$/;
  const sanitized = sanitizeInput(email);
  
  // Additional checks for proper email structure
  const hasAtSymbol = sanitized.includes('@');
  const parts = sanitized.split('@');
  const hasDotInDomain = parts.length === 2 && parts[1].includes('.');
  
  if (!sanitized || sanitized.length === 0) {
    return { isValid: false, message: 'Email requis' };
  }
  
  if (!hasAtSymbol || !hasDotInDomain || !emailRegex.test(sanitized)) {
    return { isValid: false, message: 'Format email invalide' };
  }
  
  // Check for consecutive dots or dots at start/end
  if (sanitized.includes('..') || sanitized.startsWith('.') || sanitized.endsWith('.')) {
    return { isValid: false, message: 'Format email invalide' };
  }
  
  if (sanitized.length > 100) {
    return { isValid: false, message: 'Email trop long' };
  }
  
  return { isValid: true, sanitized };
};

export const validateName = (name, fieldName) => {
  const nameRegex = /^[a-zA-ZÀ-ÿ\s'-]{2,50}$/;
  const sanitized = sanitizeInput(name);
  
  if (!sanitized || sanitized.length === 0) {
    return { isValid: false, message: `${fieldName} requis` };
  }
  
  if (!nameRegex.test(sanitized)) {
    return { isValid: false, message: `${fieldName} invalide (lettres uniquement)` };
  }
  
  return { isValid: true, sanitized };
};

export const validatePhone = (phone) => {
  const phoneRegex = /^\+?[1-9]\d{7,14}$/;
  const sanitized = sanitizeInput(phone).replace(/\s/g, '');
  
  if (!sanitized || sanitized.length === 0) {
    return { isValid: false, message: 'Téléphone requis' };
  }
  
  if (!phoneRegex.test(sanitized)) {
    return { isValid: false, message: 'Format téléphone invalide' };
  }
  
  return { isValid: true, sanitized };
};

export const validateDate = (date) => {
  const sanitized = sanitizeInput(date);
  
  if (!sanitized) {
    return { isValid: false, message: 'Date requise' };
  }
  
  const dateObj = new Date(sanitized);
  const today = new Date();
  const minAge = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
  
  if (isNaN(dateObj.getTime())) {
    return { isValid: false, message: 'Date invalide' };
  }
  
  if (dateObj > minAge) {
    return { isValid: false, message: 'Vous devez avoir au moins 18 ans' };
  }
  
  return { isValid: true, sanitized };
};

export const validateAddress = (address) => {
  const sanitized = sanitizeInput(address);
  
  if (!sanitized || sanitized.length < 5) {
    return { isValid: false, message: 'Adresse trop courte (min 5 caractères)' };
  }
  
  if (sanitized.length > 200) {
    return { isValid: false, message: 'Adresse trop longue' };
  }
  
  return { isValid: true, sanitized };
};

export const validatePostalCode = (postalCode) => {
  const sanitized = sanitizeInput(postalCode);
  const postalRegex = /^[0-9]{4,10}$/;
  
  if (!sanitized) {
    return { isValid: false, message: 'Code postal requis' };
  }
  
  if (!postalRegex.test(sanitized)) {
    return { isValid: false, message: 'Code postal invalide (chiffres uniquement)' };
  }
  
  return { isValid: true, sanitized };
};

export const validateDocumentNumber = (docNumber) => {
  const sanitized = sanitizeInput(docNumber);
  const docRegex = /^[A-Z0-9]{6,20}$/;
  
  if (!sanitized) {
    return { isValid: false, message: 'Numéro de document requis' };
  }
  
  if (!docRegex.test(sanitized.toUpperCase())) {
    return { isValid: false, message: 'Format de document invalide' };
  }
  
  return { isValid: true, sanitized: sanitized.toUpperCase() };
};

export const validateAmount = (amount, minAmount = 0, maxAmount = Number.MAX_SAFE_INTEGER) => {
  const sanitized = sanitizeInput(String(amount));
  const numAmount = parseFloat(sanitized);
  
  if (!sanitized || sanitized === '') {
    return { isValid: false, message: 'Montant requis' };
  }
  
  if (isNaN(numAmount)) {
    return { isValid: false, message: 'Montant invalide' };
  }
  
  if (numAmount < minAmount) {
    return { isValid: false, message: `Montant minimum: ${minAmount} TND` };
  }
  
  if (numAmount > maxAmount) {
    return { isValid: false, message: `Montant maximum: ${maxAmount.toLocaleString()} TND` };
  }
  
  // Check for reasonable decimal places (max 3 for TND)
  const decimalPlaces = (sanitized.split('.')[1] || '').length;
  if (decimalPlaces > 3) {
    return { isValid: false, message: 'Maximum 3 décimales autorisées' };
  }
  
  return { isValid: true, sanitized: numAmount };
};

export const checkEmailExists = async (email) => {
  try {
    const response = await fetch(`http://localhost:8080/api/auth/check-email?email=${encodeURIComponent(email)}`);
    const data = await response.json();
    return data.exists;
  } catch (error) {
    console.error('Error checking email:', error);
    return false;
  }
};