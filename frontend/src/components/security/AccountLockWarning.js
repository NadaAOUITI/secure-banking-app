import React from 'react';
import './AccountLockWarning.css';

const AccountLockWarning = ({ failedAttempts, maxAttempts = 5, isLocked = false }) => {
    if (isLocked) {
        return (
            <div className="account-lock-warning locked">
                <div className="lock-icon">🔒</div>
                <h3>Compte Verrouillé</h3>
                <p>Votre compte a été temporairement verrouillé après {maxAttempts} tentatives de connexion échouées.</p>
                <div className="lockout-details">
                    <p><strong>Durée du verrouillage :</strong> 30 minutes</p>
                    <p><strong>Raison :</strong> Mesure de sécurité préventive</p>
                </div>
            </div>
        );
    }

    if (failedAttempts > 0) {
        const remainingAttempts = maxAttempts - failedAttempts;
        const warningLevel = failedAttempts >= 3 ? 'high' : 'medium';

        return (
            <div className={`account-lock-warning ${warningLevel}`}>
                <div className="warning-icon">
                    {warningLevel === 'high' ? '⚠️' : '🔔'}
                </div>
                <div className="warning-content">
                    <p><strong>Attention :</strong> {failedAttempts}/{maxAttempts} tentatives échouées</p>
                    <p>{remainingAttempts} tentative(s) restante(s) avant verrouillage</p>
                </div>
            </div>
        );
    }

    return null;
};

export default AccountLockWarning;
