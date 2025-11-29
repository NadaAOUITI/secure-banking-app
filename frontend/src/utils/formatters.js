export const formatCurrency = (amount, currency = 'TND') => {
  return new Intl.NumberFormat('fr-TN', {
    style: 'currency',
    currency: currency
  }).format(amount);
};

export const maskAccountNumber = (accountNumber) => {
  if (!accountNumber || accountNumber === 'Aucun compte') return accountNumber;
  if (accountNumber.length < 10) return accountNumber;
  return accountNumber.substring(0, 6) + '****' + accountNumber.substring(accountNumber.length - 4);
};

export const maskEmail = (email) => {
  if (!email || !email.includes('@')) return email;
  const [local, domain] = email.split('@');
  if (local.length <= 2) return email;
  return local.charAt(0) + '***' + local.charAt(local.length - 1) + '@' + domain;
};

export const maskPhone = (phone) => {
  if (!phone || phone.length <= 4) return phone;
  return phone.substring(0, 2) + '***' + phone.substring(phone.length - 2);
};

export const maskCardNumber = (cardNumber) => {
  if (!cardNumber || cardNumber.length < 8) return cardNumber;
  const cleaned = cardNumber.replace(/\s/g, '');
  const lastFour = cleaned.substring(cleaned.length - 4);
  return '**** **** **** ' + lastFour;
};