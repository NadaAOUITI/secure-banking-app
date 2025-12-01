"use client"

import { useState, useEffect } from "react"
import TransferService from "../services/TransferService"
import { formatDate, formatCurrency, getStatusBadge } from "../utils/formatters"
import "./TransferHistory.css"

export default function TransferHistory() {
    const [transfers, setTransfers] = useState([])
    const [loading, setLoading] = useState(true)
    const [filters, setFilters] = useState({
        startDate: "",
        endDate: "",
        minAmount: "",
        maxAmount: "",
    })
    const [showFilters, setShowFilters] = useState(false)

    useEffect(() => {
        loadTransfers()
    }, [])

    const loadTransfers = async () => {
        setLoading(true)
        const result = await TransferService.getTransferHistory(filters)
        if (result.success) {
            setTransfers(result.transfers || [])
        }
        setLoading(false)
    }

    const handleFilterChange = (e) => {
        const { name, value } = e.target
        setFilters((prev) => ({ ...prev, [name]: value }))
    }

    const handleApplyFilters = () => {
        loadTransfers()
        setShowFilters(false)
    }

    const handleClearFilters = () => {
        setFilters({
            startDate: "",
            endDate: "",
            minAmount: "",
            maxAmount: "",
        })
    }

    return (
        <div className="transfer-history">
            <div className="history-header">
                <h2 className="history-title">Transfer History</h2>
                <button className="btn-filter" onClick={() => setShowFilters(!showFilters)}>
                    Filters
                </button>
            </div>

            {showFilters && (
                <div className="filters-panel">
                    <div className="filter-row">
                        <div className="filter-group">
                            <label htmlFor="startDate">From Date</label>
                            <input
                                id="startDate"
                                type="date"
                                name="startDate"
                                value={filters.startDate}
                                onChange={handleFilterChange}
                            />
                        </div>
                        <div className="filter-group">
                            <label htmlFor="endDate">To Date</label>
                            <input id="endDate" type="date" name="endDate" value={filters.endDate} onChange={handleFilterChange} />
                        </div>
                    </div>

                    <div className="filter-row">
                        <div className="filter-group">
                            <label htmlFor="minAmount">Min Amount</label>
                            <input
                                id="minAmount"
                                type="number"
                                name="minAmount"
                                value={filters.minAmount}
                                onChange={handleFilterChange}
                                placeholder="0"
                            />
                        </div>
                        <div className="filter-group">
                            <label htmlFor="maxAmount">Max Amount</label>
                            <input
                                id="maxAmount"
                                type="number"
                                name="maxAmount"
                                value={filters.maxAmount}
                                onChange={handleFilterChange}
                                placeholder="0"
                            />
                        </div>
                    </div>

                    <div className="filter-actions">
                        <button className="btn btn-secondary" onClick={handleClearFilters}>
                            Clear
                        </button>
                        <button className="btn btn-primary" onClick={handleApplyFilters}>
                            Apply Filters
                        </button>
                    </div>
                </div>
            )}

            {loading ? (
                <div className="loading-state">Loading transfers...</div>
            ) : transfers.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-icon">📋</div>
                    <p>No transfers found</p>
                </div>
            ) : (
                <div className="transfers-table">
                    <div className="table-header">
                        <div className="col-date">Date</div>
                        <div className="col-beneficiary">Beneficiary</div>
                        <div className="col-amount">Amount</div>
                        <div className="col-status">Status</div>
                    </div>
                    {transfers.map((transfer) => {
                        const statusInfo = getStatusBadge(transfer.status)
                        return (
                            <div key={transfer.id} className="table-row">
                                <div className="col-date">{formatDate(transfer.createdAt)}</div>
                                <div className="col-beneficiary">
                                    <div className="beneficiary-info">
                                        <div className="beneficiary-name">{transfer.beneficiaryName}</div>
                                        <div className="beneficiary-ref">{transfer.reference}</div>
                                    </div>
                                </div>
                                <div className="col-amount">{formatCurrency(transfer.amount)}</div>
                                <div className="col-status">
                  <span
                      className="status-badge"
                      style={{
                          backgroundColor: `${statusInfo.color}20`,
                          color: statusInfo.color,
                      }}
                  >
                    {statusInfo.label}
                  </span>
                                </div>
                            </div>
                        )
                    })}
                </div>
            )}
        </div>
    )
}
