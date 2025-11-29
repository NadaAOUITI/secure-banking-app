// Validation configuration - centralized rules
export const VALIDATION_RULES = {
  email: {
    regex: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
    maxLength: 100,
    messages: {
      required: 'Email requis',
      invalid: 'Format email invalide',
      tooLong: 'Email trop long',
      exists: 'Un compte avec cet email existe déjà'
    }
  },
  
  name: {
    regex: /^[a-zA-ZÀ-ÿ\s'-]{2,50}$/,
    minLength: 2,
    maxLength: 50,
    messages: {
      required: (field) => `${field} requis`,
      invalid: (field) => `${field} invalide (lettres uniquement)`
    }
  },
  
  phone: {
    regex: /^\+?[1-9]\d{7,14}$/,
    messages: {
      required: 'Téléphone requis',
      invalid: 'Format téléphone invalide'
    }
  },
  
  age: {
    minAge: 18,
    messages: {
      required: 'Date requise',
      invalid: 'Date invalide',
      tooYoung: 'Vous devez avoir au moins 18 ans'
    }
  },
  
  address: {
    minLength: 5,
    maxLength: 200,
    messages: {
      tooShort: 'Adresse trop courte (min 5 caractères)',
      tooLong: 'Adresse trop longue'
    }
  },
  
  postalCode: {
    regex: /^[0-9]{4,10}$/,
    messages: {
      required: 'Code postal requis',
      invalid: 'Code postal invalide (chiffres uniquement)'
    }
  },
  
  document: {
    regex: /^[A-Z0-9]{6,20}$/,
    messages: {
      required: 'Numéro de document requis',
      invalid: 'Format de document invalide'
    }
  }
};