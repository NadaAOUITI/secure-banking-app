import React from 'react';

const RequestsView = ({ user }) => {
  return (
    <div className="requests-view">
      <div className="requests-grid">
        <div className="request-card">
          <div className="request-icon">🏦</div>
          <h3>Nouveau Compte</h3>
          <p>Ouvrir un compte épargne ou premium</p>
          <button className="request-btn">Demander</button>
        </div>
        
        <div className="request-card">
          <div className="request-icon">💳</div>
          <h3>Nouvelle Carte</h3>
          <p>Ajouter une carte à un compte existant</p>
          <button className="request-btn">Demander</button>
        </div>
        
        <div className="request-card">
          <div className="request-icon">📄</div>
          <h3>Relevé Bancaire</h3>
          <p>Télécharger un relevé de compte</p>
          <button className="request-btn">Télécharger</button>
        </div>
      </div>
      
      <div className="pending-requests">
        <h3>Demandes en cours</h3>
        <div className="empty-state">
          <p>Aucune demande en cours</p>
        </div>
      </div>
    </div>
  );
};

export default RequestsView;