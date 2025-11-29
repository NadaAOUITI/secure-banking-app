"use client"

import { useState, useEffect } from "react"
import beneficiaryService from "../../services/BeneficiaryService"
import "./BeneficiaryManagement.css"

const BeneficiaryManagement = ({ onClose }) => {
    const [beneficiaries, setBeneficiaries] = useState([])
    const [loading, setLoading] = useState(true)
    const [showForm, setShowForm] = useState(false)
    const [editingId, setEditingId] = useState(null)
    const [error, setError] = useState("")
    const [success, setSuccess] = useState("")
    const [formErrors, setFormErrors] = useState({})
    const [formData, setFormData] = useState({
        name: "",
        accountNumber: "",
        bankType: "SAME_BANK",
        bankName: "",
        bankCode: "",
        swiftCode: "",
        iban: "",
        country: "",
    })

    useEffect(() => {
        loadBeneficiaries()
    }, [])

    useEffect(() => {
        if (success) {
            const timer = setTimeout(() => setSuccess(""), 4000)
            return () => clearTimeout(timer)
        }
    }, [success])

    useEffect(() => {
        if (error) {
            const timer = setTimeout(() => setError(""), 5000)
            return () => clearTimeout(timer)
        }
    }, [error])

    const loadBeneficiaries = async () => {
        setLoading(true)
        const result = await beneficiaryService.getBeneficiaries()
        if (result.success) {
            setBeneficiaries(result.beneficiaries)
        } else {
            setError(result.message)
        }
        setLoading(false)
    }

    const handleInputChange = (e) => {
        const { name, value } = e.target
        setFormData((prev) => ({ ...prev, [name]: value }))
        if (formErrors[name]) {
            setFormErrors((prev) => {
                const newErrors = { ...prev }
                delete newErrors[name]
                return newErrors
            })
        }
        setError("")
    }

    const validateForm = () => {
        const errors = {}

        if (!formData.name.trim()) {
            errors.name = "Le nom du bénéficiaire est requis"
        }

        if (!formData.accountNumber.trim()) {
            errors.accountNumber = "Le numéro de compte est requis"
        } else if (!beneficiaryService.validateAccountNumber(formData.accountNumber, formData.bankType)) {
            errors.accountNumber = "Format de numéro de compte invalide"
        }

        if (formData.bankType === "INTERNATIONAL") {
            if (!formData.iban || !beneficiaryService.validateIban(formData.iban)) {
                errors.iban = "IBAN valide requis pour les virements internationaux"
            }
            if (formData.swiftCode && !beneficiaryService.validateSwiftCode(formData.swiftCode)) {
                errors.swiftCode = "Format de code SWIFT invalide"
            }
        }

        setFormErrors(errors)
        return Object.keys(errors).length === 0
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        if (!validateForm()) return

        setLoading(true)
        setError("")
        let result

        if (editingId) {
            result = await beneficiaryService.updateBeneficiary(editingId, formData)
        } else {
            result = await beneficiaryService.addBeneficiary(formData)
        }

        if (result.success) {
            setSuccess(editingId ? "Bénéficiaire mis à jour" : "Bénéficiaire ajouté")
            resetForm()
            loadBeneficiaries()
        } else {
            setError(result.message)
        }
        setLoading(false)
    }

    const handleEdit = (beneficiary) => {
        setFormData({
            name: beneficiary.name,
            accountNumber: beneficiary.accountNumber,
            bankType: beneficiary.bankType,
            bankName: beneficiary.bankName || "",
            bankCode: beneficiary.bankCode || "",
            swiftCode: "",
            iban: "",
            country: beneficiary.country || "",
        })
        setEditingId(beneficiary.id)
        setShowForm(true)
        setFormErrors({})
    }

    const handleDelete = async (id) => {
        if (!window.confirm("Êtes-vous sûr de vouloir supprimer ce bénéficiaire ?")) {
            return
        }

        setLoading(true)
        const result = await beneficiaryService.deleteBeneficiary(id)
        if (result.success) {
            setSuccess("Bénéficiaire supprimé")
            loadBeneficiaries()
        } else {
            setError(result.message)
        }
        setLoading(false)
    }

    const resetForm = () => {
        setFormData({
            name: "",
            accountNumber: "",
            bankType: "SAME_BANK",
            bankName: "",
            bankCode: "",
            swiftCode: "",
            iban: "",
            country: "",
        })
        setEditingId(null)
        setShowForm(false)
        setFormErrors({})
    }

    const getBankTypeIcon = (type) => {
        switch (type) {
            case "SAME_BANK":
                return "🏦"
            case "NATIONAL":
                return "🇹🇳"
            case "INTERNATIONAL":
                return "🌍"
            default:
                return "💳"
        }
    }

    return (
        <div className="beneficiary-management">
            <div className="beneficiary-header">
                <h2>Gestion des Bénéficiaires</h2>
                <button className="close-btn" onClick={onClose} title="Fermer">
                    ✕
                </button>
            </div>

            {error && <div className="alert alert-error">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            {!showForm ? (
                <>
                    <button className="add-btn" onClick={() => setShowForm(true)}>
                        ➕ Ajouter un Bénéficiaire
                    </button>

                    {loading ? (
                        <div className="loading">Chargement des bénéficiaires...</div>
                    ) : beneficiaries.length === 0 ? (
                        <div className="empty-state">
                            <p>Aucun bénéficiaire enregistré</p>
                            <p>Ajoutez votre premier bénéficiaire pour effectuer des virements</p>
                        </div>
                    ) : (
                        <div className="beneficiaries-list">
                            {beneficiaries.map((beneficiary) => (
                                <div key={beneficiary.id} className="beneficiary-card">
                                    <div className="beneficiary-icon">{getBankTypeIcon(beneficiary.bankType)}</div>
                                    <div className="beneficiary-info">
                                        <h4>{beneficiary.name}</h4>
                                        <p className="account-number">{beneficiary.maskedAccountNumber}</p>
                                        <p className="bank-type">{beneficiary.bankTypeDisplay}</p>
                                        {beneficiary.bankName && <p className="bank-name">{beneficiary.bankName}</p>}
                                        {beneficiary.isVerified && <span className="verified-badge">✓ Vérifié</span>}
                                    </div>
                                    <div className="beneficiary-actions">
                                        <button
                                            className="edit-btn"
                                            onClick={() => handleEdit(beneficiary)}
                                            title="Modifier le bénéficiaire"
                                        >
                                            ✏️
                                        </button>
                                        <button
                                            className="delete-btn"
                                            onClick={() => handleDelete(beneficiary.id)}
                                            title="Supprimer le bénéficiaire"
                                        >
                                            🗑️
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </>
            ) : (
                <form onSubmit={handleSubmit} className="beneficiary-form">
                    <h3>{editingId ? "Modifier le Bénéficiaire" : "Nouveau Bénéficiaire"}</h3>

                    <div className="form-group">
                        <label htmlFor="bankType">Type de Banque *</label>
                        <select
                            id="bankType"
                            name="bankType"
                            value={formData.bankType}
                            onChange={handleInputChange}
                            disabled={editingId}
                        >
                            <option value="SAME_BANK">🏦 Même Banque</option>
                            <option value="NATIONAL">🇹🇳 Banque Nationale</option>
                            <option value="INTERNATIONAL">🌍 Banque Internationale</option>
                        </select>
                    </div>

                    <div className={`form-group ${formErrors.name ? "has-error" : ""}`}>
                        <label htmlFor="name">Nom du Bénéficiaire *</label>
                        <input
                            id="name"
                            type="text"
                            name="name"
                            value={formData.name}
                            onChange={handleInputChange}
                            placeholder="Nom complet du bénéficiaire"
                            maxLength={100}
                            aria-invalid={!!formErrors.name}
                        />
                        {formErrors.name && <span className="error-message">{formErrors.name}</span>}
                    </div>

                    <div className={`form-group ${formErrors.accountNumber ? "has-error" : ""}`}>
                        <label htmlFor="accountNumber">Numéro de Compte *</label>
                        <input
                            id="accountNumber"
                            type="text"
                            name="accountNumber"
                            value={formData.accountNumber}
                            onChange={handleInputChange}
                            placeholder="Numéro de compte"
                            disabled={editingId}
                            aria-invalid={!!formErrors.accountNumber}
                        />
                        {formErrors.accountNumber && <span className="error-message">{formErrors.accountNumber}</span>}
                    </div>

                    {formData.bankType !== "SAME_BANK" && (
                        <div className="form-group">
                            <label htmlFor="bankName">Nom de la Banque</label>
                            <input
                                id="bankName"
                                type="text"
                                name="bankName"
                                value={formData.bankName}
                                onChange={handleInputChange}
                                placeholder="Nom de la banque"
                            />
                        </div>
                    )}

                    {formData.bankType === "NATIONAL" && (
                        <div className="form-group">
                            <label htmlFor="bankCode">Code Banque</label>
                            <input
                                id="bankCode"
                                type="text"
                                name="bankCode"
                                value={formData.bankCode}
                                onChange={handleInputChange}
                                placeholder="Code banque"
                                maxLength={11}
                            />
                        </div>
                    )}

                    {formData.bankType === "INTERNATIONAL" && (
                        <>
                            <div className={`form-group ${formErrors.iban ? "has-error" : ""}`}>
                                <label htmlFor="iban">IBAN *</label>
                                <input
                                    id="iban"
                                    type="text"
                                    name="iban"
                                    value={formData.iban}
                                    onChange={handleInputChange}
                                    placeholder="Ex: FR7612345678901234567890123"
                                    disabled={editingId}
                                    aria-invalid={!!formErrors.iban}
                                />
                                {formErrors.iban && <span className="error-message">{formErrors.iban}</span>}
                            </div>

                            <div className={`form-group ${formErrors.swiftCode ? "has-error" : ""}`}>
                                <label htmlFor="swiftCode">Code SWIFT/BIC</label>
                                <input
                                    id="swiftCode"
                                    type="text"
                                    name="swiftCode"
                                    value={formData.swiftCode}
                                    onChange={handleInputChange}
                                    placeholder="Ex: BNPAFRPP"
                                    maxLength={11}
                                    aria-invalid={!!formErrors.swiftCode}
                                />
                                {formErrors.swiftCode && <span className="error-message">{formErrors.swiftCode}</span>}
                            </div>

                            <div className="form-group">
                                <label htmlFor="country">Pays</label>
                                <input
                                    id="country"
                                    type="text"
                                    name="country"
                                    value={formData.country}
                                    onChange={handleInputChange}
                                    placeholder="Pays du bénéficiaire"
                                />
                            </div>
                        </>
                    )}

                    <div className="form-actions">
                        <button type="button" className="cancel-btn" onClick={resetForm}>
                            Annuler
                        </button>
                        <button type="submit" className="submit-btn" disabled={loading}>
                            {loading ? "Traitement..." : editingId ? "Mettre à jour" : "Ajouter"}
                        </button>
                    </div>
                </form>
            )}
        </div>
    )
}

export default BeneficiaryManagement
