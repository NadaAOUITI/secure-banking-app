class InputValidator {
  
  static sanitizeInput(input) {
    if (!input || typeof input !== 'string') return input;
    
    return input
      .trim()
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
      .replace(/&/g, '&amp;')
      .replace(/javascript:/gi, '')
      .replace(/<script[^>]*>.*?<\/script>/gi, '');
  }
  
  static validateEmail(email) {
    const emailRegex = /^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\.[A-Za-z]{2,})$/;
    const sanitized = this.sanitizeInput(email);
    
    if (!sanitized || sanitized.length > 100) {
      return { valid: false, message: 'Email invalide', sanitized: null };
    }
    
    if (!emailRegex.test(sanitized)) {
      return { valid: false, message: 'Format email invalide', sanitized: null };
    }
    
    return { valid: true, message: 'Email valide', sanitized: sanitized.toLowerCase() };
  }
  
  static validatePassword(password) {
    if (!password || password.length < 12 || password.length > 128) {
      return { valid: false, message: 'Mot de passe doit contenir entre 12 et 128 caractères' };
    }
    
    const hasUpper = /[A-Z]/.test(password);
    const hasLower = /[a-z]/.test(password);
    const hasNumber = /\d/.test(password);
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);
    
    if (!hasUpper || !hasLower || !hasNumber || !hasSpecial) {
      return { valid: false, message: 'Mot de passe doit contenir majuscules, minuscules, chiffres et caractères spéciaux' };
    }
    
    return { valid: true, message: 'Mot de passe valide' };
  }
  
  static validatePhone(phone) {
    const phoneRegex = /^\+?[1-9]\d{1,14}$/;
    const sanitized = this.sanitizeInput(phone);
    
    if (!phoneRegex.test(sanitized)) {
      return { valid: false, message: 'Format téléphone invalide', sanitized: null };
    }
    
    return { valid: true, message: 'Téléphone valide', sanitized };
  }
  
  static validatePin(pin) {
    const pinRegex = /^\d{4}$/;
    
    if (!pinRegex.test(pin)) {
      return { valid: false, message: 'PIN doit contenir exactement 4 chiffres' };
    }
    
    // Check for weak PINs
    const weakPins = ['0000', '1111', '2222', '3333', '4444', '5555', '6666', '7777', '8888', '9999', '1234', '4321'];
    if (weakPins.includes(pin)) {
      return { valid: false, message: 'PIN trop faible, choisissez un autre code' };
    }
    
    return { valid: true, message: 'PIN valide' };
  }
  
  static validateText(text, maxLength = 255) {
    const sanitized = this.sanitizeInput(text);
    
    if (!sanitized) {
      return { valid: false, message: 'Texte requis', sanitized: null };
    }
    
    if (sanitized.length > maxLength) {
      return { valid: false, message: `Texte trop long (max ${maxLength} caractères)`, sanitized: null };
    }
    
    // Check for injection attempts
    if (this.containsInjectionAttempt(sanitized)) {
      return { valid: false, message: 'Contenu invalide détecté', sanitized: null };
    }
    
    return { valid: true, message: 'Texte valide', sanitized };
  }
  
  static containsInjectionAttempt(input) {
    const injectionPatterns = [
      /union.*select/i,
      /insert.*into/i,
      /delete.*from/i,
      /update.*set/i,
      /drop.*table/i,
      /<script/i,
      /javascript:/i,
      /vbscript:/i,
      /onload=/i,
      /onerror=/i
    ];
    
    return injectionPatterns.some(pattern => pattern.test(input));
  }
  
  static sanitizeFormData(formData) {
    const sanitized = {};
    
    for (const [key, value] of Object.entries(formData)) {
      if (typeof value === 'string') {
        sanitized[key] = this.sanitizeInput(value);
      } else {
        sanitized[key] = value;
      }
    }
    
    return sanitized;
  }
}

export default InputValidator;
