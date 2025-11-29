import React from 'react';

const RecentTransactions = ({ transactions }) => {
    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('fr-FR', {
            style: 'currency',
            currency: 'EUR'
        }).format(amount);
    };

    return (
        <section className="recent-transactions">
            <h3>📋 Transactions Récentes</h3>
            <div className="transactions-list">
                {transactions.map(transaction => (
                    <div key={transaction.id} className={`transaction-item ${transaction.type}`}>
                        <div className="transaction-info">
                            <div className="transaction-description">
                                {transaction.description}
                            </div>
                            <div className="transaction-date">
                                {new Date(transaction.date).toLocaleDateString('fr-FR')}
                            </div>
                        </div>
                        <div className={`transaction-amount ${transaction.type}`}>
                            {transaction.type === 'credit' ? '+' : ''}
                            {formatCurrency(transaction.amount)}
                        </div>
                    </div>
                ))}
            </div>
            <button className="view-all-btn">
                Voir toutes les transactions
            </button>
        </section>
    );
};

export default RecentTransactions;