import React, { useState } from 'react';
import Header from './Header';
import AccountSummary from './AccountSummary';
import QuickActions from './QuickActions';
import RecentTransactions from './RecentTransactions';
import SecurityInfo from './SecurityInfo';
import './Dashboard.css';

const Dashboard = ({ user, onLogout }) => {
    const mockTransactions = [
        { id: 1, date: "2024-01-15", description: "Virement reçu - Salaire", amount: 2500.00, type: "credit" },
        { id: 2, date: "2024-01-14", description: "Prélèvement - Loyer", amount: -850.00, type: "debit" },
        { id: 3, date: "2024-01-13", description: "Achat - Supermarché", amount: -67.45, type: "debit" },
        { id: 4, date: "2024-01-12", description: "Virement envoyé", amount: -200.00, type: "debit" }
    ];

    return (
        <div className="dashboard">
            <Header user={user} onLogout={onLogout} />
            
            <main className="dashboard-main">
                <AccountSummary />
                <QuickActions />
                <RecentTransactions transactions={mockTransactions} />
                <SecurityInfo user={user} />
            </main>

            <footer className="dashboard-footer">
                <p>© 2024 Secure Banking - Application Bancaire Sécurisée</p>
            </footer>
        </div>
    );
};

export default Dashboard;