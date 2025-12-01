import React from 'react';
import { formatCurrency, maskAccountNumber } from '../../../utils/formatters';

const AccountCard = ({ accountData, isMain = false, onRefresh }) => {
  if (!accountData) return null;

  return (
    <div className={`account-card ${isMain ? 'main-account' : ''}`}>
      <div className="card-header">
        <span className="account-holder">{accountData.firstName} {accountData.lastName}</span>
        <div className="account-type">{accountData.accountType}</div>
      </div>
      
      <div className="balance-section">
        <div className="balance-amount">
          {formatCurrency(accountData.balance, accountData.currency)}
        </div>
        <div className="account-number">
          {maskAccountNumber(accountData.accountNumber)}
        </div>
        {onRefresh && (
          <button onClick={onRefresh} className="refresh-btn-card">
            🔄 Actualiser
          </button>
        )}
      </div>

      <div className="card-footer">
        <div className="mini-chart">
          <svg width="100%" height="40" viewBox="0 0 200 40">
            <path 
              d="M0,30 Q50,10 100,20 T200,15" 
              stroke="#ffffff" 
              strokeWidth="2" 
              fill="none"
              opacity="0.8"
            />
          </svg>
        </div>
      </div>
    </div>
  );
};

export default AccountCard;
