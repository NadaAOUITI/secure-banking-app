import React, { useState, useEffect } from 'react';
import transferService from '../../services/TransferService';
import beneficiaryService from '../../services/BeneficiaryService';
import './TransferManagement.css';

// Enhanced Modal Component
const Modal = ({ isOpen, onClose, type, title, children, actions }) => {
    if (!isOpen) return null;

    const getIcon = () => {
        switch (type) {
            case 'success': return '✅';
            case 'error': return '❌';
            case 'warning': return '⚠️';
            case 'confirm': return '🔐';
            case 'loading': return null;
            case 'info': return 'ℹ️';
            default: return '📋';
        }
    };

    const getIconClass = () => {
        return `modal-icon modal-icon-${type}`;
    };

    return (
        <div className="modal-overlay" onClick={type !== 'loading' ?  onClose : undefined}>
            <div className={`modal-container modal-${type}`} onClick={(e) => e.stopPropagation()}>
                {type !== 'loading' && (
                    <button className="modal-close-btn" onClick={onClose}>×</button>
                )}
                <div className={getIconClass()}>
                    {type === 'loading' ? (
                        <div className="spinner-container">
                            <div className="spinner"></div>
                            <div className="spinner-glow"></div>
                        </div>
                    ) : (
                        <span className="icon-bounce">{getIcon()}</span>
                    )}
                </div>
                <h3 className="modal-title">{title}</h3>
                <div className="modal-content">
                    {children}
                </div>
                {actions && (
                    <div className="modal-actions">
                        {actions}
                    </div>
                )}
            </div>
        </div>
    );
};

