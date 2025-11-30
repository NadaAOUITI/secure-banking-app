import React from 'react';

const QuickActions = () => {
    const handleAction = (actionType) => {
        console.log(`Action: ${actionType}`);
        // TODO: Implémenter les actions
    };

    return (
        <section className="quick-actions">
            <h3>🚀 Actions Rapides</h3>
            <div className="actions-grid">
                <button 
                    className="action-btn transfer"
                    onClick={() => handleAction('transfer')}
                >
                    💸 Effectuer un Virement
                </button>
                <button 
                    className="action-btn beneficiary"
                    onClick={() => handleAction('beneficiary')}
                >
                    👥 Gérer les Bénéficiaires
                </button>
                <button 
                    className="action-btn history"
                    onClick={() => handleAction('history')}
                >
                    📊 Historique des Transactions
                </button>
                <button 
                    className="action-btn profile"
                    onClick={() => handleAction('profile')}
                >
                    ⚙️ Mon Profil
                </button>
            </div>
        </section>
    );
};

export default QuickActions;
