import React, { useState, useEffect } from 'react';
import './StepStyles.css';

const EmailVerificationStep = ({ data, onNext, onBack, allData }) => {
  const [verificationCode, setVerificationCode] = useState('');
  const [isCodeSent, setIsCodeSent] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [error, setError] = useState('');

  useEffect(() => {
    if (countdown > 0) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [countdown]);

  const sendVerificationCode = async () => {
    try {
      const response = await fetch('https://localhost:8443/api/onboarding/send-verification', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email: allData.personalInfo.email })
      });
      
      const data = await response.json();
      
      if (data.success) {
        setIsCodeSent(true);
        setCountdown(60);
        setError('');
      } else {
        setError(data.message || 'Erreur lors de l\'envoi du code');
      }
    } catch (error) {
      setError('Erreur de connexion. Veuillez réessayer.');
    }
  };

  const resendCode = () => {
    sendVerificationCode();
  };

  const handleVerify = async (e) => {
    e.preventDefault();
    
    if (verificationCode.length !== 6) {
      setError('Le code doit contenir 6 chiffres');
      return;
    }

    try {
      const response = await fetch('https://localhost:8080/api/onboarding/verify-email', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          email: allData.personalInfo.email,
          otpCode: verificationCode 
        })
      });
      
      const data = await response.json();
      
      if (data.success) {
        onNext({ verified: true, code: verificationCode });
      } else {
        setError(data.message || 'Code incorrect. Veuillez réessayer.');
      }
    } catch (error) {
      setError('Erreur de connexion. Veuillez réessayer.');
    }
  };

  return (
    <div className="step-container">
      <h2>Vérification de votre email</h2>
      <p>Nous devons vérifier votre adresse email : <strong>{allData.personalInfo.email}</strong></p>

      {!isCodeSent ? (
        <div className="verification-start">
          <div className="info-box">
            <p>Un code de vérification à 6 chiffres sera envoyé à votre adresse email.</p>
          </div>
          <div className="step-actions">
            <button onClick={onBack} className="btn-secondary">
              Retour
            </button>
            <button onClick={sendVerificationCode} className="btn-primary">
              Envoyer le code
            </button>
          </div>
        </div>
      ) : (
        <form onSubmit={handleVerify} className="step-form">
          <div className="verification-sent">
            <div className="success-message">
              Code envoyé à {allData.personalInfo.email}
            </div>
            
            <div className="form-group">
              <label>Code de vérification</label>
              <input
                type="text"
                value={verificationCode}
                onChange={(e) => setVerificationCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                placeholder="123456"
                className={`verification-input ${error ? 'error' : ''}`}
                maxLength="6"
              />
              {error && <span className="error-text">{error}</span>}
            </div>

            <div className="resend-section">
              {countdown > 0 ? (
                <p>Renvoyer le code dans {countdown}s</p>
              ) : (
                <button type="button" onClick={resendCode} className="btn-link">
                  Renvoyer le code
                </button>
              )}
            </div>

            <div className="step-actions">
              <button type="button" onClick={onBack} className="btn-secondary">
                Retour
              </button>
              <button type="submit" className="btn-primary">
                Vérifier
              </button>
            </div>
          </div>
        </form>
      )}
    </div>
  );
};

export default EmailVerificationStep;
