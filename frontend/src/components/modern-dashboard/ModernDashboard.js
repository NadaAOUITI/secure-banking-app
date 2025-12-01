import React, { useState } from "react";
import TabNavigation from "./components/TabNavigation";
import AccountsView from "./components/AccountsView";
import CardsView from "./components/CardsView";
import RequestsView from "./components/RequestsView";
import ProfileView from "./components/ProfileView";
import BeneficiaryManagement from "../dashboard/BeneficiaryManagement";
import TransferManagement from "../dashboard/TransferManagement";
import "./ModernDashboard.css";

const ModernDashboard = ({ user, accounts = [], onLogout, onNavigateToBeneficiaries, onNavigateToTransfers }) => {
  const [activeTab, setActiveTab] = useState("accounts");
  const [activeView, setActiveView] = useState("dashboard"); // 'dashboard', 'beneficiaries', 'transfers'
  const [notification, setNotification] = useState(null);

  const showNotification = (message, type = "success", duration = 3000) => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), duration);
  };

  // Si on affiche les bénéficiaires
  if (activeView === "beneficiaries") {
    return <BeneficiaryManagement onClose={() => setActiveView("dashboard")} />;
  }

  // Si on affiche les virements
  if (activeView === "transfers") {
    return (
        <TransferManagement
            onClose={() => setActiveView("dashboard")}
            accounts={accounts}
        />
    );
  }

  const renderActiveView = () => {
    switch (activeTab) {
      case "accounts":
        return (
            <AccountsView
                user={user}
                onManageBeneficiaries={() => setActiveView("beneficiaries")}
                onMakeTransfer={() => setActiveView("transfers")}
            />
        );
      case "cards":
        return <CardsView user={user} />;
      case "requests":
        return <RequestsView user={user} />;
      case "profile":
        return <ProfileView user={user} />;
      default:
        return (
            <AccountsView
                user={user}
                onManageBeneficiaries={() => setActiveView("beneficiaries")}
                onMakeTransfer={() => setActiveView("transfers")}
            />
        );
    }
  };

  return (
      <div className="modern-dashboard">
        {/* Header */}
        <header className="dashboard-header">
          <div className="header-left">
            <button className="menu-btn" title="Menu">
              ☰
            </button>
            <h1>Tableau de bord</h1>
          </div>
          <div className="header-right">
            <button className="notification-btn" title="Notifications">
              🔔
            </button>
            <button onClick={onLogout} className="logout-btn" title="Déconnexion">
              ⚡
            </button>
          </div>
        </header>

        {/* Tab Navigation */}
        <TabNavigation activeTab={activeTab} onTabChange={setActiveTab} />

        {/* Main Content */}
        <main className="dashboard-content">{renderActiveView()}</main>

        {/* Quick Actions */}
        <section className="quick-actions-section">
          <h3>🚀 Actions Rapides</h3>
          <div className="quick-actions-grid">
            {/* Virement */}
            <button
                className="quick-action-btn transfer"
                onClick={() => setActiveView("transfers")}
                title="Effectuer un virement"
            >
              <span className="action-icon">💸</span>
              <span className="action-text">Virement</span>
            </button>

            {/* Bénéficiaires */}
            <button
                className="quick-action-btn beneficiary"
                onClick={() => setActiveView("beneficiaries")}
                title="Gérer les bénéficiaires"
            >
              <span className="action-icon">👥</span>
              <span className="action-text">Bénéficiaires</span>
            </button>

            {/* Historique */}
            <button
                className="quick-action-btn history"
                onClick={() => {
                  setActiveView("transfers");
                  // L'historique sera affiché dans TransferManagement
                }}
                title="Consulter l'historique"
            >
              <span className="action-icon">📊</span>
              <span className="action-text">Historique</span>
            </button>

            {/* Paramètres */}
            <button
                className="quick-action-btn settings"
                onClick={() => setActiveTab("profile")}
                title="Accéder aux paramètres"
            >
              <span className="action-icon">⚙️</span>
              <span className="action-text">Paramètres</span>
            </button>
          </div>
        </section>

        {/* Notification Toast */}
        {notification && (
            <div className={`notification notification-${notification.type}`}>
              {notification. message}
            </div>
        )}
      </div>
  );
};

export default ModernDashboard;
