import React, { useState, useEffect, useRef } from "react";
import { transactionService } from "../../services/TransactionService.js";
import "./TransactionManagement.css";

const TransactionManagement = ({ onClose, user }) => {
    // États pour les données
    const [transactions, setTransactions] = useState([]);
    const [filteredTransactions, setFilteredTransactions] = useState([]);
    const [accounts, setAccounts] = useState([]);
    const [cards, setCards] = useState([]);
    const [selectedCard, setSelectedCard] = useState(null);

    // États de chargement
    const [loading, setLoading] = useState(true);
    const [accountsLoading, setAccountsLoading] = useState(true);
    const [cardsLoading, setCardsLoading] = useState(false);
    const [transactionLoading, setTransactionLoading] = useState(false);

    // États d'erreur et succès
    const [error, setError] = useState(null);
    const [transactionError, setTransactionError] = useState(null);
    const [transactionSuccess, setTransactionSuccess] = useState(null);

    // États des filtres
    const [selectedAccount, setSelectedAccount] = useState("all");
    const [filterType, setFilterType] = useState("all");
    const [searchTerm, setSearchTerm] = useState("");
    const [dateRange, setDateRange] = useState({ start: "", end: "" });

    // États du formulaire
    const [showTransactionForm, setShowTransactionForm] = useState(false);
    const [transactionForm, setTransactionForm] = useState({
        accountId: "",
        cardId: "",
        type: "debit",
        amount: "",
        description: "",
        reference: "",
        pin: "",
        otpCode: ""
    });

    // ✅ États pour le flux sécurisé PIN + OTP
    const [transactionStep, setTransactionStep] = useState(1);
    // 1: Formulaire, 2: PIN, 3: OTP, 4: Succès
    const [pinVerified, setPinVerified] = useState(false);
    const [otpSent, setOtpSent] = useState(false);
    const [remainingAttempts, setRemainingAttempts] = useState(3);
    const [cardBlocked, setCardBlocked] = useState(false);
    const [otpCooldown, setOtpCooldown] = useState(0);
    const [userEmail, setUserEmail] = useState("");

    // Référence pour les inputs
    const pinInputRef = useRef(null);
    const otpInputRef = useRef(null);

    // ==================== EFFETS ====================

    useEffect(() => {
        loadInitialData();
    }, []);

    useEffect(() => {
        applyFilters();
    }, [transactions, selectedAccount, filterType, searchTerm, dateRange]);

    useEffect(() => {
        if (accounts.length > 0 && !transactionForm.accountId) {
            setTransactionForm(prev => ({ ...prev, accountId: accounts[0].id. toString() }));
        }
    }, [accounts]);

    useEffect(() => {
        if (transactionForm.accountId) {
            loadCardsForAccount(transactionForm.accountId);
        }
    }, [transactionForm.accountId]);

    // Cooldown pour renvoyer l'OTP
    useEffect(() => {
        if (otpCooldown > 0) {
            const timer = setTimeout(() => setOtpCooldown(otpCooldown - 1), 1000);
            return () => clearTimeout(timer);
        }
    }, [otpCooldown]);

    // Focus automatique sur les inputs
    useEffect(() => {
        if (transactionStep === 2 && pinInputRef.current) {
            pinInputRef. current.focus();
        }
        if (transactionStep === 3 && otpInputRef. current) {
            otpInputRef. current.focus();
        }
    }, [transactionStep]);

    // ==================== CHARGEMENT DES DONNÉES ====================

    const loadInitialData = async () => {
        try {
            setLoading(true);
            setAccountsLoading(true);

            // Charger les comptes
            const accountData = await transactionService.getAccountDetails();
            if (accountData?. accounts) {
                setAccounts(accountData. accounts. map(acc => ({
                    id: acc.id,
                    accountNumber: acc.accountNumber,
                    balance: acc.balance,
                    accountType: acc.accountType
                })));
            }

            // Stocker l'email de l'utilisateur
            if (accountData?.email) {
                setUserEmail(accountData.email);
            } else if (user?.email) {
                setUserEmail(user.email);
            }

            // Charger les transactions
            const transactionsData = await transactionService.getTransactions();
            if (Array.isArray(transactionsData)) {
                setTransactions(transactionsData);
            }

        } catch (err) {
            console.error("Erreur chargement:", err);
            setError(err.message);
        } finally {
            setLoading(false);
            setAccountsLoading(false);
        }
    };

    const loadCardsForAccount = async (accountId) => {
        try {
            setCardsLoading(true);
            const data = await transactionService.getUserCards();

            if (data. success && data.cards) {
                const accountCards = data.cards.filter(
                    card => card.accountId === parseInt(accountId) && card.isActive
                );
                setCards(accountCards);

                if (accountCards.length > 0) {
                    setTransactionForm(prev => ({ ...prev, cardId: accountCards[0].id.toString() }));
                    setSelectedCard(accountCards[0]);
                } else {
                    setTransactionForm(prev => ({ ...prev, cardId: "" }));
                    setSelectedCard(null);
                }
            }
        } catch (err) {
            console.error("Erreur cartes:", err);
            setCards([]);
        } finally {
            setCardsLoading(false);
        }
    };

    // ==================== VALIDATION ====================

    const validateTransactionForm = () => {
        if (! transactionForm.accountId) {
            setTransactionError("Veuillez sélectionner un compte");
            return false;
        }
        if (!transactionForm.cardId) {
            setTransactionError("Veuillez sélectionner une carte");
            return false;
        }
        if (!transactionForm. amount || parseFloat(transactionForm.amount) <= 0) {
            setTransactionError("Veuillez entrer un montant valide supérieur à 0");
            return false;
        }
        if (!transactionForm.description. trim()) {
            setTransactionError("Veuillez entrer une description");
            return false;
        }

        // Vérifier le solde pour les débits
        if (transactionForm.type === "debit") {
            const account = accounts.find(a => a.id === parseInt(transactionForm.accountId));
            if (account && parseFloat(transactionForm.amount) > account.balance) {
                setTransactionError(`Solde insuffisant.  Disponible: ${account. balance. toFixed(2)} MAD`);
                return false;
            }
        }

        return true;
    };

    // ==================== FLUX DE TRANSACTION SÉCURISÉ ====================

    // Étape 1 -> 2: Valider le formulaire et passer au PIN
    const proceedToPin = async () => {
        if (!validateTransactionForm()) return;

        // Vérifier le statut de la carte
        try {
            const cardStatus = await transactionService.getCardStatus(transactionForm. cardId);
            if (cardStatus.blocked) {
                setTransactionError(`Carte bloquée: ${cardStatus.blockReason || "Raison inconnue"}`);
                return;
            }
            if (! cardStatus.active) {
                setTransactionError("Cette carte est inactive");
                return;
            }
        } catch (err) {
            console.error("Erreur statut carte:", err);
        }

        setTransactionStep(2);
        setTransactionError(null);
    };

    // Étape 2: Vérifier le PIN
    const handlePinSubmit = async (e) => {
        e.preventDefault();

        if (!transactionForm.pin || transactionForm.pin.length !== 4) {
            setTransactionError("Le code PIN doit contenir 4 chiffres");
            return;
        }

        setTransactionLoading(true);
        setTransactionError(null);

        try {
            const result = await transactionService.verifyPin(
                parseInt(transactionForm. cardId),
                transactionForm.pin
            );

            if (result.success) {
                // PIN valide -> Envoyer l'OTP
                setPinVerified(true);

                // Envoyer l'OTP
                const otpResult = await transactionService.sendOtp(userEmail);

                if (otpResult.success) {
                    setOtpSent(true);
                    setOtpCooldown(60); // 60 secondes avant de pouvoir renvoyer
                    setTransactionStep(3);
                    setTransactionForm(prev => ({ ...prev, pin: "" })); // Effacer le PIN
                } else {
                    setTransactionError(otpResult.message || "Erreur lors de l'envoi du code OTP");
                }
            } else {
                // PIN invalide
                setRemainingAttempts(result.remainingAttempts || 0);

                if (result.cardBlocked) {
                    setCardBlocked(true);
                    setTransactionError("🔒 Carte bloquée après trop de tentatives incorrectes.  Contactez votre banque.");
                    setTransactionStep(1);
                } else {
                    setTransactionError(`Code PIN incorrect. ${result.remainingAttempts} tentative(s) restante(s)`);
                }
            }
        } catch (err) {
            console. error("Erreur PIN:", err);
            setTransactionError("Erreur de connexion au serveur");
        } finally {
            setTransactionLoading(false);
        }
    };

    // Étape 3: Vérifier l'OTP et exécuter la transaction
    const handleOtpSubmit = async (e) => {
        e.preventDefault();

        if (! transactionForm.otpCode || transactionForm.otpCode. length !== 6) {
            setTransactionError("Le code OTP doit contenir 6 chiffres");
            return;
        }

        setTransactionLoading(true);
        setTransactionError(null);

        try {
            // Vérifier l'OTP
            const otpResult = await transactionService. verifyOtp(userEmail, transactionForm.otpCode);

            if (! otpResult.success) {
                setTransactionError(otpResult.message || "Code OTP invalide ou expiré");
                setTransactionLoading(false);
                return;
            }

            // OTP valide -> Exécuter la transaction
            const transactionResult = await transactionService.processSecureTransaction({
                cardId: parseInt(transactionForm. cardId),
                pin: transactionForm. pin, // Le PIN a déjà été vérifié mais requis par l'API
                amount: parseFloat(transactionForm.amount),
                type: transactionForm. type,
                description: transactionForm. description. trim(),
                reference: transactionForm. reference.trim() || null
            });

            if (transactionResult. success) {
                // Transaction réussie!
                setTransactionStep(4);
                setTransactionSuccess({
                    message: "Transaction effectuée avec succès! ",
                    referenceNumber: transactionResult.referenceNumber,
                    amount: transactionResult. amount,
                    type: transactionForm.type,
                    transactionDate: transactionResult.transactionDate,
                    remainingDailyLimit: transactionResult. remainingDailyLimit,
                    remainingDailyTransactions: transactionResult.remainingDailyTransactions
                });

                // Rafraîchir les données
                await loadInitialData();

                // Fermer automatiquement après 5 secondes
                setTimeout(() => {
                    resetForm();
                    setShowTransactionForm(false);
                }, 5000);

            } else {
                // Gérer les erreurs de transaction
                handleTransactionError(transactionResult);
            }

        } catch (err) {
            console.error("Erreur transaction:", err);
            setTransactionError("Erreur lors de l'exécution de la transaction");
        } finally {
            setTransactionLoading(false);
        }
    };

    // Gérer les erreurs de transaction
    const handleTransactionError = (result) => {
        switch (result.errorType) {
            case 'INVALID_PIN':
                setRemainingAttempts(result.remainingAttempts || 0);
                if (result.cardBlocked) {
                    setCardBlocked(true);
                    setTransactionError("🔒 Carte bloquée.  Contactez votre banque.");
                    setTransactionStep(1);
                } else {
                    setTransactionError(`PIN incorrect. ${result. remainingAttempts} tentative(s) restante(s)`);
                }
                break;
            case 'CARD_BLOCKED':
                setCardBlocked(true);
                setTransactionError(`🔒 Carte bloquée: ${result.reason || "Raison inconnue"}`);
                setTransactionStep(1);
                break;
            case 'LIMIT_EXCEEDED':
                setTransactionError(`Limite dépassée: ${result.message}`);
                setTransactionStep(1);
                break;
            default:
                setTransactionError(result.message || "Erreur lors de la transaction");
        }
    };

    // Renvoyer l'OTP
    const resendOtp = async () => {
        if (otpCooldown > 0) return;

        setTransactionLoading(true);
        try {
            const result = await transactionService.sendOtp(userEmail);
            if (result.success) {
                setOtpCooldown(60);
                setTransactionError(null);
            } else {
                setTransactionError(result.message || "Erreur lors de l'envoi");
            }
        } catch (err) {
            setTransactionError("Erreur de connexion");
        } finally {
            setTransactionLoading(false);
        }
    };

    // Annuler la transaction
    const cancelTransaction = () => {
        resetForm();
    };

    // Réinitialiser le formulaire
    const resetForm = () => {
        setTransactionForm({
            accountId: accounts. length > 0 ?  accounts[0].id. toString() : "",
            cardId: "",
            type: "debit",
            amount: "",
            description: "",
            reference: "",
            pin: "",
            otpCode: ""
        });
        setTransactionStep(1);
        setTransactionError(null);
        setTransactionSuccess(null);
        setPinVerified(false);
        setOtpSent(false);
        setCardBlocked(false);
        setOtpCooldown(0);
    };

    // ==================== GESTIONNAIRES D'ÉVÉNEMENTS ====================

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setTransactionForm(prev => ({ ...prev, [name]: value }));
        setTransactionError(null);

        if (name === "cardId") {
            const card = cards.find(c => c.id === parseInt(value));
            setSelectedCard(card);
            setPinVerified(false);
            setTransactionForm(prev => ({ ... prev, pin: "", otpCode: "" }));
        }
    };

    // ==================== FILTRES ====================

    const applyFilters = () => {
        let filtered = [... transactions];

        if (selectedAccount !== "all") {
            filtered = filtered. filter(t => t.accountId === parseInt(selectedAccount));
        }
        if (filterType !== "all") {
            filtered = filtered.filter(t => t.type?. toLowerCase() === filterType.toLowerCase());
        }
        if (searchTerm) {
            const searchLower = searchTerm.toLowerCase();
            filtered = filtered. filter(t =>
                t.description?. toLowerCase().includes(searchLower) ||
                t.reference?.toLowerCase().includes(searchLower)
            );
        }
        if (dateRange. start) {
            filtered = filtered.filter(t => new Date(t.date) >= new Date(dateRange.start));
        }
        if (dateRange. end) {
            const endDate = new Date(dateRange.end);
            endDate.setHours(23, 59, 59, 999);
            filtered = filtered.filter(t => new Date(t. date) <= endDate);
        }

        setFilteredTransactions(filtered);
    };

    // ==================== UTILITAIRES ====================

    const getTotalsByType = () => {
        const totals = { credit: 0, debit: 0, balance: 0 };
        filteredTransactions.forEach(t => {
            const amount = parseFloat(t.amount) || 0;
            if (t.type?. toLowerCase() === "credit") totals.credit += amount;
            else if (t. type?.toLowerCase() === "debit") totals.debit += amount;
        });
        totals.balance = totals.credit - totals.debit;
        return totals;
    };

    const formatDate = (dateString) => {
        try {
            return new Date(dateString). toLocaleDateString("fr-FR", {
                day: "2-digit", month: "short", year: "numeric",
                hour: "2-digit", minute: "2-digit"
            });
        } catch (e) { return dateString; }
    };

    const formatAccountType = (type) => {
        const types = { 'COURANT': 'Courant', 'EPARGNE': 'Épargne', 'PROFESSIONNEL': 'Professionnel' };
        return types[type] || type;
    };

    const formatCardType = (type) => {
        const types = {
            'VISA': '💳 Visa',
            'MASTERCARD': '💳 Mastercard',
            'VISA_GOLD': '🌟 Visa Gold',
            'MASTERCARD_PLATINUM': '💎 Mastercard Platinum'
        };
        return types[type] || `💳 ${type}`;
    };

    const getSelectedAccountBalance = () => {
        const account = accounts.find(a => a.id === parseInt(transactionForm.accountId));
        return account?. balance || null;
    };

    const exportToCSV = () => {
        if (filteredTransactions.length === 0) {
            alert("Aucune transaction à exporter");
            return;
        }

        const headers = ["Date", "Description", "Type", "Montant", "Référence", "Compte"];
        const csvData = filteredTransactions.map(t => {
            const account = accounts.find(a => a.id === t.accountId);
            return [
                new Date(t.date).toLocaleDateString("fr-FR"),
                `"${t.description}"`,
                t.type === "credit" ? "Crédit" : "Débit",
                t.amount?. toFixed(2) || "0. 00",
                t.reference || "N/A",
                account?.accountNumber || "N/A"
            ];
        });

        const csvContent = [headers.join(","), ...csvData. map(row => row.join(","))]. join("\n");
        const blob = new Blob(["\uFEFF" + csvContent], { type: "text/csv;charset=utf-8;" });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `transactions_${new Date().toISOString().split("T")[0]}. csv`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL. revokeObjectURL(url);
    };

    const totals = getTotalsByType();

    // ==================== RENDU ====================

    if (loading && accountsLoading) {
        return (
            <div className="transaction-management">
                <div className="loading-spinner">
                    <div className="spinner"></div>
                    <p>Chargement des données...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="transaction-management">
                <div className="error-container">
                    <div className="error-icon">⚠️</div>
                    <div className="error-message">{error}</div>
                    <div className="error-actions">
                        <button onClick={loadInitialData} className="action-btn primary">
                            🔄 Réessayer
                        </button>
                        <button onClick={onClose} className="action-btn secondary">
                            ← Retour
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="transaction-management">
            {/* Header */}
            <div className="transaction-header">
                <div className="header-left">
                    <button onClick={onClose} className="back-btn" title="Retour">
                        ← Retour
                    </button>
                    <h2>📋 Gestion des Transactions</h2>
                </div>
                <div className="header-actions">
                    <button
                        onClick={() => {
                            setShowTransactionForm(! showTransactionForm);
                            if (! showTransactionForm) resetForm();
                        }}
                        className={`action-btn ${showTransactionForm ?  'secondary' : 'success'}`}
                    >
                        {showTransactionForm ?  '✕ Fermer' : '➕ Nouvelle Transaction'}
                    </button>
                    <button onClick={loadInitialData} className="action-btn secondary" title="Actualiser">
                        🔄 Actualiser
                    </button>
                    <button
                        onClick={exportToCSV}
                        className="action-btn primary"
                        disabled={filteredTransactions.length === 0}
                    >
                        📥 Exporter CSV
                    </button>
                </div>
            </div>

            {/* ==================== FORMULAIRE DE TRANSACTION SÉCURISÉ ==================== */}
            {showTransactionForm && (
                <div className="transaction-form-container">
                    <div className="transaction-form-card secure">
                        <div className="form-header">
                            <h3>🔐 Transaction Sécurisée</h3>
                            <div className="security-badges">
                                <span className="badge pin">🔢 PIN</span>
                                <span className="badge otp">📧 OTP Email</span>
                            </div>
                            <button className="close-form-btn" onClick={() => setShowTransactionForm(false)}>✕</button>
                        </div>

                        {/* Indicateur d'étapes */}
                        <div className="transaction-steps">
                            <div className={`step ${transactionStep >= 1 ? 'active' : ''} ${transactionStep > 1 ? 'completed' : ''}`}>
                                <span className="step-number">{transactionStep > 1 ? '✓' : '1'}</span>
                                <span className="step-label">Détails</span>
                            </div>
                            <div className="step-line"></div>
                            <div className={`step ${transactionStep >= 2 ? 'active' : ''} ${transactionStep > 2 ? 'completed' : ''}`}>
                                <span className="step-number">{transactionStep > 2 ? '✓' : '2'}</span>
                                <span className="step-label">Code PIN</span>
                            </div>
                            <div className="step-line"></div>
                            <div className={`step ${transactionStep >= 3 ? 'active' : ''} ${transactionStep > 3 ? 'completed' : ''}`}>
                                <span className="step-number">{transactionStep > 3 ?  '✓' : '3'}</span>
                                <span className="step-label">Code OTP</span>
                            </div>
                            <div className="step-line"></div>
                            <div className={`step ${transactionStep >= 4 ? 'active success' : ''}`}>
                                <span className="step-number">✓</span>
                                <span className="step-label">Confirmé</span>
                            </div>
                        </div>

                        {/* Message d'erreur */}
                        {transactionError && (
                            <div className="form-error">
                                <span>⚠️</span> {transactionError}
                            </div>
                        )}

                        {/* ========== ÉTAPE 1: Formulaire des détails ========== */}
                        {transactionStep === 1 && ! transactionSuccess && (
                            <form onSubmit={(e) => { e.preventDefault(); proceedToPin(); }} className="transaction-form">
                                <div className="form-row">
                                    <div className="form-group">
                                        <label htmlFor="accountId">🏦 Compte</label>
                                        {accountsLoading ?  (
                                            <div className="loading-text">Chargement...</div>
                                        ) : accounts.length === 0 ? (
                                            <div className="warning-text">Aucun compte disponible</div>
                                        ) : (
                                            <select
                                                id="accountId"
                                                name="accountId"
                                                value={transactionForm.accountId}
                                                onChange={handleInputChange}
                                                className="form-select"
                                            >
                                                <option value="">Sélectionner un compte</option>
                                                {accounts. map(acc => (
                                                    <option key={acc.id} value={acc.id}>
                                                        {acc.accountNumber} ({formatAccountType(acc. accountType)}) - {acc.balance?. toFixed(2)} MAD
                                                    </option>
                                                ))}
                                            </select>
                                        )}
                                    </div>

                                    <div className="form-group">
                                        <label htmlFor="cardId">💳 Carte</label>
                                        {cardsLoading ? (
                                            <div className="loading-text">Chargement des cartes...</div>
                                        ) : cards.length === 0 ? (
                                            <div className="warning-text">⚠️ Aucune carte active pour ce compte</div>
                                        ) : (
                                            <select
                                                id="cardId"
                                                name="cardId"
                                                value={transactionForm.cardId}
                                                onChange={handleInputChange}
                                                className="form-select"
                                            >
                                                <option value="">Sélectionner une carte</option>
                                                {cards.map(card => (
                                                    <option key={card. id} value={card.id}>
                                                        {formatCardType(card. cardType)}
                                                    </option>
                                                ))}
                                            </select>
                                        )}
                                    </div>
                                </div>

                                <div className="form-row">
                                    <div className="form-group">
                                        <label>📊 Type de Transaction</label>
                                        <div className="type-selector">
                                            <button
                                                type="button"
                                                className={`type-btn debit ${transactionForm.type === 'debit' ?  'active' : ''}`}
                                                onClick={() => setTransactionForm(prev => ({ ... prev, type: 'debit' }))}
                                            >
                                                💸 Débit (Retrait)
                                            </button>
                                            <button
                                                type="button"
                                                className={`type-btn credit ${transactionForm.type === 'credit' ? 'active' : ''}`}
                                                onClick={() => setTransactionForm(prev => ({ ... prev, type: 'credit' }))}
                                            >
                                                💰 Crédit (Dépôt)
                                            </button>
                                        </div>
                                    </div>

                                    <div className="form-group">
                                        <label htmlFor="amount">💵 Montant (MAD)</label>
                                        <input
                                            type="number"
                                            id="amount"
                                            name="amount"
                                            value={transactionForm. amount}
                                            onChange={handleInputChange}
                                            placeholder="0.00"
                                            min="0. 01"
                                            step="0. 01"
                                            className="form-input"
                                        />
                                        {getSelectedAccountBalance() !== null && (
                                            <span className="balance-hint">
                                                Solde disponible: <strong>{getSelectedAccountBalance()?.toFixed(2)} MAD</strong>
                                            </span>
                                        )}
                                    </div>
                                </div>

                                <div className="form-group full-width">
                                    <label htmlFor="description">📝 Description</label>
                                    <textarea
                                        id="description"
                                        name="description"
                                        value={transactionForm.description}
                                        onChange={handleInputChange}
                                        placeholder="Décrivez la transaction..."
                                        maxLength="500"
                                        rows="2"
                                        className="form-textarea"
                                    />
                                    <span className="char-count">{transactionForm.description. length}/500</span>
                                </div>

                                <div className="form-group">
                                    <label htmlFor="reference">🔖 Référence (optionnel)</label>
                                    <input
                                        type="text"
                                        id="reference"
                                        name="reference"
                                        value={transactionForm.reference}
                                        onChange={handleInputChange}
                                        placeholder="Ex: FAC-2024-001"
                                        maxLength="100"
                                        className="form-input"
                                    />
                                </div>

                                {/* Aperçu de la transaction */}
                                {transactionForm.amount && parseFloat(transactionForm.amount) > 0 && (
                                    <div className={`transaction-preview ${transactionForm.type}`}>
                                        <div className="preview-icon">
                                            {transactionForm.type === 'credit' ? '💰' : '💸'}
                                        </div>
                                        <div className="preview-details">
                                            <span className="preview-type">
                                                {transactionForm.type === 'credit' ?  'Crédit' : 'Débit'}
                                            </span>
                                            <span className="preview-amount">
                                                {transactionForm.type === 'credit' ? '+' : '-'}
                                                {parseFloat(transactionForm.amount).toFixed(2)} MAD
                                            </span>
                                        </div>
                                    </div>
                                )}

                                <div className="form-actions">
                                    <button type="button" onClick={resetForm} className="action-btn secondary">
                                        🗑️ Réinitialiser
                                    </button>
                                    <button
                                        type="submit"
                                        className="action-btn primary"
                                        disabled={cards.length === 0 || ! transactionForm.cardId}
                                    >
                                        🔐 Continuer →
                                    </button>
                                </div>
                            </form>
                        )}

                        {/* ========== ÉTAPE 2: Vérification du PIN ========== */}
                        {transactionStep === 2 && (
                            <div className="security-step">
                                <div className="step-icon">🔐</div>
                                <h4>Vérification du code PIN</h4>
                                <p>Entrez le code PIN à 4 chiffres de votre carte</p>

                                <div className="transaction-summary-mini">
                                    <span>{transactionForm.type === 'credit' ? '💰 Crédit' : '💸 Débit'}</span>
                                    <strong className={transactionForm.type}>
                                        {transactionForm.type === 'credit' ? '+' : '-'}
                                        {parseFloat(transactionForm.amount).toFixed(2)} MAD
                                    </strong>
                                </div>

                                <form onSubmit={handlePinSubmit}>
                                    <div className="pin-input-group">
                                        <input
                                            type="password"
                                            name="pin"
                                            ref={pinInputRef}
                                            value={transactionForm.pin}
                                            onChange={handleInputChange}
                                            placeholder="••••"
                                            maxLength="4"
                                            pattern="[0-9]{4}"
                                            inputMode="numeric"
                                            className="pin-input"
                                            autoComplete="off"
                                        />
                                    </div>

                                    <div className="form-actions">
                                        <button
                                            type="button"
                                            onClick={() => { setTransactionStep(1); setTransactionForm(prev => ({ ...prev, pin: "" })); }}
                                            className="action-btn secondary"
                                            disabled={transactionLoading}
                                        >
                                            ← Retour
                                        </button>
                                        <button
                                            type="submit"
                                            className="action-btn primary"
                                            disabled={transactionLoading || transactionForm.pin. length !== 4}
                                        >
                                            {transactionLoading ?  (
                                                <><span className="btn-spinner"></span> Vérification...</>
                                            ) : (
                                                'Vérifier le PIN →'
                                            )}
                                        </button>
                                    </div>
                                </form>
                            </div>
                        )}

                        {/* ========== ÉTAPE 3: Vérification OTP ========== */}
                        {transactionStep === 3 && (
                            <div className="security-step">
                                <div className="step-icon">📧</div>
                                <h4>Confirmation par email</h4>
                                <p>
                                    Un code de vérification à 6 chiffres a été envoyé à<br/>
                                    <strong>{transactionService.maskEmail(userEmail)}</strong>
                                </p>

                                <div className="transaction-summary-mini">
                                    <span>{transactionForm.type === 'credit' ? '💰 Crédit' : '💸 Débit'}</span>
                                    <strong className={transactionForm.type}>
                                        {transactionForm.type === 'credit' ? '+' : '-'}
                                        {parseFloat(transactionForm.amount). toFixed(2)} MAD
                                    </strong>
                                </div>

                                <form onSubmit={handleOtpSubmit}>
                                    <div className="otp-input-group">
                                        <input
                                            type="text"
                                            name="otpCode"
                                            ref={otpInputRef}
                                            value={transactionForm.otpCode}
                                            onChange={handleInputChange}
                                            placeholder="000000"
                                            maxLength="6"
                                            pattern="[0-9]{6}"
                                            inputMode="numeric"
                                            className="otp-input"
                                            autoComplete="off"
                                        />
                                    </div>

                                    <div className="resend-otp">
                                        {otpCooldown > 0 ? (
                                            <span className="cooldown">
                                                Renvoyer le code dans {otpCooldown}s
                                            </span>
                                        ) : (
                                            <button
                                                type="button"
                                                onClick={resendOtp}
                                                className="resend-btn"
                                                disabled={transactionLoading}
                                            >
                                                📧 Renvoyer le code
                                            </button>
                                        )}
                                    </div>

                                    <div className="form-actions">
                                        <button
                                            type="button"
                                            onClick={cancelTransaction}
                                            className="action-btn secondary"
                                            disabled={transactionLoading}
                                        >
                                            Annuler
                                        </button>
                                        <button
                                            type="submit"
                                            className="action-btn success"
                                            disabled={transactionLoading || transactionForm.otpCode.length !== 6}
                                        >
                                            {transactionLoading ? (
                                                <><span className="btn-spinner"></span> Confirmation...</>
                                            ) : (
                                                '✓ Confirmer la transaction'
                                            )}
                                        </button>
                                    </div>
                                </form>
                            </div>
                        )}

                        {/* ========== ÉTAPE 4: Succès ========== */}
                        {transactionStep === 4 && transactionSuccess && (
                            <div className="success-step">
                                <div className="success-icon">✅</div>
                                <h4>Transaction réussie! </h4>

                                <div className="success-details">
                                    <div className="detail-row">
                                        <span>Référence:</span>
                                        <strong>{transactionSuccess.referenceNumber}</strong>
                                    </div>
                                    <div className="detail-row">
                                        <span>Type:</span>
                                        <strong>{transactionSuccess.type === 'credit' ?  '💰 Crédit' : '💸 Débit'}</strong>
                                    </div>
                                    <div className="detail-row">
                                        <span>Montant:</span>
                                        <strong className={transactionSuccess. type}>
                                            {transactionSuccess.type === 'credit' ? '+' : '-'}
                                            {transactionSuccess.amount?. toFixed(2)} MAD
                                        </strong>
                                    </div>
                                    {transactionSuccess.remainingDailyLimit && (
                                        <div className="detail-row">
                                            <span>Limite journalière restante:</span>
                                            <strong>{transactionSuccess.remainingDailyLimit?. toFixed(2)} MAD</strong>
                                        </div>
                                    )}
                                    {transactionSuccess.remainingDailyTransactions && (
                                        <div className="detail-row">
                                            <span>Transactions restantes:</span>
                                            <strong>{transactionSuccess. remainingDailyTransactions}</strong>
                                        </div>
                                    )}
                                </div>

                                <p className="email-notice">
                                    📧 Un email de confirmation a été envoyé à votre adresse
                                </p>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {/* ==================== CARTES RÉCAPITULATIVES ==================== */}
            <div className="transaction-summary">
                <div className="summary-card credit">
                    <div className="summary-icon">💰</div>
                    <div className="summary-details">
                        <span className="summary-label">Total Crédits</span>
                        <span className="summary-amount">+{totals.credit. toFixed(2)} MAD</span>
                        <span className="summary-count">
                            {filteredTransactions.filter(t => t. type?. toLowerCase() === "credit").length} transaction(s)
                        </span>
                    </div>
                </div>
                <div className="summary-card debit">
                    <div className="summary-icon">💸</div>
                    <div className="summary-details">
                        <span className="summary-label">Total Débits</span>
                        <span className="summary-amount">-{totals. debit.toFixed(2)} MAD</span>
                        <span className="summary-count">
                            {filteredTransactions.filter(t => t.type?.toLowerCase() === "debit").length} transaction(s)
                        </span>
                    </div>
                </div>
                <div className="summary-card balance">
                    <div className="summary-icon">📊</div>
                    <div className="summary-details">
                        <span className="summary-label">Solde Net</span>
                        <span className={`summary-amount ${totals.balance >= 0 ? "positive" : "negative"}`}>
                            {totals. balance >= 0 ? "+" : ""}{totals.balance.toFixed(2)} MAD
                        </span>
                        <span className="summary-count">
                            {filteredTransactions.length} transaction(s)
                        </span>
                    </div>
                </div>
            </div>

            {/* ==================== FILTRES ==================== */}
            <div className="transaction-filters">
                <div className="filter-group">
                    <label>🏦 Compte</label>
                    <select
                        value={selectedAccount}
                        onChange={(e) => setSelectedAccount(e.target.value)}
                        className="filter-select"
                    >
                        <option value="all">Tous les comptes</option>
                        {accounts. map(acc => (
                            <option key={acc.id} value={acc.id}>
                                {acc. accountNumber} - {acc.balance?.toFixed(2)} MAD
                            </option>
                        ))}
                    </select>
                </div>

                <div className="filter-group">
                    <label>💳 Type</label>
                    <select
                        value={filterType}
                        onChange={(e) => setFilterType(e.target.value)}
                        className="filter-select"
                    >
                        <option value="all">Tous</option>
                        <option value="credit">Crédits</option>
                        <option value="debit">Débits</option>
                    </select>
                </div>

                <div className="filter-group">
                    <label>🔍 Recherche</label>
                    <input
                        type="text"
                        placeholder="Description, référence..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="filter-input"
                    />
                </div>

                <div className="filter-group">
                    <label>📅 Du</label>
                    <input
                        type="date"
                        value={dateRange.start}
                        onChange={(e) => setDateRange({ ...dateRange, start: e.target. value })}
                        className="filter-input"
                    />
                </div>

                <div className="filter-group">
                    <label>📅 Au</label>
                    <input
                        type="date"
                        value={dateRange.end}
                        onChange={(e) => setDateRange({ ...dateRange, end: e. target.value })}
                        className="filter-input"
                    />
                </div>
            </div>

            {/* ==================== LISTE DES TRANSACTIONS ==================== */}
            <div className="transactions-container">
                <div className="transactions-count">
                    {filteredTransactions. length} transaction(s) trouvée(s)
                    {transactions.length > filteredTransactions.length &&
                        ` sur ${transactions.length} au total`
                    }
                </div>

                {filteredTransactions.length === 0 ?  (
                    <div className="empty-state">
                        <div className="empty-icon">🔍</div>
                        <h3>Aucune transaction trouvée</h3>
                        {transactions.length === 0 ? (
                            <>
                                <p>Vous n'avez aucune transaction enregistrée</p>
                                <button
                                    className="action-btn success"
                                    onClick={() => setShowTransactionForm(true)}
                                >
                                    ➕ Créer votre première transaction
                                </button>
                            </>
                        ) : (
                            <p>Essayez de modifier vos filtres</p>
                        )}
                    </div>
                ) : (
                    <div className="transactions-table-wrapper">
                        <table className="transactions-table">
                            <thead>
                            <tr>
                                <th>Date</th>
                                <th>Description</th>
                                <th>Référence</th>
                                <th>Type</th>
                                <th>Montant</th>
                                <th>Compte</th>
                            </tr>
                            </thead>
                            <tbody>
                            {filteredTransactions.map(transaction => (
                                <tr key={transaction.id} className="transaction-row">
                                    <td className="date-cell">
                                        {formatDate(transaction.date)}
                                    </td>
                                    <td className="description-cell">
                                        <div className="description-content">
                                                <span className="transaction-icon">
                                                    {transaction.type?.toLowerCase() === "credit" ? "💰" : "💸"}
                                                </span>
                                            <span>{transaction.description || "Sans description"}</span>
                                        </div>
                                    </td>
                                    <td className="reference-cell">
                                        {transaction.reference || "—"}
                                    </td>
                                    <td className="type-cell">
                                            <span className={`type-badge ${transaction. type?. toLowerCase()}`}>
                                                {transaction.type?.toLowerCase() === "credit" ?  "Crédit" : "Débit"}
                                            </span>
                                    </td>
                                    <td className={`amount-cell ${transaction.type?.toLowerCase()}`}>
                                        {transaction.type?.toLowerCase() === "credit" ? "+" : "-"}
                                        {parseFloat(transaction.amount). toFixed(2)} MAD
                                    </td>
                                    <td className="account-cell">
                                        {accounts.find(a => a.id === transaction.accountId)?.accountNumber || "N/A"}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};

export default TransactionManagement;