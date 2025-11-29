import React from 'react';

const AccountSummary = ({ accountData }) => {
    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('fr-FR', {
            style: 'currency',
            currency: 'EUR'
        }).format(amount);
    };

    return (
        <section className="account-summary">
            <div className="balance-card">
                <h2>💰 Solde du Compte</h2>
                <div className="balance-amount">
                    {formatCurrency(accountData.balance)}
                </div>
                <div className="account-info">
                    <p><strong>Numéro de compte:</strong> {accountData.accountNumber}</p>
                    <p><strong>IBAN:</strong> {accountData.iban}</p>
                </div>
            </div>
        </section>
    );
};

export default AccountSummary;