const TransferManagement = ({ onClose, accounts: propAccounts = [] }) => {
    const [step, setStep] = useState('form');
    const [beneficiaries, setBeneficiaries] = useState([]);
    const [transfers, setTransfers] = useState([]);
    const [accounts, setAccounts] = useState(propAccounts);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Modal states
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [showSuccessModal, setShowSuccessModal] = useState(false);
    const [showErrorModal, setShowErrorModal] = useState(false);
    const [showLoadingModal, setShowLoadingModal] = useState(false);
    const [showCancelConfirmModal, setShowCancelConfirmModal] = useState(false);
    const [modalMessage, setModalMessage] = useState('');

    const [formData, setFormData] = useState({
        senderAccountId: '',
        beneficiaryId: '',
        amount: '',
        description: ''
    });

    const [pendingTransfer, setPendingTransfer] = useState(null);
    const [otpCode, setOtpCode] = useState('');
    const [completedTransfer, setCompletedTransfer] = useState(null);

    useEffect(() => {
        loadBeneficiaries();
        loadTransferHistory();
        if (propAccounts. length === 0) {
            loadUserAccounts();
        }
    }, []);

    const loadUserAccounts = async () => {
        try {
            const result = await transferService.getUserAccounts();
            console.log('Accounts loaded:', result);

            if (result.success && result.accounts.length > 0) {
                setAccounts(result. accounts);
                setError('');
            } else if (propAccounts.length > 0) {
                setAccounts(propAccounts);
            } else {
                console.warn('No accounts found');
            }
        } catch (error) {
            console.error('Error loading accounts:', error);
            if (propAccounts. length > 0) {
                setAccounts(propAccounts);
            } else {
                showError('Impossible de charger vos comptes');
            }
        }
    };

    const loadBeneficiaries = async () => {
        const result = await beneficiaryService.getBeneficiaries();
        if (result.success) {
            setBeneficiaries(result.beneficiaries || []);
        }
    };

    const loadTransferHistory = async () => {
        const result = await transferService.getTransferHistory();
        if (result.success) {
            setTransfers(result.transfers || []);
        }
    };

    // Modal helper functions
    const showError = (message) => {
        setModalMessage(message);
        setShowErrorModal(true);
    };

    const showSuccessMessage = (message) => {
        setModalMessage(message);
        setShowSuccessModal(true);
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        setError('');
    };

    // Calculate fees
    const calculateFees = (beneficiary, amount) => {
        if (!beneficiary || ! amount) return 0;
        const amt = parseFloat(amount);
        if (beneficiary.bankType === 'SAME_BANK') return 0;
        if (beneficiary.bankType === 'NATIONAL') return 2.5;
        return Math.max(10, amt * 0.01);
    };

    // Validate transfer before showing confirmation
    const validateTransfer = () => {
        if (!formData.senderAccountId || ! formData.beneficiaryId || !formData.amount) {
            showError('Veuillez remplir tous les champs obligatoires');
            return false;
        }

        const amount = parseFloat(formData.amount);

        if (amount < 1) {
            showError('Le montant minimum est 1 TND');
            return false;
        }

        if (amount > 50000) {
            showError('Le montant maximum par virement est de 50,000 TND');
            return false;
        }

        if (selectedAccount && amount > parseFloat(selectedAccount. balance)) {
            showError(`Solde insuffisant.  Votre solde disponible est de ${formatBalance(selectedAccount.balance)} TND`);
            return false;
        }

        return true;
    };

    // Show confirmation modal before initiating transfer
    const handleShowConfirmation = (e) => {
        e.preventDefault();
        if (validateTransfer()) {
            setShowConfirmModal(true);
        }
    };

    // Initiate transfer after confirmation
    const handleInitiateTransfer = async () => {
        setShowConfirmModal(false);
        setShowLoadingModal(true);

        try {
            const result = await transferService.initiateTransfer({
                senderAccountId: parseInt(formData.senderAccountId),
                beneficiaryId: parseInt(formData.beneficiaryId),
                amount: parseFloat(formData.amount),
                description: formData. description
            });

            setShowLoadingModal(false);

            if (result. success) {
                setPendingTransfer(result.transfer);
                setStep('otp');
            } else {
                showError(result.message || 'Erreur lors de l\'initiation du virement');
            }
        } catch (error) {
            setShowLoadingModal(false);
            showError('Erreur de connexion au serveur');
        }
    };

    const handleConfirmOtp = async (e) => {
        e.preventDefault();

        if (!otpCode || otpCode.length !== 6) {
            showError('Veuillez entrer un code OTP valide (6 chiffres)');
            return;
        }

        setShowLoadingModal(true);

        try {
            const result = await transferService.confirmTransfer(
                pendingTransfer.reference,
                otpCode
            );

            setShowLoadingModal(false);

            if (result.success) {
                setCompletedTransfer({
                    ... pendingTransfer,
                    ... result. transfer
                });
                setShowSuccessModal(true);
                loadTransferHistory();
                loadUserAccounts();
            } else {
                showError(result. message || 'Code OTP invalide');
            }
        } catch (error) {
            setShowLoadingModal(false);
            showError('Erreur de connexion au serveur');
        }
    };

    const handleShowCancelConfirm = () => {
        setShowCancelConfirmModal(true);
    };

    const handleCancelTransfer = async () => {
        setShowCancelConfirmModal(false);
        setShowLoadingModal(true);

        try {
            if (pendingTransfer) {
                await transferService.cancelTransfer(pendingTransfer.reference);
            }
            setShowLoadingModal(false);
            resetForm();
            showSuccessMessage('Virement annulé avec succès');
        } catch (error) {
            setShowLoadingModal(false);
            showError('Erreur lors de l\'annulation');
        }
    };

    const resetForm = () => {
        setFormData({
            senderAccountId: '',
            beneficiaryId: '',
            amount: '',
            description: ''
        });
        setPendingTransfer(null);
        setCompletedTransfer(null);
        setOtpCode('');
        setStep('form');
        setError('');
        setSuccess('');
    };

    const handleSuccessModalClose = () => {
        setShowSuccessModal(false);
        resetForm();
    };

    const handleNewTransfer = () => {
        setShowSuccessModal(false);
        resetForm();
    };

    const handleViewHistory = () => {
        setShowSuccessModal(false);
        resetForm();
        setStep('history');
    };

    const getStatusColor = (status) => {
        const colors = {
            'COMPLETED': '#2ecc71',
            'PENDING': '#f39c12',
            'OTP_REQUIRED': '#3498db',
            'PROCESSING': '#9b59b6',
            'FAILED': '#e74c3c',
            'CANCELLED': '#95a5a6'
        };
        return colors[status] || '#666';
    };

    const getStatusIcon = (status) => {
        const icons = {
            'COMPLETED': '✅',
            'PENDING': '⏳',
            'OTP_REQUIRED': '🔐',
            'PROCESSING': '⚙️',
            'FAILED': '❌',
            'CANCELLED': '🚫'
        };
        return icons[status] || '📋';
    };

    const formatDate = (dateString) => {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-FR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const formatBalance = (balance) => {
        if (balance === undefined || balance === null) return '0. 000';
        return parseFloat(balance).toFixed(3);
    };

    const selectedBeneficiary = beneficiaries.find(b => b.id === parseInt(formData.beneficiaryId));
    const selectedAccount = accounts.find(a => a.id === parseInt(formData. senderAccountId));

    const fees = calculateFees(selectedBeneficiary, formData.amount);
    const totalAmount = formData.amount ?  parseFloat(formData.amount) + fees : 0;

    return (
        <div className="transfer-management">
            {/* Animated Background */}
            <div className="animated-bg">
                <div className="floating-shape shape-1"></div>
                <div className="floating-shape shape-2"></div>
                <div className="floating-shape shape-3"></div>
            </div>

            {/* Header */}
            <div className="transfer-header">
                <button className="back-btn" onClick={onClose}>
                    <span className="back-icon">←</span>
                    <span className="back-text">Retour</span>
                </button>
                <h2>
                    <span className="header-icon">💸</span>
                    Virements
                </h2>
                <div className="header-tabs">
                    <button
                        className={`tab-btn ${step !== 'history' ? 'active' : ''}`}
                        onClick={() => setStep('form')}
                    >
                        <span className="tab-icon">➕</span>
                        <span className="tab-text">Nouveau</span>
                    </button>
                    <button
                        className={`tab-btn ${step === 'history' ?  'active' : ''}`}
                        onClick={() => setStep('history')}
                    >
                        <span className="tab-icon">📊</span>
                        <span className="tab-text">Historique</span>
                    </button>
                </div>
            </div>

            {/* Step: Form */}
            {step === 'form' && (
                <div className="transfer-form-container">
                    <div className="form-header">
                        <div className="form-header-icon">💳</div>
                        <div className="form-header-text">
                            <h3>Nouveau Virement</h3>
                            <p>Transférez de l'argent en toute sécurité</p>
                        </div>
                    </div>

                    <form onSubmit={handleShowConfirmation}>
                        {/* Sender Account */}
                        <div className="form-group">
                            <label>
                                <span className="label-icon">💳</span>
                                Compte source
                                <span className="required">*</span>
                            </label>
                            <div className="select-wrapper">
                                <select
                                    name="senderAccountId"
                                    value={formData. senderAccountId}
                                    onChange={handleInputChange}
                                    required
                                    className={formData.senderAccountId ? 'has-value' : ''}
                                >
                                    <option value="">Sélectionner un compte</option>
                                    {accounts. map(account => (
                                        <option key={account. id} value={account.id}>
                                            {account. accountType || 'Compte'} - ****{(account.accountNumber || '').slice(-4)} ({formatBalance(account.balance)} TND)
                                        </option>
                                    ))}
                                </select>
                                <span className="select-arrow">▼</span>
                            </div>
                            {accounts.length === 0 && (
                                <p className="helper-text warning">
                                    <span className="warning-icon">⚠️</span>
                                    Aucun compte disponible.  Créez un compte d'abord.
                                </p>
                            )}
                        </div>

                        {/* Beneficiary */}
                        <div className="form-group">
                            <label>
                                <span className="label-icon">👤</span>
                                Bénéficiaire
                                <span className="required">*</span>
                            </label>
                            <div className="select-wrapper">
                                <select
                                    name="beneficiaryId"
                                    value={formData. beneficiaryId}
                                    onChange={handleInputChange}
                                    required
                                    className={formData.beneficiaryId ? 'has-value' : ''}
                                >
                                    <option value="">Sélectionner un bénéficiaire</option>
                                    {beneficiaries.map(ben => (
                                        <option key={ben.id} value={ben.id}>
                                            {ben.name} - {ben.maskedAccountNumber || '****'} ({ben.bankTypeDisplay || ben.bankType})
                                        </option>
                                    ))}
                                </select>
                                <span className="select-arrow">▼</span>
                            </div>
                            {beneficiaries.length === 0 && (
                                <p className="helper-text warning">
                                    <span className="warning-icon">⚠️</span>
                                    Aucun bénéficiaire.  Ajoutez-en un d'abord.
                                </p>
                            )}
                        </div>

                        {/* Amount */}
                        <div className="form-group">
                            <label>
                                <span className="label-icon">💰</span>
                                Montant
                                <span className="required">*</span>
                            </label>
                            <div className="amount-input-wrapper">
                                <input
                                    type="number"
                                    name="amount"
                                    value={formData.amount}
                                    onChange={handleInputChange}
                                    placeholder="0.000"
                                    min="1"
                                    max="50000"
                                    step="0.001"
                                    required
                                    className={formData.amount ? 'has-value' : ''}
                                />
                                <span className="currency-badge">TND</span>
                            </div>
                            {selectedAccount && (
                                <p className="helper-text balance">
                                    <span className="balance-icon">💰</span>
                                    Solde disponible: <strong>{formatBalance(selectedAccount.balance)} TND</strong>
                                </p>
                            )}
                        </div>

                        {/* Description */}
                        <div className="form-group">
                            <label>
                                <span className="label-icon">📝</span>
                                Motif du virement
                                <span className="optional">(optionnel)</span>
                            </label>
                            <input
                                type="text"
                                name="description"
                                value={formData.description}
                                onChange={handleInputChange}
                                placeholder="Ex: Paiement facture, Remboursement..."
                                maxLength="255"
                                className={formData.description ? 'has-value' : ''}
                            />
                        </div>

                        {/* Summary Preview */}
                        {selectedBeneficiary && formData.amount && parseFloat(formData. amount) > 0 && (
                            <div className="transfer-summary">
                                <div className="summary-header">
                                    <span className="summary-icon">📋</span>
                                    <h4>Récapitulatif</h4>
                                </div>
                                <div className="summary-body">
                                    <div className="summary-row">
                                        <span className="summary-label">Bénéficiaire</span>
                                        <span className="summary-value">{selectedBeneficiary.name}</span>
                                    </div>
                                    <div className="summary-row">
                                        <span className="summary-label">Compte</span>
                                        <span className="summary-value mono">{selectedBeneficiary.maskedAccountNumber || '****'}</span>
                                    </div>
                                    <div className="summary-row">
                                        <span className="summary-label">Type</span>
                                        <span className="summary-value">
                                            <span className="bank-type-badge">{selectedBeneficiary.bankTypeDisplay || selectedBeneficiary.bankType}</span>
                                        </span>
                                    </div>
                                    <div className="summary-divider"></div>
                                    <div className="summary-row">
                                        <span className="summary-label">Montant</span>
                                        <span className="summary-value">{parseFloat(formData. amount).toFixed(3)} TND</span>
                                    </div>
                                    <div className="summary-row">
                                        <span className="summary-label">Frais</span>
                                        <span className="summary-value fee">{fees.toFixed(3)} TND</span>
                                    </div>
                                    <div className="summary-row total">
                                        <span className="summary-label">Total à débiter</span>
                                        <span className="summary-value">{totalAmount. toFixed(3)} TND</span>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Submit */}
                        <button
                            type="submit"
                            className="submit-btn"
                            disabled={loading || beneficiaries.length === 0 || accounts.length === 0}
                        >
                            <span className="btn-icon">🚀</span>
                            <span className="btn-text">Effectuer le virement</span>
                            <span className="btn-arrow">→</span>
                        </button>
                    </form>
                </div>
            )}

            {/* Step: OTP Verification */}
            {step === 'otp' && pendingTransfer && (
                <div className="otp-container">
                    <div className="otp-header">
                        <div className="otp-icon-wrapper">
                            <div className="otp-icon-bg"></div>
                            <span className="otp-icon">🔐</span>
                        </div>
                        <h3>Vérification OTP</h3>
                        <p>Un code de confirmation a été envoyé à votre email</p>
                    </div>

                    <div className="pending-transfer-card">
                        <div className="card-header">
                            <span className="card-icon">📄</span>
                            <span>Détails du virement</span>
                        </div>
                        <div className="card-body">
                            <div className="info-row">
                                <span className="info-label">Référence</span>
                                <span className="info-value mono">{pendingTransfer.reference}</span>
                            </div>
                            <div className="info-row">
                                <span className="info-label">Bénéficiaire</span>
                                <span className="info-value">{pendingTransfer. beneficiaryName}</span>
                            </div>
                            <div className="info-row">
                                <span className="info-label">Montant</span>
                                <span className="info-value">{pendingTransfer.amount} TND</span>
                            </div>
                            <div className="info-row">
                                <span className="info-label">Frais</span>
                                <span className="info-value">{pendingTransfer.fee} TND</span>
                            </div>
                            <div className="info-row total">
                                <span className="info-label">Total à débiter</span>
                                <span className="info-value highlight">{pendingTransfer.totalAmount} TND</span>
                            </div>
                        </div>
                    </div>

                    <form onSubmit={handleConfirmOtp}>
                        <div className="otp-input-section">
                            <label>Entrez le code OTP</label>
                            <div className="otp-input-wrapper">
                                <input
                                    type="text"
                                    value={otpCode}
                                    onChange={(e) => setOtpCode(e.target.value. replace(/\D/g, ''). slice(0, 6))}
                                    placeholder="• • • • • •"
                                    maxLength="6"
                                    className="otp-input"
                                    autoFocus
                                />
                                <div className="otp-input-decoration">
                                    {[...Array(6)].map((_, i) => (
                                        <span key={i} className={`dot ${otpCode.length > i ? 'filled' : ''}`}></span>
                                    ))}
                                </div>
                            </div>
                            <p className="otp-timer">
                                <span className="timer-icon">⏱️</span>
                                Le code expire dans 15 minutes
                            </p>
                        </div>

                        <div className="otp-actions">
                            <button type="button" className="cancel-btn" onClick={handleShowCancelConfirm}>
                                <span className="btn-icon">❌</span>
                                Annuler
                            </button>
                            <button type="submit" className="confirm-btn" disabled={loading || otpCode.length !== 6}>
                                <span className="btn-icon">✅</span>
                                Confirmer
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Step: History */}
            {step === 'history' && (
                <div className="history-container">
                    <div className="history-header">
                        <div className="history-header-icon">📊</div>
                        <div className="history-header-text">
                            <h3>Historique des Virements</h3>
                            <p>{transfers.length} transaction(s)</p>
                        </div>
                    </div>

                    {transfers.length === 0 ? (
                        <div className="empty-history">
                            <div className="empty-illustration">
                                <span className="empty-icon">📭</span>
                                <div className="empty-circles">
                                    <span className="circle c1"></span>
                                    <span className="circle c2"></span>
                                    <span className="circle c3"></span>
                                </div>
                            </div>
                            <h4>Aucun virement effectué</h4>
                            <p>Commencez par effectuer votre premier virement</p>
                            <button className="new-transfer-btn" onClick={() => setStep('form')}>
                                <span className="btn-icon">💸</span>
                                Faire un virement
                            </button>
                        </div>
                    ) : (
                        <div className="transfers-list">
                            {transfers.map((transfer, index) => (
                                <div
                                    key={transfer.id}
                                    className="transfer-item"
                                    style={{ animationDelay: `${index * 0.05}s` }}
                                >
                                    <div
                                        className="transfer-icon"
                                        style={{
                                            background: `linear-gradient(135deg, ${getStatusColor(transfer. status)}40 0%, ${getStatusColor(transfer.status)}20 100%)`,
                                            borderColor: getStatusColor(transfer. status)
                                        }}
                                    >
                                        {getStatusIcon(transfer. status)}
                                    </div>
                                    <div className="transfer-details">
                                        <div className="transfer-main">
                                            <span className="transfer-beneficiary">{transfer.beneficiaryName}</span>
                                            <span className="transfer-amount">-{transfer.totalAmount} TND</span>
                                        </div>
                                        <div className="transfer-secondary">
                                            <span className="transfer-date">
                                                <span className="date-icon">📅</span>
                                                {formatDate(transfer.createdAt)}
                                            </span>
                                            <span className="transfer-ref">
                                                <span className="ref-icon">🔖</span>
                                                {transfer. reference}
                                            </span>
                                        </div>
                                        <div className="transfer-footer">
                                            <span
                                                className="status-badge"
                                                style={{
                                                    background: `linear-gradient(135deg, ${getStatusColor(transfer.status)} 0%, ${getStatusColor(transfer.status)}cc 100%)`,
                                                    boxShadow: `0 4px 15px ${getStatusColor(transfer.status)}40`
                                                }}
                                            >
                                                {transfer.statusDisplay}
                                            </span>
                                            <span className="transfer-type">{transfer.transferTypeDisplay}</span>
                                        </div>
                                        {transfer.description && (
                                            <p className="transfer-description">
                                                <span className="desc-icon">📝</span>
                                                {transfer.description}
                                            </p>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}

            {/* Confirmation Modal */}
            <Modal
                isOpen={showConfirmModal}
                onClose={() => setShowConfirmModal(false)}
                type="confirm"
                title="Confirmer le virement"
                actions={
                    <>
                        <button className="modal-btn secondary" onClick={() => setShowConfirmModal(false)}>
                            <span>Annuler</span>
                        </button>
                        <button className="modal-btn primary" onClick={handleInitiateTransfer}>
                            <span>Confirmer</span>
                            <span className="btn-arrow">→</span>
                        </button>
                    </>
                }
            >
                <div className="confirm-details">
                    <p className="confirm-question">Voulez-vous effectuer ce virement ?</p>
                    <div className="confirm-card">
                        <div className="confirm-row">
                            <span className="confirm-label">De</span>
                            <span className="confirm-value">{selectedAccount?. accountType} - ****{(selectedAccount?.accountNumber || '').slice(-4)}</span>
                        </div>
                        <div className="confirm-row">
                            <span className="confirm-label">Vers</span>
                            <span className="confirm-value highlight">{selectedBeneficiary?.name}</span>
                        </div>
                        <div className="confirm-row">
                            <span className="confirm-label">Compte</span>
                            <span className="confirm-value mono">{selectedBeneficiary?.maskedAccountNumber}</span>
                        </div>
                        <div className="confirm-divider"></div>
                        <div className="confirm-row">
                            <span className="confirm-label">Montant</span>
                            <span className="confirm-value">{formData.amount ?  parseFloat(formData.amount).toFixed(3) : '0.000'} TND</span>
                        </div>
                        <div className="confirm-row">
                            <span className="confirm-label">Frais</span>
                            <span className="confirm-value fee">{fees.toFixed(3)} TND</span>
                        </div>
                        <div className="confirm-row total">
                            <span className="confirm-label">Total</span>
                            <span className="confirm-value">{totalAmount.toFixed(3)} TND</span>
                        </div>
                        {formData.description && (
                            <div className="confirm-row">
                                <span className="confirm-label">Motif</span>
                                <span className="confirm-value">{formData.description}</span>
                            </div>
                        )}
                    </div>
                </div>
            </Modal>

            {/* Success Modal */}
            <Modal
                isOpen={showSuccessModal}
                onClose={handleSuccessModalClose}
                type="success"
                title="Virement Effectué !"
                actions={
                    <>
                        <button className="modal-btn secondary" onClick={handleViewHistory}>
                            <span className="btn-icon">📊</span>
                            <span>Historique</span>
                        </button>
                        <button className="modal-btn primary" onClick={handleNewTransfer}>
                            <span className="btn-icon">💸</span>
                            <span>Nouveau</span>
                        </button>
                    </>
                }
            >
                <div className="success-details">
                    <div className="success-animation">
                        <div className="success-checkmark">
                            <div className="check-icon">✓</div>
                        </div>
                    </div>
                    <p className="success-message">Votre virement a été traité avec succès</p>
                    {completedTransfer && (
                        <div className="success-card">
                            <div className="success-row">
                                <span className="success-label">Référence</span>
                                <span className="success-value reference">{completedTransfer.reference}</span>
                            </div>
                            <div className="success-row">
                                <span className="success-label">Bénéficiaire</span>
                                <span className="success-value">{completedTransfer.beneficiaryName}</span>
                            </div>
                            <div className="success-row">
                                <span className="success-label">Montant total</span>
                                <span className="success-value amount">{completedTransfer.totalAmount} TND</span>
                            </div>
                        </div>
                    )}
                    <p className="email-notice">
                        <span className="email-icon">📧</span>
                        Un email de confirmation a été envoyé
                    </p>
                </div>
            </Modal>

            {/* Error Modal */}
            <Modal
                isOpen={showErrorModal}
                onClose={() => setShowErrorModal(false)}
                type="error"
                title="Erreur"
                actions={
                    <button className="modal-btn primary" onClick={() => setShowErrorModal(false)}>
                        <span>Compris</span>
                    </button>
                }
            >
                <div className="error-details">
                    <div className="error-icon-wrapper">
                        <span className="error-x">×</span>
                    </div>
                    <p className="error-message">{modalMessage}</p>
                </div>
            </Modal>

            {/* Cancel Confirmation Modal */}
            <Modal
                isOpen={showCancelConfirmModal}
                onClose={() => setShowCancelConfirmModal(false)}
                type="warning"
                title="Annuler le virement ?"
                actions={
                    <>
                        <button className="modal-btn secondary" onClick={() => setShowCancelConfirmModal(false)}>
                            <span>Non, continuer</span>
                        </button>
                        <button className="modal-btn danger" onClick={handleCancelTransfer}>
                            <span>Oui, annuler</span>
                        </button>
                    </>
                }
            >
                <div className="warning-details">
                    <p>Êtes-vous sûr de vouloir annuler ce virement ?</p>
                    <p className="warning-text">
                        <span className="warning-icon">⚠️</span>
                        Cette action est irréversible.
                    </p>
                </div>
            </Modal>

            {/* Loading Modal */}
            <Modal
                isOpen={showLoadingModal}
                type="loading"
                title="Traitement en cours..."
            >
                <p className="loading-message">Veuillez patienter pendant que nous traitons votre demande...</p>
            </Modal>
        </div>
    );
};

export default TransferManagement;