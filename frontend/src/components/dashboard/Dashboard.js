import React, { useState } from 'react';
import Header from './Header';
import AccountSummary from './AccountSummary';
import QuickActions from './QuickActions';
import RecentTransactions from './RecentTransactions';
import SecurityInfo from './SecurityInfo';
import './Dashboard.css';

const Dashboard = ({ user, onLogout }) => {
    const [accountData] = useState({
        balance: 15420.50,
        accountNumber: "FR76 3000 3000 1100 0000 0137",
        iban: "FR76 3000 3000 1100 0000 0137",
        recentTransactions: [
            { id: 1, date: "2024-01-15", description: "Virement reçu - Salaire", amount: 2500.00, type: "credit" },
            { id: 2, date: "2024-01-14", description: "Prélèvement - Loyer", amount: -850.00, type: "debit" },
            { id: 3, date: "2024-01-13", description: "Achat - Supermarché", amount: -67.45, type: "debit" },
            { id: 4, date: "2024-01-12", description: "Virement envoyé", amount: -200.00, type: "debit" }
        ]
    });

    return (
        <div className="dashboard">
            <Header user={user} onLogout={onLogout} />
            
            <main className="dashboard-main">
                <AccountSummary accountData={accountData} />
                <QuickActions />
                <RecentTransactions transactions={accountData.recentTransactions} />
                <SecurityInfo user={user} />
            </main>

            <footer className="dashboard-footer">
                <p>© 2024 Secure Banking - Application Bancaire Sécurisée</p>
            </footer>
        </div>
    );
};

export default Dashboard;