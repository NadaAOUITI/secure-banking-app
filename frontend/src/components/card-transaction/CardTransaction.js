import React, { useState, useEffect, useCallback } from 'react';
import './CardTransaction.css';

const CardTransaction = ({ onClose, onTransactionSuccess }) => {
    // State
    const [cards, setCards] = useState([]);
    const [selectedCardId, setSelectedCardId] = useState('');
    const [cardStatus, setCardStatus] = useState(null);
    const [pin, setPin] = useState('');
    const [showPin, setShowPin] = useState(false);
    const [amount, setAmount] = useState('');
    const [beneficiary, setBeneficiary] = useState('');
    const [description, setDescription] = useState('');
    const [loading, setLoading] = useState(false);
    const [loadingCards, setLoadingCards] = useState(true);

    // Alert & Modal State
    const [alert, setAlert] = useState({ show: false, type: '', title: '', message: '' });
    const [blockedModal, setBlockedModal] = useState({ show: false, reason: '' });
    const [successModal, setSuccessModal] = useState({ show: false, data: null });

    const API_BASE = 'https://localhost:8080/api';

    // Charger les cartes de l'utilisateur
    useEffect(() => {
        loadUserCards();
    }, []);

    const loadUserCards = async () => {
        try {
            setLoadingCards(true);
            const response = await fetch(`${API_BASE}/cards/my-cards`, {
                method: 'GET',
                credentials: 'include'
            });

            if (response. ok) {
                const data = await response.json();
                setCards(data);
            } else {
                showAlert('error', 'Erreur', 'Impossible de charger vos cartes');
            }
        } catch (error) {
            console.error('Erreur chargement cartes:', error);
            showAlert('error', 'Erreur', 'Erreur de connexion au serveur');
        } finally {
            setLoadingCards(false);
        }
    };

    // Charger le statut de la carte sélectionnée
    const loadCardStatus = useCallback(async (cardId) => {
        if (!cardId) {
            setCardStatus(null);
            return;
        }

        try {
            const response = await fetch(`${API_BASE}/cards/${cardId}/status`, {
                method: 'GET',
                credentials: 'include'
            });

            if (response. ok) {
                const status = await response.json();
                setCardStatus(status);

                if (status.isBlocked) {
                    setBlockedModal({ show: true, reason: status. blockReason });
                }
            }
        } catch (error) {
            console. error('Erreur chargement statut:', error);
        }
    }, []);

    useEffect(() => {
        if (selectedCardId) {
            loadCardStatus(selectedCardId);
        }
    }, [selectedCardId, loadCardStatus]);

    // Afficher une alerte
    const showAlert = (type, title, message) => {
        setAlert({ show: true, type, title, message });
        setTimeout(() => setAlert({ show: false, type: '', title: '', message: '' }), 5000);
    };

    // Gérer la soumission du formulaire
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!selectedCardId || !pin || !amount || !beneficiary) {
            showAlert('error', 'Erreur', 'Veuillez remplir tous les champs requis');
            return;
        }

        if (pin.length < 4 || pin.length > 6) {
            showAlert('error', 'Erreur', 'Le code PIN doit contenir entre 4 et 6 chiffres');
            return;
        }

        const amountValue = parseFloat(amount);
        if (isNaN(amountValue) || amountValue <= 0) {
            showAlert('error', 'Erreur', 'Montant invalide');
            return;
        }

        setLoading(true);

        try {
            const response = await fetch(`${API_BASE}/cards/transaction`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({
                    cardId: parseInt(selectedCardId),
                    pin,
                    amount: amountValue,
                    beneficiary,
                    description
                })
            });

            const result = await response. json();

            if (result.success) {
                setSuccessModal({ show: true, data: result });
                loadCardStatus(selectedCardId);
                if (onTransactionSuccess) {
                    onTransactionSuccess(result);
                }
            } else {
                handleTransactionError(result);
            }
        } catch (error) {
            console. error('Erreur transaction:', error);
            showAlert('error', 'Erreur', 'Erreur de connexion au serveur');
        } finally {
            setLoading(false);
        }
    };

    // Gérer les erreurs de transaction
    const handleTransactionError = (result) => {
        switch (result.errorType) {
            case 'INVALID_PIN':
                if (result.cardBlocked) {
                    setBlockedModal({
                        show: true,
                        reason: 'Trop de tentatives de PIN incorrectes'
                    });
                } else {
                    showAlert(
                        'warning',
                        'Code PIN incorrect',
                        `Tentatives restantes: ${result.remainingAttempts}`
                    );
                }
                setPin('');
                loadCardStatus(selectedCardId);
                break;

            case 'CARD_BLOCKED':
                setBlockedModal({ show: true, reason: result.reason });
                break;

            case 'LIMIT_EXCEEDED':
                let limitMessage = '';
                switch (result.limitType) {
                    case 'SINGLE_TRANSACTION':
                        limitMessage = `Le montant dépasse la limite par transaction (${formatCurrency(result. limit)})`;
                        break;
                    case 'DAILY_AMOUNT':
                        limitMessage = `Le montant dépasse votre limite journalière restante`;
                        break;
                    case 'DAILY_COUNT':
                        limitMessage = 'Vous avez atteint le nombre maximum de transactions pour aujourd\'hui';
                        break;
                    default:
                        limitMessage = result.message;
                }
                showAlert('error', 'Limite dépassée', limitMessage);
                break;

            default:
                showAlert('error', 'Erreur', result.message || 'Une erreur est survenue');
        }
    };

    // Formater la devise
    const formatCurrency = (amount) => {
        return new Intl. NumberFormat('fr-FR', {
            style: 'currency',
            currency: 'EUR'
        }).format(amount);
    };

    // Réinitialiser le formulaire
    const resetForm = () => {
        setPin('');
        setAmount('');
        setBeneficiary('');
        setDescription('');
    };

    // Fermer le modal de succès
    const handleCloseSuccess = () => {
        setSuccessModal({ show: false, data: null });
        resetForm();
    };

    // Bloquer sa propre carte
    const handleBlockCard = async () => {
        if (!selectedCardId) return;

        const confirmed = window.confirm('Êtes-vous sûr de vouloir bloquer cette carte ?');
        if (! confirmed) return;

        try {
            const response = await fetch(`${API_BASE}/cards/${selectedCardId}/block`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ reason: 'Blocage demandé par l\'utilisateur' })
            });

            if (response. ok) {
                showAlert('success', 'Succès', 'Votre carte a été bloquée');
                loadCardStatus(selectedCardId);
            }
        } catch (error) {
            showAlert('error', 'Erreur', 'Impossible de bloquer la carte');
        }
    };

    return (
        <div className="card-transaction-container">
            {/* Header */}
            <div className="ct-header">
                <button className="ct-back-btn" onClick={onClose}>
                    ← Retour
                </button>
                <h1>💳 Transaction par Carte</h1>
                <p>Effectuez vos paiements en toute sécurité</p>
            </div>

            {/* Card Status */}
            {cardStatus && (
                <div className={`ct-card-status ${cardStatus.isBlocked ?  'blocked' : 'active'}`}>
                    <div className="ct-status-header">
                        <span className="ct-card-type">{cardStatus. cardType}</span>
                        <span className={`ct-status-badge ${cardStatus.isBlocked ? 'blocked' : 'active'}`}>
                            {cardStatus.isBlocked ?  '🔒 Bloquée' : '✓ Active'}
                        </span>
                    </div>

                    {! cardStatus.isBlocked && (
                        <div className="ct-limits-grid">
                            <div className="ct-limit-item">
                                <span className="ct-limit-label">Limite restante aujourd'hui</span>
                                <span className="ct-limit-value">
                                    {formatCurrency(cardStatus.remainingDailyLimit)}
                                </span>
                            </div>
                            <div className="ct-limit-item">
                                <span className="ct-limit-label">Transactions restantes</span>
                                <span className="ct-limit-value">{cardStatus.remainingDailyTransactions}</span>
                            </div>
                            <div className="ct-limit-item">
                                <span className="ct-limit-label">Limite par transaction</span>
                                <span className="ct-limit-value">
                                    {formatCurrency(cardStatus.singleTransactionLimit)}
                                </span>
                            </div>
                            {cardStatus.failedAttempts > 0 && (
                                <div className="ct-limit-item warning">
                                    <span className="ct-limit-label">⚠️ Tentatives PIN échouées</span>
                                    <span className="ct-limit-value danger">
                                        {cardStatus.failedAttempts}/{cardStatus.maxAttempts}
                                    </span>
                                </div>
                            )}
                        </div>
                    )}

                    {! cardStatus.isBlocked && (
                        <button className="ct-block-btn" onClick={handleBlockCard}>
                            🔒 Bloquer ma carte
                        </button>
                    )}
                </div>
            )}

            {/* Transaction Form */}
            <form className="ct-form" onSubmit={handleSubmit}>
                {/* Card Selection */}
                <div className="ct-form-group">
                    <label htmlFor="cardSelect">Sélectionner une carte</label>
                    {loadingCards ? (
                        <div className="ct-loading-cards">Chargement des cartes...</div>
                    ) : (
                        <select
                            id="cardSelect"
                            value={selectedCardId}
                            onChange={(e) => setSelectedCardId(e. target.value)}
                            required
                            disabled={loading}
                        >
                            <option value="">-- Choisir une carte --</option>
                            {cards.map((card) => (
                                <option key={card.id} value={card. id}>
                                    {card.cardType} - **** {card.lastFourDigits}
                                </option>
                            ))}
                        </select>
                    )}
                </div>

                {/* PIN Input */}
                <div className="ct-form-group">
                    <label htmlFor="pinInput">Code PIN</label>
                    <div className="ct-pin-container">
                        <input
                            type={showPin ? 'text' : 'password'}
                            id="pinInput"
                            value={pin}
                            onChange={(e) => setPin(e. target.value. replace(/\D/g, ''). slice(0, 6))}
                            placeholder="••••"
                            maxLength={6}
                            inputMode="numeric"
                            pattern="[0-9]*"
                            required
                            disabled={loading || cardStatus?.isBlocked}
                            className="ct-pin-input"
                        />
                        <button
                            type="button"
                            className="ct-toggle-pin"
                            onClick={() => setShowPin(!showPin)}
                        >
                            {showPin ? '🙈' : '👁️'}
                        </button>
                    </div>
                    {cardStatus?.failedAttempts > 0 && (
                        <div className="ct-pin-warning">
                            ⚠️ Attention: {cardStatus.remainingAttempts} tentative(s) restante(s) avant blocage
                        </div>
                    )}
                </div>

                {/* Amount Input */}
                <div className="ct-form-group">
                    <label htmlFor="amountInput">Montant</label>
                    <div className="ct-amount-container">
                        <span className="ct-currency-symbol">€</span>
                        <input
                            type="number"
                            id="amountInput"
                            value={amount}
                            onChange={(e) => setAmount(e.target. value)}
                            placeholder="0.00"
                            min="0. 01"
                            step="0.01"
                            required
                            disabled={loading || cardStatus?.isBlocked}
                        />
                    </div>
                    {cardStatus && (
                        <span className="ct-amount-hint">
                            Max par transaction: {formatCurrency(cardStatus. singleTransactionLimit)}
                        </span>
                    )}
                </div>

                {/* Beneficiary Input */}
                <div className="ct-form-group">
                    <label htmlFor="beneficiaryInput">Bénéficiaire</label>
                    <input
                        type="text"
                        id="beneficiaryInput"
                        value={beneficiary}
                        onChange={(e) => setBeneficiary(e.target. value)}
                        placeholder="Nom du bénéficiaire"
                        required
                        disabled={loading || cardStatus?.isBlocked}
                    />
                </div>

                {/* Description Input */}
                <div className="ct-form-group">
                    <label htmlFor="descriptionInput">Description (optionnel)</label>
                    <input
                        type="text"
                        id="descriptionInput"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        placeholder="Motif du paiement"
                        disabled={loading || cardStatus?.isBlocked}
                    />
                </div>

                {/* Submit Button */}
                <button
                    type="submit"
                    className="ct-submit-btn"
                    disabled={loading || ! selectedCardId || cardStatus?.isBlocked}
                >
                    {loading ?  (
                        <>
                            <span className="ct-spinner"></span>
                            Traitement en cours...
                        </>
                    ) : (
                        'Confirmer la transaction'
                    )}
                </button>
            </form>

            {/* Alert */}
            {alert.show && (
                <div className={`ct-alert ${alert.type}`}>
                    <span className="ct-alert-icon">
                        {alert.type === 'error' ? '❌' : alert.type === 'warning' ? '⚠️' : '✅'}
                    </span>
                    <div className="ct-alert-content">
                        <strong>{alert.title}</strong>
                        <p>{alert.message}</p>
                    </div>
                    <button
                        className="ct-alert-close"
                        onClick={() => setAlert({ show: false, type: '', title: '', message: '' })}
                    >
                        ×
                    </button>
                </div>
            )}

            {/* Blocked Modal */}
            {blockedModal. show && (
                <div className="ct-modal-overlay">
                    <div className="ct-modal blocked">
                        <div className="ct-modal-icon">🔒</div>
                        <h2>Carte Bloquée</h2>
                        <p className="ct-modal-reason">{blockedModal. reason}</p>
                        <p className="ct-modal-info">
                            Veuillez contacter le service client pour débloquer votre carte.
                        </p>
                        <div className="ct-modal-actions">
                            <a href="tel:+33123456789" className="ct-modal-btn primary">
                                📞 Appeler le support
                            </a>
                            <button
                                className="ct-modal-btn secondary"
                                onClick={() => setBlockedModal({ show: false, reason: '' })}
                            >
                                Fermer
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Success Modal */}
            {successModal.show && successModal.data && (
                <div className="ct-modal-overlay">
                    <div className="ct-modal success">
                        <div className="ct-modal-icon">✅</div>
                        <h2>Transaction Réussie</h2>
                        <div className="ct-tx-details">
                            <div className="ct-tx-row">
                                <span>Référence:</span>
                                <strong>{successModal.data.referenceNumber}</strong>
                            </div>
                            <div className="ct-tx-row">
                                <span>Montant:</span>
                                <strong>{formatCurrency(successModal.data. amount)}</strong>
                            </div>
                            <div className="ct-tx-row">
                                <span>Date:</span>
                                <strong>
                                    {new Date(successModal.data.transactionDate).toLocaleString('fr-FR')}
                                </strong>
                            </div>
                            <div className="ct-tx-row">
                                <span>Limite restante:</span>
                                <strong>{formatCurrency(successModal.data.remainingDailyLimit)}</strong>
                            </div>
                            <div className="ct-tx-row">
                                <span>Transactions restantes:</span>
                                <strong>{successModal.data.remainingDailyTransactions}</strong>
                            </div>
                        </div>
                        <button className="ct-modal-btn primary" onClick={handleCloseSuccess}>
                            OK
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CardTransaction;