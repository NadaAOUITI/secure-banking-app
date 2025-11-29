import React, { useState } from 'react';
import TabNavigation from './components/TabNavigation';
import AccountsView from './components/AccountsView';
import CardsView from './components/CardsView';
import RequestsView from './components/RequestsView';
import ProfileView from './components/ProfileView';
import './ModernDashboard.css';

const ModernDashboard = ({ user, onLogout }) => {
  const [activeTab, setActiveTab] = useState('accounts');

  const renderActiveView = () => {
    switch(activeTab) {
      case 'accounts':
        return <AccountsView user={user} />;
      case 'cards':
        return <CardsView user={user} />;
      case 'requests':
        return <RequestsView user={user} />;
      case 'profile':
        return <ProfileView user={user} />;
      default:
        return <AccountsView user={user} />;
    }
  };

  return (
    <div className="modern-dashboard">
      <header className="dashboard-header">
        <div className="header-left">
          <button className="menu-btn">☰</button>
          <h1>Tableau de bord</h1>
        </div>
        <div className="header-right">
          <button onClick={onLogout} className="logout-btn">⚡</button>
        </div>
      </header>

      <TabNavigation 
        activeTab={activeTab} 
        onTabChange={setActiveTab} 
      />

      <main className="dashboard-content">
        {renderActiveView()}
      </main>
    </div>
  );
};

export default ModernDashboard;