import React, { useState, useEffect } from 'react';

const Header = ({ user, onLogout }) => {
    const [currentTime, setCurrentTime] = useState(new Date());

    useEffect(() => {
        const timer = setInterval(() => {
            setCurrentTime(new Date());
        }, 1000);
        return () => clearInterval(timer);
    }, []);

    return (
        <header className="dashboard-header">
            <div className="header-left">
                <h1>🏦 Secure Banking</h1>
                <span className="user-welcome">
                    Bienvenue, {user.firstName} {user.lastName}
                </span>
            </div>
            <div className="header-right">
                <div className="current-time">
                    {currentTime.toLocaleString('fr-FR')}
                </div>
                <button className="logout-btn" onClick={onLogout}>
                    🔒 Déconnexion
                </button>
            </div>
        </header>
    );
};

export default Header;
