import React from 'react';

const RecentTransactions = () => {
  const transactions = [
    {
      id: 1,
      type: 'credit',
      description: 'Virement reçu - Salaire',
      amount: 2500.00,
      date: '2024-11-29',
      icon: '💰'
    },
    {
      id: 2,
      type: 'debit',
      description: 'Prélèvement - Loyer',
      amount: -850.00,
      date: '2024-11-28',
      icon: '🏠'
    },
    {
      id: 3,
      type: 'debit',
      description: 'Achat - Supermarché',
      amount: -67.45,
      date: '2024-11-27',
      icon: '🛒'
    },
    {
      id: 4,
      type: 'credit',
      description: 'Remboursement',
      amount: 150.00,
      date: '2024-11-26',
      icon: '↩️'
    }
  ];

  const formatAmount = (amount) => {
    const formatted = Math.abs(amount).toLocaleString('fr-TN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
    return `${amount >= 0 ? '+' : '-'}${formatted} DT`;
  };

  return (
    <div className="recent-transactions">
      <div className="transactions-list">
        {transactions.map(transaction => (
          <div key={transaction.id} className="transaction-item">
            <div className="transaction-icon">
              {transaction.icon}
            </div>
            <div className="transaction-details">
              <div className="transaction-description">
                {transaction.description}
              </div>
              <div className="transaction-date">
                {new Date(transaction.date).toLocaleDateString('fr-FR')}
              </div>
            </div>
            <div className={`transaction-amount ${transaction.type}`}>
              {formatAmount(transaction.amount)}
            </div>
          </div>
        ))}
      </div>
      
      <button className="view-all-transactions">
        Voir toutes les transactions
      </button>
    </div>
  );
};

export default RecentTransactions;