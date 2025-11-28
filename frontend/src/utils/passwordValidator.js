export const validatePassword = (password) => {
  const errors = [];
  
  // Critères de validation
  const minLength = 12;
  const hasUppercase = /[A-Z]/.test(password);
  const hasLowercase = /[a-z]/.test(password);
  const hasDigit = /\d/.test(password);
  const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password);
  
  // Vérifications
  if (password.length < minLength) {
    errors.push(`Au moins ${minLength} caractères`);
  }
  
  if (!hasUppercase) {
    errors.push('Au moins une lettre majuscule');
  }
  
  if (!hasLowercase) {
    errors.push('Au moins une lettre minuscule');
  }
  
  if (!hasDigit) {
    errors.push('Au moins un chiffre');
  }
  
  if (!hasSpecialChar) {
    errors.push('Au moins un caractère spécial (!@#$%^&*()_+-=[]{}|;\':"\\,.<>/?)');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

export const getPasswordStrength = (password) => {
  const validation = validatePassword(password);
  
  if (validation.isValid) {
    return 'strong';
  }
  
  if (password.length >= 8 && validation.errors.length <= 2) {
    return 'medium';
  }
  
  return 'weak';
};