import React, { useState } from 'react';
import { useAccountDetails } from '../../../hooks/useAccountDetails';
import { formatCurrency, maskAccountNumber } from '../../../utils/formatters';
import AccountCard from './AccountCard';
import TransactionChart from './TransactionChart';
import RecentTransactions from './RecentTransactions';
import AddAccountProductModal from '../../AddAccountProductModal';

const AccountsView = ({ user }) => {
  const { data: accountData, loading, error, refetch } = useAccountDetails();
  const [showAddModal, setShowAddModal] = useState(false);
  const [selectedAccountIndex, setSelectedAccountIndex] = useState(0);

  if (loading) return <div className="loading-spinner">Chargement...</div>;
  if (error) return <div className="error-message">{error}</div>;
  if (!accountData) return <div className="error-message">Aucune donnée de compte</div>;

  return (
    <div className="accounts-view">
      <div className="accounts-carousel">
        {accountData.accounts && accountData.accounts.map((account, index) => {
          const accountTypeMap = {
            'CURRENT': 'Compte Courant',
            'SAVINGS': 'Compte Épargne', 
            'PREMIUM': 'Compte Premium'
          };
          
          return (
            <div 
              key={account.accountNumber}
              onClick={() => setSelectedAccountIndex(index)}
              className={`account-card-wrapper ${selectedAccountIndex === index ? 'active' : ''}`}
            >
              <AccountCard 
                accountData={{
                  firstName: accountData.firstName,
                  lastName: accountData.lastName,
                  accountNumber: account.accountNumber,
                  balance: account.balance,
                  accountType: accountTypeMap[account.accountType] || account.accountType,
                  currency: accountData.currency
                }}
                isMain={index === 0}
                onRefresh={refetch}
              />
            </div>
          );
        })}
        <div className="add-account-card" onClick={() => setShowAddModal(true)}>
          <div className="add-icon">+</div>
          <span>Ajouter un compte</span>
        </div>
      </div>

      <div className="account-chart-section">
        <h3>Évolution du solde - {maskAccountNumber(accountData.accounts[selectedAccountIndex]?.accountNumber)}</h3>
        <TransactionChart selectedAccount={accountData.accounts[selectedAccountIndex]} />
      </div>

      <div className="activities-section">
        <div className="section-header">
          <h3>Activités - {maskAccountNumber(accountData.accounts[selectedAccountIndex]?.accountNumber)}</h3>
          <div className="filter-buttons">
            <button className="filter-btn active">Solde</button>
            <button className="filter-btn">Jour</button>
          </div>
        </div>
        <RecentTransactions selectedAccount={accountData.accounts[selectedAccountIndex]} />
      </div>
      
      <AddAccountProductModal 
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        onSuccess={(data) => {
          setShowAddModal(false);
          // Force immediate refresh to show new account
          window.location.reload();
        }}
      />
    </div>
  );
};

export default AccountsView;