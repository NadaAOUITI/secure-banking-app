
import React, { useState, useEffect } from 'react';
import transferService from '../../services/TransferService';
import beneficiaryService from '../../services/BeneficiaryService';

const TransferManagement = ({ onClose, accounts: propAccounts = [] }) => {
    const [step, setStep] = useState('form');
    const [beneficiaries, setBeneficiaries] = useState([]);
    const [transfers, setTransfers] = useState([]);
    const [accounts, setAccounts] = useState(propAccounts); // Utiliser les comptes passés en props ou les charger
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const [formData, setFormData] = useState({
        senderAccountId: '',
        beneficiaryId: '',
        amount: '',
        description: ''
    });

    const [pendingTransfer, setPendingTransfer] = useState(null);
    const [otpCode, setOtpCode] = useState('');

    useEffect(() => {
        loadBeneficiaries();
        loadTransferHistory();
        // Si pas de comptes passés en props, les charger
        if (propAccounts. length === 0) {
            loadUserAccounts();
        }
    }, []);

    // ✅ NOUVELLE FONCTION: Charger les comptes de l'utilisateur
    const loadUserAccounts = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/account/details', {
                method: 'GET',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                const data = await response.json();
                // Les comptes peuvent être dans data.accounts ou directement un tableau
                if (data.accounts) {
                    setAccounts(data. accounts);
                } else if (Array.isArray(data)) {
                    setAccounts(data);
                }
            }
        } catch (error) {
            console.error('Erreur chargement comptes:', error);
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

    // ...  reste du code inchangé ...

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        setError('');
    };

    const handleInitiateTransfer = async (e) => {
        e.preventDefault();
        setError('');

        if (!formData.senderAccountId || ! formData.beneficiaryId || !formData.amount) {
            setError('Veuillez remplir tous les champs obligatoires');
            return;
        }

        if (parseFloat(formData. amount) < 1) {
            setError('Le montant minimum est 1 TND');
            return;
        }

        setLoading(true);
        const result = await transferService.initiateTransfer({
            senderAccountId: parseInt(formData.senderAccountId),
            beneficiaryId: parseInt(formData.beneficiaryId),
            amount: parseFloat(formData.amount),
            description: formData. description
        });

        if (result.success) {
            setPendingTransfer(result.transfer);
            setStep('otp');
        } else {
            setError(result.message);
        }
        setLoading(false);
    };

    const handleConfirmOtp = async (e) => {
        e.preventDefault();
        setError('');

        if (!otpCode || otpCode.length !== 6) {
            setError('Veuillez entrer un code OTP valide (6 chiffres)');
            return;
        }

        setLoading(true);
        const result = await transferService.confirmTransfer(
            pendingTransfer.reference,
            otpCode
        );

        if (result. success) {
            setSuccess('Virement effectué avec succès!');
            setStep('success');
            loadTransferHistory();
            loadUserAccounts(); // Recharger les comptes pour mettre à jour les soldes
        } else {
            setError(result.message);
        }
        setLoading(false);
    };

    const handleCancelTransfer = async () => {
        if (pendingTransfer) {
            await transferService.cancelTransfer(pendingTransfer.reference);
        }
        resetForm();
    };

    const resetForm = () => {
        setFormData({
            senderAccountId: '',
            beneficiaryId: '',
            amount: '',
            description: ''
        });
        setPendingTransfer(null);
        setOtpCode('');
        setStep('form');
        setError('');
        setSuccess('');
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

    return (
        <div className="transfer-management">
            {/* Header */}
            <div className="transfer-header">
                <button className="back-btn" onClick={onClose}>← Retour</button>
                <h2>💸 Virements</h2>
                <div className="header-tabs">
                    <button
                        className={`tab-btn ${step !== 'history' ? 'active' : ''}`}
                        onClick={() => setStep('form')}
                    >
                        Nouveau
                    </button>
                    <button
                        className={`tab-btn ${step === 'history' ?  'active' : ''}`}
                        onClick={() => setStep('history')}
                    >
                        Historique
                    </button>
                </div>
            </div>

            {/* Alerts */}
            {error && <div className="alert error">❌ {error}</div>}
            {success && <div className="alert success">✅ {success}</div>}

            {/* Step: Form */}
            {step === 'form' && (
                <div className="transfer-form-container">
                    <form onSubmit={handleInitiateTransfer}>
                        {/* Sender Account */}
                        <div className="form-group">
                            <label>💳 Compte source *</label>
                            <select
                                name="senderAccountId"
                                value={formData.senderAccountId}
                                onChange={handleInputChange}
                                required
                            >
                                <option value="">Sélectionner un compte</option>
                                {accounts.map(account => (
                                    <option key={account.id} value={account. id}>
                                        {account.accountType || 'Compte'} - ****{(account.accountNumber || '').slice(-4)} ({formatBalance(account.balance)} TND)
                                    </option>
                                ))}
                            </select>
                            {accounts.length === 0 && (
                                <p className="helper-text warning">⚠️ Aucun compte disponible.  Créez un compte d'abord.</p>
                            )}
                        </div>

                        {/* Beneficiary */}
                        <div className="form-group">
                            <label>👤 Bénéficiaire *</label>
                            <select
                                name="beneficiaryId"
                                value={formData.beneficiaryId}
                                onChange={handleInputChange}
                                required
                            >
                                <option value="">Sélectionner un bénéficiaire</option>
                                {beneficiaries.map(ben => (
                                    <option key={ben.id} value={ben.id}>
                                        {ben.name} - {ben.maskedAccountNumber || '****'} ({ben.bankTypeDisplay || ben.bankType})
                                    </option>
                                ))}
                            </select>
                            {beneficiaries.length === 0 && (
                                <p className="helper-text warning">⚠️ Aucun bénéficiaire.  Ajoutez-en un d'abord.</p>
                            )}
                        </div>

                        {/* Amount */}
                        <div className="form-group">
                            <label>💰 Montant (TND) *</label>
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
                            />
                            {selectedAccount && (
                                <p className="helper-text">💰 Solde disponible: {formatBalance(selectedAccount.balance)} TND</p>
                            )}
                        </div>

                        {/* Description */}
                        <div className="form-group">
                            <label>📝 Motif du virement</label>
                            <input
                                type="text"
                                name="description"
                                value={formData.description}
                                onChange={handleInputChange}
                                placeholder="Ex: Paiement facture"
                                maxLength="255"
                            />
                        </div>

                        {/* Summary */}
                        {selectedBeneficiary && formData.amount && parseFloat(formData. amount) > 0 && (
                            <div className="transfer-summary">
                                <h4>📋 Récapitulatif</h4>
                                <div className="summary-row">
                                    <span>Bénéficiaire:</span>
                                    <span>{selectedBeneficiary.name}</span>
                                </div>
                                <div className="summary-row">
                                    <span>Compte:</span>
                                    <span>{selectedBeneficiary.maskedAccountNumber || '****'}</span>
                                </div>
                                <div className="summary-row">
                                    <span>Type:</span>
                                    <span>{selectedBeneficiary.bankTypeDisplay || selectedBeneficiary.bankType}</span>
                                </div>
                                <div className="summary-row">
                                    <span>Montant:</span>
                                    <span>{parseFloat(formData.amount).toFixed(3)} TND</span>
                                </div>
                                <div className="summary-row">
                                    <span>Frais estimés:</span>
                                    <span>
                                        {selectedBeneficiary.bankType === 'SAME_BANK' ? '0.000' :
                                            selectedBeneficiary. bankType === 'NATIONAL' ? '2.500' :
                                                Math.max(10, parseFloat(formData.amount) * 0.01). toFixed(3)} TND
                                    </span>
                                </div>
                                <div className="summary-row total">
                                    <span>Total estimé:</span>
                                    <span>
                                        {(parseFloat(formData.amount) +
                                            (selectedBeneficiary.bankType === 'SAME_BANK' ? 0 :
                                                selectedBeneficiary. bankType === 'NATIONAL' ? 2.5 :
                                                    Math.max(10, parseFloat(formData.amount) * 0.01))).toFixed(3)} TND
                                    </span>
                                </div>
                            </div>
                        )}

                        {/* Submit */}
                        <button
                            type="submit"
                            className="submit-btn"
                            disabled={loading || beneficiaries.length === 0 || accounts.length === 0}
                        >
                            {loading ? '⏳ Traitement.. .' : '🚀 Effectuer le virement'}
                        </button>
                    </form>
                </div>
            )}

            {/* Step: OTP Verification */}
            {step === 'otp' && pendingTransfer && (
                <div className="otp-container">
                    <div className="otp-icon">🔐</div>
                    <h3>Vérification OTP</h3>
                    <p>Un code de confirmation a été envoyé à votre email</p>

                    <div className="pending-transfer-info">
                        <div className="info-row">
                            <span className="info-label">Référence:</span>
                            <span className="info-value">{pendingTransfer.reference}</span>
                        </div>
                        <div className="info-row">
                            <span className="info-label">Bénéficiaire:</span>
                            <span className="info-value">{pendingTransfer. beneficiaryName}</span>
                        </div>
                        <div className="info-row">
                            <span className="info-label">Montant:</span>
                            <span className="info-value">{pendingTransfer.amount} TND</span>
                        </div>
                        <div className="info-row">
                            <span className="info-label">Frais:</span>
                            <span className="info-value">{pendingTransfer.fee} TND</span>
                        </div>
                        <div className="info-row total">
                            <span className="info-label">Total à débiter:</span>
                            <span className="info-value">{pendingTransfer.totalAmount} TND</span>
                        </div>
                    </div>

                    <form onSubmit={handleConfirmOtp}>
                        <div className="otp-input-container">
                            <input
                                type="text"
                                value={otpCode}
                                onChange={(e) => setOtpCode(e.target.value. replace(/\D/g, ''). slice(0, 6))}
                                placeholder="000000"
                                maxLength="6"
                                className="otp-input"
                                autoFocus
                            />
                        </div>

                        <p className="otp-timer">⏱️ Le code expire dans 15 minutes</p>

                        <div className="otp-actions">
                            <button type="button" className="cancel-btn" onClick={handleCancelTransfer}>
                                ❌ Annuler
                            </button>
                            <button type="submit" className="confirm-btn" disabled={loading || otpCode. length !== 6}>
                                {loading ? '⏳ Vérification...' : '✅ Confirmer'}
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Step: Success */}
            {step === 'success' && pendingTransfer && (
                <div className="success-container">
                    <div className="success-icon">✅</div>
                    <h3>Virement Effectué! </h3>
                    <p>Votre virement a été traité avec succès</p>

                    <div className="success-details">
                        <div className="detail-row">
                            <span>Référence:</span>
                            <span className="reference">{pendingTransfer.reference}</span>
                        </div>
                        <div className="detail-row">
                            <span>Bénéficiaire:</span>
                            <span>{pendingTransfer. beneficiaryName}</span>
                        </div>
                        <div className="detail-row">
                            <span>Montant:</span>
                            <span className="amount">{pendingTransfer.totalAmount} TND</span>
                        </div>
                    </div>

                    <p className="email-notice">📧 Un email de confirmation a été envoyé</p>

                    <div className="success-actions">
                        <button className="new-transfer-btn" onClick={resetForm}>
                            💸 Nouveau Virement
                        </button>
                        <button className="history-btn" onClick={() => setStep('history')}>
                            📊 Voir l'historique
                        </button>
                    </div>
                </div>
            )}

            {/* Step: History */}
            {step === 'history' && (
                <div className="history-container">
                    <h3>📊 Historique des Virements</h3>

                    {transfers.length === 0 ?  (
                        <div className="empty-history">
                            <div className="empty-icon">📭</div>
                            <p>Aucun virement effectué</p>
                            <button className="new-transfer-btn" onClick={() => setStep('form')}>
                                💸 Faire un virement
                            </button>
                        </div>
                    ) : (
                        <div className="transfers-list">
                            {transfers.map(transfer => (
                                <div key={transfer.id} className="transfer-item">
                                    <div className="transfer-icon" style={{ background: getStatusColor(transfer.status) }}>
                                        {getStatusIcon(transfer.status)}
                                    </div>
                                    <div className="transfer-details">
                                        <div className="transfer-main">
                                            <span className="transfer-beneficiary">{transfer.beneficiaryName}</span>
                                            <span className="transfer-amount">-{transfer.totalAmount} TND</span>
                                        </div>
                                        <div className="transfer-secondary">
                                            <span className="transfer-date">{formatDate(transfer.createdAt)}</span>
                                            <span className="transfer-ref">Réf: {transfer. reference}</span>
                                        </div>
                                        <div className="transfer-status">
                                            <span
                                                className="status-badge"
                                                style={{ background: getStatusColor(transfer.status) }}
                                            >
                                                {transfer.statusDisplay}
                                            </span>
                                            <span className="transfer-type">{transfer.transferTypeDisplay}</span>
                                        </div>
                                        {transfer.description && (
                                            <p className="transfer-description">📝 {transfer.description}</p>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default TransferManagement;