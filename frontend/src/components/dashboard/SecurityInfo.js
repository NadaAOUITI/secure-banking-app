import React from 'react';

const SecurityInfo = ({ user }) => {
    return (
        <section className="security-info">
            <h3>🔐 Informations de Sécurité</h3>
            <div className="security-grid">
                <div className="security-item">
                    <span className="security-label">Dernière connexion:</span>
                    <span className="security-value">
                        {user.lastLoginAt 
                            ? new Date(user.lastLoginAt).toLocaleString('fr-FR') 
                            : 'Première connexion'
                        }
                    </span>
                </div>
                <div className="security-item">
                    <span className="security-label">Session expire dans:</span>
                    <span className="security-value">14 minutes</span>
                </div>
                <div className="security-item">
                    <span className="security-label">Connexion sécurisée:</span>
                    <span className="security-value">✅ HTTPS + 2FA</span>
                </div>
            </div>
        </section>
    );
};

export default SecurityInfo;
