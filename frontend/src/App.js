import React, { useState, useEffect } from 'react';
import LoginPage from './pages/auth/LoginPage';
import Dashboard from './components/dashboard/Dashboard';
import ModernDashboard from './components/modern-dashboard/ModernDashboard';
import OnboardingPage from './pages/onboarding/OnboardingPage';
import BeneficiaryManagement from './components/dashboard/BeneficiaryManagement';
import './App.css';

function App() {
  const [currentPage, setCurrentPage] = useState('login');
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Vérifier si l'utilisateur est déjà connecté au démarrage
  useEffect(() => {
    checkExistingSession();
  }, []);

  const checkExistingSession = async () => {
    try {
      const response = await fetch('https://localhost:8443/api/account/details', {
        method: 'GET',
        credentials: 'include'
      });

      if (response. ok) {
        const accountData = await response.json();
        setUser({
          firstName: accountData.firstName,
          lastName: accountData.lastName,
          email: accountData.email
        });
        setCurrentPage('dashboard');
      }
    } catch (error) {
      console.log('Aucune session active');
    } finally {
      setLoading(false);
    }
  };

  const handleLoginSuccess = (userData) => {
    setUser(userData);
    setCurrentPage('dashboard');
  };

  const handleLogout = () => {
    setUser(null);
    setCurrentPage('login');
  };

  // Navigation vers bénéficiaires
  const handleNavigateToBeneficiaries = () => {
    setCurrentPage('beneficiaries');
  };

  const renderCurrentPage = () => {
    switch(currentPage) {
      case 'dashboard':
        return (
            <ModernDashboard
                user={user}
                onLogout={handleLogout}
                onNavigateToBeneficiaries={handleNavigateToBeneficiaries}
            />
        );
      case 'beneficiaries':
        return (
            <BeneficiaryManagement
                onClose={() => setCurrentPage('dashboard')}
            />
        );
      case 'onboarding':
        return <OnboardingPage />;
      default:
        return <LoginPage onLoginSuccess={handleLoginSuccess} />;
    }
  };

  if (loading) {
    return <div className="loading">Chargement...</div>;
  }

  return (
      <div className="App">
        {currentPage !== 'dashboard' && currentPage !== 'beneficiaries' && (
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
