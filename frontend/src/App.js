import React, { useState } from 'react';
import RegisterPage from './pages/auth/RegisterPage';
import LoginPage from './pages/auth/LoginPage';
import './App.css';

function App() {
  const [currentPage, setCurrentPage] = useState('login'); // 'login' or 'register'

  return (
    <div className="App">
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
      
      {currentPage === 'login' ? <LoginPage /> : <RegisterPage />}
    </div>
  );
}

export default App;