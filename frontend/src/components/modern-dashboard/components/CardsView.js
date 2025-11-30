import React, { useState } from 'react';
import BankCard from './BankCard';
import { useAccountDetails } from '../../../hooks/useAccountDetails';
import { maskCardNumber } from '../../../utils/formatters';
import AddCardModal from '../../AddCardModal';

const CardsView = ({ user }) => {
  const [selectedCard, setSelectedCard] = useState(0);
  const [showAddCardModal, setShowAddCardModal] = useState(false);
  const { data: accountData, loading, error, refetch } = useAccountDetails();
  
  // Extract all cards from all accounts
  const allCards = accountData?.accounts?.flatMap((account, accountIndex) => 
    account.cards?.map((card, cardIndex) => ({
      id: `${account.id}-${cardIndex}`,
      type: card.cardType,
      number: maskCardNumber(card.cardNumber),
      holder: `${accountData.firstName} ${accountData.lastName}`,
      expiry: card.expiryDate,
      balance: account.balance,
      accountNumber: account.accountNumber
    })) || []
  ) || [];
  
  if (loading) return <div className="loading-spinner">Chargement...</div>;
  if (error) return <div className="error-message">{error}</div>;

  return (
    <div className="cards-view">
      <div className="cards-carousel">
        {allCards.map((card, index) => (
          <BankCard 
            key={card.id}
            card={card}
            isActive={selectedCard === index}
            onClick={() => setSelectedCard(index)}
          />
        ))}
        <div className="add-card-btn" onClick={() => setShowAddCardModal(true)}>
          <div className="add-icon">+</div>
          <span>Ajouter une carte</span>
        </div>
      </div>

      <div className="card-balance-section">
        <h3>Solde du compte</h3>
        <div className="balance-display">
          {allCards[selectedCard]?.balance?.toLocaleString('fr-TN') || '0'} <span className="currency">DT</span>
        </div>
        
        <div className="card-actions">
          <button className="action-btn primary">Retrait</button>
          <button className="action-btn secondary">Paiement</button>
        </div>
      </div>

      <div className="spending-chart">
        <div className="chart-container">
          <div className="spending-circle">
            <div className="circle-progress">
              <span className="spent">0 <small>DT</small></span>
              <span className="label">Dépenses</span>
            </div>
            <div className="remaining">
              <span className="amount">3 000 <small>DT</small></span>
              <span className="label">Reste</span>
            </div>
          </div>
        </div>
      </div>
      
      <AddCardModal 
        isOpen={showAddCardModal}
        onClose={() => setShowAddCardModal(false)}
        onSuccess={async (data) => {
          console.log('Carte ajoutée:', data);
          setShowAddCardModal(false);
          // Attendre un peu avant de rafraîchir pour s'assurer que la DB est à jour
          setTimeout(() => {
            refetch();
          }, 500);
        }}
        accounts={accountData?.accounts?.map(account => ({
          id: account.id,
          accountId: account.id,
          accountNumber: account.accountNumber,
          accountType: account.accountType
        }))}
      />
    </div>
  );
};

export default CardsView;