import React from 'react';
import { useAccountDetails } from '../../hooks/useAccountDetails';
import { formatCurrency, maskAccountNumber } from '../../utils/formatters';

const AccountSummary = () => {
    const { data, loading, error, refetch } = useAccountDetails();

    if (loading) return <div className="loading">Chargement...</div>;
    if (error) return <div className="error">{error}</div>;
    if (!data) return <div className="error">Aucune donnée disponible</div>;

    return (
        <section className="account-summary">
            <div className="balance-card">
                <h2>💰 Solde du Compte</h2>
                <div className="balance-amount">
                    {formatCurrency(data.balance, data.currency)}
                </div>
                <div className="account-info">
                    <p><strong>Numéro de compte:</strong> {maskAccountNumber(data.accountNumber)}</p>
                    <p><strong>Type:</strong> {data.accountType}</p>
                    <p><strong>Titulaire:</strong> {data.firstName} {data.lastName}</p>
                </div>
                <button onClick={refetch} className="refresh-btn">
                    🔄 Actualiser
                </button>
            </div>
        </section>
    );
};

export default AccountSummary;
