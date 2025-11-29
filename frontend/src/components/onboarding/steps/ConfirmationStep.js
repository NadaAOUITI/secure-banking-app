import React, { useState } from 'react';
import './StepStyles.css';

const ConfirmationStep = ({ allData, onBack }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isCompleted, setIsCompleted] = useState(false);

  const handleFinalSubmit = async () => {
    setIsSubmitting(true);
    
    try {
      // Prepare registration data
      const registrationData = {
        // Personal info
        email: allData.personalInfo.email,
        firstName: allData.personalInfo.firstName,
        lastName: allData.personalInfo.lastName,
        country: allData.personalInfo.country,
        phone: allData.personalInfo.phone,
        birthDate: allData.personalInfo.birthDate,
        
        // Address
        address: allData.identityVerification.address,
        city: allData.identityVerification.city,
        postalCode: allData.identityVerification.postalCode,
        
        // Identity
        documentType: allData.identityVerification.documentType,
        documentNumber: allData.identityVerification.documentNumber,
        
        // Account setup
        password: allData.accountSetup.password,
        confirmPassword: allData.accountSetup.confirmPassword,
        
        // Products
        accountType: allData.productSelection.accountType,
        selectedCards: allData.productSelection.cards,
        cardPins: allData.productSelection.cards.map(cardId => allData.productSelection.cardPins[cardId]),
        initialDeposit: allData.productSelection.initialDeposit
      };
      
      const response = await fetch('http://localhost:8080/api/onboarding/complete', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(registrationData)
      });
      
      const data = await response.json();
      
      if (data.success) {
        setIsCompleted(true);
      } else {
        alert('Erreur: ' + (data.message || 'Erreur inconnue'));
      }
    } catch (error) {
      alert('Erreur de connexion: ' + error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const getAccountTypeName = (id) => {
    const types = {
      'current': 'Compte Courant',
      'savings': 'Compte Épargne',
      'premium': 'Compte Premium'
    };
    return types[id] || id;
  };

  const getCardNames = (cardIds) => {
    const cards = {
      'classic': 'Carte Classique',
      'gold': 'Carte Gold',
      'platinum': 'Carte Platinum'
    };
    return cardIds.map(id => cards[id] || id);
  };

  if (isCompleted) {
    return (
      <div className="step-container">
        <div className="success-container">
          <h2>Félicitations !</h2>
          <p>Votre demande d'ouverture de compte a été soumise avec succès.</p>
          
          <div className="next-steps">
            <h3>Prochaines étapes :</h3>
            <ol>
              <li>Vous recevrez un email de confirmation dans les prochaines minutes</li>
              <li>Notre équipe examinera votre dossier sous 24-48h</li>
              <li>Vous serez notifié par email de l'activation de votre compte</li>
              <li>Vous pourrez alors effectuer votre premier dépôt</li>
            </ol>
          </div>

          <div className="account-info">
            <h4>Informations de votre futur compte :</h4>
            <p><strong>Type de compte :</strong> {getAccountTypeName(allData.productSelection.accountType)}</p>
          </div>

          <button 
            onClick={() => window.location.href = '/login'} 
            className="btn-primary"
          >
            Aller à la connexion
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="step-container">
      <h2>Récapitulatif de votre demande</h2>
      <p>Vérifiez vos informations avant de finaliser votre demande</p>

      <div className="summary-sections">
        <div className="summary-section">
          <h3>Informations personnelles</h3>
          <div className="summary-content">
            <p><strong>Nom :</strong> {allData.personalInfo.firstName} {allData.personalInfo.lastName}</p>
            <p><strong>Email :</strong> {allData.personalInfo.email}</p>
            <p><strong>Téléphone :</strong> {allData.personalInfo.phone}</p>
            <p><strong>Date de naissance :</strong> {allData.personalInfo.birthDate}</p>
            <p><strong>Pays :</strong> {allData.personalInfo.country}</p>
          </div>
        </div>

        <div className="summary-section">
          <h3>Adresse</h3>
          <div className="summary-content">
            <p>{allData.identityVerification.address}</p>
            <p>{allData.identityVerification.postalCode} {allData.identityVerification.city}</p>
          </div>
        </div>

        <div className="summary-section">
          <h3>Vérifications</h3>
          <div className="summary-content">
            <p>Email vérifié</p>
            <p>Document d'identité : {allData.identityVerification.documentType}</p>
            <p>Mot de passe configuré</p>
          </div>
        </div>

        <div className="summary-section">
          <h3>Produits sélectionnés</h3>
          <div className="summary-content">
            <p><strong>Compte :</strong> {getAccountTypeName(allData.productSelection.accountType)}</p>
            
            {allData.productSelection.cards.length > 0 && (
              <p><strong>Cartes :</strong> {getCardNames(allData.productSelection.cards).join(', ')}</p>
            )}
            

            
            {allData.productSelection.initialDeposit && (
              <p><strong>Dépôt initial :</strong> {allData.productSelection.initialDeposit} TND</p>
            )}
          </div>
        </div>
      </div>

      <div className="final-notice">
        <div className="notice-box">
          <h4>Important</h4>
          <p>En soumettant cette demande, vous confirmez que toutes les informations fournies sont exactes et complètes. Toute fausse déclaration peut entraîner le rejet de votre demande.</p>
        </div>
      </div>

      <div className="step-actions">
        <button type="button" onClick={onBack} className="btn-secondary">
          Modifier
        </button>
        <button 
          onClick={handleFinalSubmit} 
          disabled={isSubmitting}
          className="btn-primary"
        >
          {isSubmitting ? 'Soumission en cours...' : 'Finaliser ma demande'}
        </button>
      </div>
    </div>
  );
};

export default ConfirmationStep;