import React, { useState } from 'react';
import './StepStyles.css';

const ProductSelectionStep = ({ data, onNext, onBack }) => {
  const [selectedProducts, setSelectedProducts] = useState({
    accountType: data.accountType || '',
    cards: data.cards || [],
    cardPins: data.cardPins || {},
    initialDeposit: data.initialDeposit || '',
    ...data
  });

  const accountTypes = [
    {
      id: 'current',
      name: 'Compte Courant',
      description: 'Pour vos opérations quotidiennes',
      features: ['Carte bancaire incluse', 'Virements illimités', 'Découvert autorisé'],
      monthlyFee: '15 TND'
    },
    {
      id: 'savings',
      name: 'Compte Épargne',
      description: 'Pour faire fructifier votre argent',
      features: ['Taux d\'intérêt 2%', 'Pas de frais', 'Retraits limités'],
      monthlyFee: 'Gratuit'
    },
    {
      id: 'premium',
      name: 'Compte Premium',
      description: 'Tous les avantages inclus',
      features: ['Carte Gold', 'Conseiller dédié', 'Assurances incluses'],
      monthlyFee: '45 TND'
    }
  ];

  const cardOptions = [
    {
      id: 'classic',
      name: 'Carte Classique',
      description: 'Paiements et retraits',
      annualFee: 'Gratuite'
    },
    {
      id: 'gold',
      name: 'Carte Gold',
      description: 'Avantages et assurances',
      annualFee: '150 TND'
    },
    {
      id: 'platinum',
      name: 'Carte Platinum',
      description: 'Services premium',
      annualFee: '450 TND'
    }
  ];



  const handleAccountTypeChange = (accountType) => {
    setSelectedProducts({
      ...selectedProducts,
      accountType
    });
  };

  const handleCardChange = (cardId) => {
    const updatedCards = selectedProducts.cards.includes(cardId)
      ? selectedProducts.cards.filter(id => id !== cardId)
      : [...selectedProducts.cards, cardId];
    
    // Remove PIN if card is deselected
    const updatedPins = { ...selectedProducts.cardPins };
    if (!updatedCards.includes(cardId)) {
      delete updatedPins[cardId];
    }
    
    setSelectedProducts({
      ...selectedProducts,
      cards: updatedCards,
      cardPins: updatedPins
    });
  };
  
  const handlePinChange = (cardId, pin) => {
    setSelectedProducts({
      ...selectedProducts,
      cardPins: {
        ...selectedProducts.cardPins,
        [cardId]: pin
      }
    });
  };



  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!selectedProducts.accountType) {
      alert('Veuillez sélectionner un type de compte');
      return;
    }
    
    // Validate minimum deposit if provided
    if (selectedProducts.initialDeposit && parseFloat(selectedProducts.initialDeposit) < 300) {
      alert('Le dépôt initial doit être d\'au moins 300 TND');
      return;
    }
    
    // Validate PINs for selected cards
    for (const cardId of selectedProducts.cards) {
      const pin = selectedProducts.cardPins[cardId];
      if (!pin || pin.length !== 4 || !/^\d{4}$/.test(pin)) {
        alert(`Veuillez saisir un code PIN valide (4 chiffres) pour la carte ${cardOptions.find(c => c.id === cardId)?.name}`);
        return;
      }
    }
    
    onNext(selectedProducts);
  };

  return (
    <div className="step-container">
      <h2>Sélection de vos produits</h2>
      <p>Choisissez les produits et services qui correspondent à vos besoins</p>

      <form onSubmit={handleSubmit} className="step-form">
        <h3>Type de compte *</h3>
        <div className="product-grid">
          {accountTypes.map(account => (
            <div 
              key={account.id}
              className={`product-card ${selectedProducts.accountType === account.id ? 'selected' : ''}`}
              onClick={() => handleAccountTypeChange(account.id)}
            >
              <h4>{account.name}</h4>
              <p>{account.description}</p>
              <ul>
                {account.features.map((feature, index) => (
                  <li key={index}>{feature}</li>
                ))}
              </ul>
              <div className="price">{account.monthlyFee}/mois</div>
            </div>
          ))}
        </div>

        <h3>Cartes bancaires (optionnel)</h3>
        <div className="product-grid">
          {cardOptions.map(card => (
            <div key={card.id}>
              <div 
                className={`product-card ${selectedProducts.cards.includes(card.id) ? 'selected' : ''}`}
                onClick={() => handleCardChange(card.id)}
              >
                <h4>{card.name}</h4>
                <p>{card.description}</p>
                <div className="price">{card.annualFee}/an</div>
              </div>
              
              {selectedProducts.cards.includes(card.id) && (
                <div className="pin-input-section">
                  <label>Code PIN (4 chiffres) *</label>
                  <input
                    type="password"
                    maxLength="4"
                    pattern="\d{4}"
                    value={selectedProducts.cardPins[card.id] || ''}
                    onChange={(e) => handlePinChange(card.id, e.target.value)}
                    placeholder="••••"
                    className="pin-input"
                    onClick={(e) => e.stopPropagation()}
                  />
                  <small>Saisissez un code PIN sécurisé de 4 chiffres</small>
                </div>
              )}
            </div>
          ))}
        </div>



        <div className="form-group">
          <label>Dépôt initial (optionnel)</label>
          <input
            type="number"
            name="initialDeposit"
            value={selectedProducts.initialDeposit}
            onChange={(e) => setSelectedProducts({
              ...selectedProducts,
              initialDeposit: e.target.value
            })}
            placeholder="Montant en dinars tunisiens"
            min="300"
          />
          <small>Minimum recommandé: 300 TND</small>
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

export default ProductSelectionStep;