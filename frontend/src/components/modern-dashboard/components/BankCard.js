import React from 'react';

const BankCard = ({ card, isActive, onClick }) => {
  const getCardDesign = (type) => {
    switch(type) {
      case 'PLATINUM':
        return {
          gradient: 'linear-gradient(135deg, #2C3E50 0%, #34495E 100%)',
          logo: '💎',
          accent: '#ECF0F1'
        };
      case 'GOLD':
        return {
          gradient: 'linear-gradient(135deg, #F39C12 0%, #E67E22 100%)',
          logo: '⭐',
          accent: '#FFF'
        };
      case 'CLASSIC':
        return {
          gradient: 'linear-gradient(135deg, #3498DB 0%, #2980B9 100%)',
          logo: '🔷',
          accent: '#FFF'
        };
      default:
        return {
          gradient: 'linear-gradient(135deg, #95A5A6 0%, #7F8C8D 100%)',
          logo: '💳',
          accent: '#FFF'
        };
    }
  };

  const design = getCardDesign(card.type);

  return (
    <div 
      className={`bank-card ${isActive ? 'active' : ''}`}
      style={{ background: design.gradient }}
      onClick={onClick}
    >
      <div className="card-header">
        <div className="bank-logo">
          <span className="logo-icon">{design.logo}</span>
          <span className="bank-name">SECURE BANK</span>
        </div>
        <div className="card-type" style={{ color: design.accent }}>
          {card.type.toLowerCase()}
        </div>
      </div>

      <div className="card-chip">
        <div className="chip"></div>
        <div className="contactless">📶</div>
      </div>

      <div className="card-number" style={{ color: design.accent }}>
        {card.number}
      </div>

      <div className="card-footer">
        <div className="card-holder" style={{ color: design.accent }}>
          <small>Titulaire</small>
          <div>{card.holder}</div>
        </div>
        <div className="card-expiry" style={{ color: design.accent }}>
          <small>Expire</small>
          <div>{card.expiry}</div>
        </div>
        <div className="card-network">
          <div className="mastercard-logo">
            <div className="circle red"></div>
            <div className="circle yellow"></div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BankCard;