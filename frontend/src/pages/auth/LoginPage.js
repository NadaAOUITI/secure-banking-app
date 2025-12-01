import React from 'react';
import LoginForm from '../../components/auth/LoginForm';

const LoginPage = ({ onLoginSuccess }) => {
  return (
    <div className="login-page">
      <LoginForm onLoginSuccess={onLoginSuccess} />
    </div>
  );
};

export default LoginPage;
