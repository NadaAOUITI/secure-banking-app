"use client"

import { useState } from "react"
import TabNavigation from "./components/TabNavigation"
import AccountsView from "./components/AccountsView"
import CardsView from "./components/CardsView"
import RequestsView from "./components/RequestsView"
import ProfileView from "./components/ProfileView"
import BeneficiaryManagement from "../dashboard/BeneficiaryManagement"
import "./ModernDashboard.css"

const ModernDashboard = ({ user, onLogout, onNavigateToBeneficiaries }) => {
  const [activeTab, setActiveTab] = useState("accounts")
  const [showBeneficiaries, setShowBeneficiaries] = useState(false)
  const [notification, setNotification] = useState(null)

  const showNotification = (message, type = "success", duration = 3000) => {
    setNotification({ message, type })
    setTimeout(() => setNotification(null), duration)
  }

  // Si on affiche les bénéficiaires
  if (showBeneficiaries) {
    return <BeneficiaryManagement onClose={() => setShowBeneficiaries(false)} />
  }

  const renderActiveView = () => {
    switch (activeTab) {
      case "accounts":
        return <AccountsView user={user} onManageBeneficiaries={() => setShowBeneficiaries(true)} />
      case "cards":
        return <CardsView user={user} />
      case "requests":
        return <RequestsView user={user} />
      case "profile":
        return <ProfileView user={user} />
      default:
        return <AccountsView user={user} onManageBeneficiaries={() => setShowBeneficiaries(true)} />
    }
  }

  const handleQuickAction = (actionType) => {
    switch (actionType) {
      case "transfer":
        showNotification("Fonctionnalité de virement en cours de développement", "info")
        break
      case "history":
        showNotification("Historique chargé", "success")
        break
      case "settings":
        showNotification("Paramètres en cours de développement", "info")
        break
      default:
        break
    }
  }

  return (
      <div className="modern-dashboard">
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

        <TabNavigation activeTab={activeTab} onTabChange={setActiveTab} />

        <main className="dashboard-content">{renderActiveView()}</main>

        <section className="quick-actions-section">
          <h3>Actions Rapides</h3>
          <div className="quick-actions-grid">
            <button
                className="quick-action-btn transfer"
                onClick={() => handleQuickAction("transfer")}
                title="Effectuer un virement"
            >
              <span className="action-icon">💸</span>
              <span className="action-text">Virement</span>
            </button>

            <button
                className="quick-action-btn beneficiary"
                onClick={() => setShowBeneficiaries(true)}
                title="Gérer les bénéficiaires"
            >
              <span className="action-icon">👥</span>
              <span className="action-text">Bénéficiaires</span>
            </button>

            <button
                className="quick-action-btn history"
                onClick={() => handleQuickAction("history")}
                title="Consulter l'historique"
            >
              <span className="action-icon">📊</span>
              <span className="action-text">Historique</span>
            </button>

            <button
                className="quick-action-btn settings"
                onClick={() => handleQuickAction("settings")}
                title="Accéder aux paramètres"
            >
              <span className="action-icon">⚙️</span>
              <span className="action-text">Paramètres</span>
            </button>
          </div>
        </section>

        {notification && <div className={`notification notification-${notification.type}`}>{notification.message}</div>}
      </div>
  )
}

export default ModernDashboard
