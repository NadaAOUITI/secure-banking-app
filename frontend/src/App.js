import React, { useState } from 'react';
import LoginPage from './pages/auth/LoginPage';
import Dashboard from './components/dashboard/Dashboard';
import OnboardingPage from './pages/onboarding/OnboardingPage';
import './App.css';

function App() {
  const [currentPage, setCurrentPage] = useState('login'); // 'login', 'register', 'dashboard', 'onboarding'
  const [user, setUser] = useState(null);

  const handleLoginSuccess = (userData) => {
    setUser(userData);
    setCurrentPage('dashboard');
  };

  const handleLogout = () => {
    setUser(null);
    setCurrentPage('login');
  };

  const renderCurrentPage = () => {
    switch(currentPage) {
      case 'dashboard':
        return <Dashboard user={user} onLogout={handleLogout} />;
      case 'onboarding':
        return <OnboardingPage />;
      default:
        return <LoginPage onLoginSuccess={handleLoginSuccess} />;
    }
  };

  return (
    <div className="App">
      {currentPage !== 'dashboard' && (
        <div className="auth-toggle">
          <button 
            onClick={() => setCurrentPage('login')}
            className={currentPage === 'login' ? 'active' : ''}
          >
            Se connecter
          </button>
          <button 
            onClick={() => setCurrentPage('onboarding')}
            className={currentPage === 'onboarding' ? 'active' : ''}
          >
            Créer un compte
          </button>
        </div>
      )}
      
      {renderCurrentPage()}
    </div>
  );
}

export default App;