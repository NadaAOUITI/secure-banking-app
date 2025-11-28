import React, { useState } from 'react';
import RegisterPage from './pages/auth/RegisterPage';
import LoginPage from './pages/auth/LoginPage';
import Dashboard from './components/dashboard/Dashboard';
import './App.css';

function App() {
  const [currentPage, setCurrentPage] = useState('login'); // 'login', 'register', 'dashboard'
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
      case 'register':
        return <RegisterPage />;
      case 'dashboard':
        return <Dashboard user={user} onLogout={handleLogout} />;
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
            onClick={() => setCurrentPage('register')}
            className={currentPage === 'register' ? 'active' : ''}
          >
            S'inscrire
          </button>
        </div>
      )}
      
      {renderCurrentPage()}
    </div>
  );
}

export default App;