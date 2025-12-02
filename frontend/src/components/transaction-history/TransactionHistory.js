import React, { useState, useEffect, useCallback } from 'react';
import './TransactionHistory.css';

const TransactionHistory = ({ onClose }) => {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalItems, setTotalItems] = useState(0);

    // Filtres
    const [filters, setFilters] = useState({
        startDate: '',
        endDate: '',
        minAmount: '',
        maxAmount: '',
        beneficiary: '',
        transactionType: '',
        status: ''
    });
    const [showFilters, setShowFilters] = useState(false);

    const API_BASE = 'https://localhost:8080/api/transactions';
    const PAGE_SIZE = 10;

    const loadTransactions = useCallback(async (page = 0, appliedFilters = null) => {
        setLoading(true);
        setError(null);

        try {
            let response;
            const filtersToUse = appliedFilters || filters;
            const hasFilters = Object. values(filtersToUse).some(v => v !== '');

            if (hasFilters) {
                response = await fetch(`${API_BASE}/filter`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify({
                        ... filtersToUse,
                        page,
                        size: PAGE_SIZE
                    })
                });
            } else {
                response = await fetch(`${API_BASE}?page=${page}&size=${PAGE_SIZE}`, {
                    method: 'GET',
                    credentials: 'include'
                });
            }

            if (response.ok) {
                const data = await response.json();
                setTransactions(data.transactions || []);
                setCurrentPage(data.currentPage || 0);
                setTotalPages(data.totalPages || 0);
                setTotalItems(data.totalItems || 0);
            } else {
                setError('Erreur lors du chargement des transactions');
            }
        } catch (err) {
            console.error('Erreur:', err);
            setError('Erreur de connexion au serveur');
        } finally {
            setLoading(false);
        }
    }, [filters]);

    useEffect(() => {
        loadTransactions();
    }, []);

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
    };

    const handleApplyFilters = (e) => {
        e.preventDefault();
        setCurrentPage(0);
        loadTransactions(0, filters);
    };

    const handleClearFilters = () => {
        const clearedFilters = {
            startDate: '',
            endDate: '',
            minAmount: '',
            maxAmount: '',
            beneficiary: '',
            transactionType: '',
            status: ''
        };
        setFilters(clearedFilters);
        setCurrentPage(0);
        loadTransactions(0, clearedFilters);
    };

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            setCurrentPage(newPage);
            loadTransactions(newPage);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl. NumberFormat('fr-FR', {
            style: 'currency',
            currency: 'EUR'
        }).format(amount);
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleDateString('fr-FR', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getStatusClass = (status) => {
        switch (status?. toUpperCase()) {
            case 'COMPLETED': return 'completed';
            case 'PENDING': return 'pending';
            case 'FAILED': return 'failed';
            case 'CANCELLED': return 'cancelled';
            default: return '';
        }
    };

    const getAmountClass = (type) => {
        const creditTypes = ['CREDIT', 'DEPOSIT'];
        return creditTypes.includes(type?. toUpperCase()) ?  'credit' : 'debit';
    };

    const getStatusLabel = (status) => {
        const labels = {
            'COMPLETED': 'Terminé',
            'PENDING': 'En attente',
            'FAILED': 'Échoué',
            'CANCELLED': 'Annulé'
        };
        return labels[status?. toUpperCase()] || status;
    };

    const getTypeLabel = (type) => {
        const labels = {
            'CREDIT': 'Crédit',
            'DEBIT': 'Débit',
            'TRANSFER': 'Virement',
            'PAYMENT': 'Paiement',
            'WITHDRAWAL': 'Retrait',
            'DEPOSIT': 'Dépôt'
        };
        return labels[type?. toUpperCase()] || type;
    };

    return (
        <div className="transaction-history-container">
            {/* Header */}
            <div className="th-header">
                <button className="th-back-btn" onClick={onClose}>
                    ← Retour
                </button>
                <h1>📊 Historique des Transactions</h1>
                <p>Consultez et filtrez vos transactions</p>
            </div>

            {/* Filter Toggle */}
            <button
                className="th-filter-toggle"
                onClick={() => setShowFilters(!showFilters)}
            >
                🔍 {showFilters ? 'Masquer les filtres' : 'Afficher les filtres'}
            </button>

            {/* Filters */}
            {showFilters && (
                <form className="th-filters" onSubmit={handleApplyFilters}>
                    <div className="th-filter-row">
                        <div className="th-filter-group">
                            <label>Date début</label>
                            <input
                                type="date"
                                name="startDate"
                                value={filters. startDate}
                                onChange={handleFilterChange}
                            />
                        </div>
                        <div className="th-filter-group">
                            <label>Date fin</label>
                            <input
                                type="date"
                                name="endDate"
                                value={filters.endDate}
                                onChange={handleFilterChange}
                            />
                        </div>
                    </div>

                    <div className="th-filter-row">
                        <div className="th-filter-group">
                            <label>Montant min</label>
                            <input
                                type="number"
                                name="minAmount"
                                value={filters.minAmount}
                                onChange={handleFilterChange}
                                placeholder="0.00"
                                min="0"
                                step="0.01"
                            />
                        </div>
                        <div className="th-filter-group">
                            <label>Montant max</label>
                            <input
                                type="number"
                                name="maxAmount"
                                value={filters.maxAmount}
                                onChange={handleFilterChange}
                                placeholder="0.00"
                                min="0"
                                step="0.01"
                            />
                        </div>
                    </div>

                    <div className="th-filter-row">
                        <div className="th-filter-group">
                            <label>Bénéficiaire</label>
                            <input
                                type="text"
                                name="beneficiary"
                                value={filters.beneficiary}
                                onChange={handleFilterChange}
                                placeholder="Rechercher..."
                            />
                        </div>
                        <div className="th-filter-group">
                            <label>Type</label>
                            <select
                                name="transactionType"
                                value={filters.transactionType}
                                onChange={handleFilterChange}
                            >
                                <option value="">Tous</option>
                                <option value="CREDIT">Crédit</option>
                                <option value="DEBIT">Débit</option>
                                <option value="TRANSFER">Virement</option>
                                <option value="PAYMENT">Paiement</option>
                            </select>
                        </div>
                        <div className="th-filter-group">
                            <label>Statut</label>
                            <select
                                name="status"
                                value={filters.status}
                                onChange={handleFilterChange}
                            >
                                <option value="">Tous</option>
                                <option value="COMPLETED">Terminé</option>
                                <option value="PENDING">En attente</option>
                                <option value="FAILED">Échoué</option>
                            </select>
                        </div>
                    </div>

                    <div className="th-filter-actions">
                        <button type="submit" className="th-btn primary">
                            Appliquer
                        </button>
                        <button type="button" className="th-btn secondary" onClick={handleClearFilters}>
                            Effacer
                        </button>
                    </div>
                </form>
            )}

            {/* Summary */}
            <div className="th-summary">
                <div className="th-summary-item">
                    <span className="th-summary-label">Total</span>
                    <span className="th-summary-value">{totalItems}</span>
                </div>
            </div>

            {/* Content */}
            <div className="th-content">
                {loading ? (
                    <div className="th-loading">
                        <div className="th-spinner"></div>
                        <p>Chargement...</p>
                    </div>
                ) : error ? (
                    <div className="th-error">
                        <p>{error}</p>
                        <button onClick={() => loadTransactions()}>Réessayer</button>
                    </div>
                ) : transactions.length === 0 ? (
                    <div className="th-empty">
                        <p>Aucune transaction trouvée</p>
                    </div>
                ) : (
                    <div className="th-list">
                        {transactions.map((tx) => (
                            <div key={tx.id} className="th-item">
                                <div className="th-item-main">
                                    <div className="th-item-info">
                                        <span className="th-item-beneficiary">
                                            {tx.beneficiary || 'N/A'}
                                        </span>
                                        <span className="th-item-description">
                                            {tx.description || 'Aucune description'}
                                        </span>
                                        <span className="th-item-date">
                                            {formatDate(tx.transactionDate)}
                                        </span>
                                    </div>
                                    <div className="th-item-right">
                                        <span className={`th-item-amount ${getAmountClass(tx.transactionType)}`}>
                                            {getAmountClass(tx.transactionType) === 'credit' ? '+' : '-'}
                                            {formatCurrency(tx. amount)}
                                        </span>
                                        <span className={`th-item-status ${getStatusClass(tx.status)}`}>
                                            {getStatusLabel(tx.status)}
                                        </span>
                                    </div>
                                </div>
                                <div className="th-item-footer">
                                    <span className="th-item-type">{getTypeLabel(tx.transactionType)}</span>
                                    <span className="th-item-ref">Réf: {tx. referenceNumber}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
                <div className="th-pagination">
                    <button
                        className="th-page-btn"
                        onClick={() => handlePageChange(currentPage - 1)}
                        disabled={currentPage === 0}
                    >
                        ← Précédent
                    </button>
                    <span className="th-page-info">
                        Page {currentPage + 1} sur {totalPages}
                    </span>
                    <button
                        className="th-page-btn"
                        onClick={() => handlePageChange(currentPage + 1)}
                        disabled={currentPage >= totalPages - 1}
                    >
                        Suivant →
                    </button>
                </div>
            )}
        </div>
    );
};

export default TransactionHistory;