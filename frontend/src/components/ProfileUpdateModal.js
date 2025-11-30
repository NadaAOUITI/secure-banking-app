import React, { useState } from 'react';
import './ProfileUpdateModal.css';

const ProfileUpdateModal = ({ isOpen, onClose, updateType, onSuccess }) => {
    const [step, setStep] = useState(1); // 1: Update form, 2: Email confirmation
    const [formData, setFormData] = useState({
        otp: '',
        currentPassword: '',
        newPassword: '',
        confirmNewPassword: '',
        newEmail: '',
        newPhone: '',
        newAddress: '',
        newCountry: '',
        confirmationOtp: ''
    });
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [otpExpiry, setOtpExpiry] = useState(0);
    const [pendingEmail, setPendingEmail] = useState('');

    const handleInputChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const submitUpdate = async () => {
        setLoading(true);
        try {
            const updateData = {};
            
            if (updateType === 'password') {
                updateData.currentPassword = formData.currentPassword;
                updateData.newPassword = formData.newPassword;
                updateData.confirmNewPassword = formData.confirmNewPassword;
            } else if (updateType === 'email') {
                updateData.newEmail = formData.newEmail;
            } else if (updateType === 'phone') {
                updateData.newPhone = formData.newPhone;
            } else if (updateType === 'address') {
                updateData.newAddress = formData.newAddress;
            } else if (updateType === 'country') {
                updateData.newCountry = formData.newCountry;
            }
            
            const response = await fetch('http://localhost:8080/api/profile/update', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(updateData)
            });
            const data = await response.json();
            
            if (data.success) {
                if (data.requiresConfirmation && updateType === 'email') {
                    setStep(2);
                    setPendingEmail(formData.newEmail);
                    setMessage(data.message);
                } else {
                    setMessage(data.message);
                    if (data.logoutRequired) {
                        setTimeout(() => window.location.href = '/login', 2000);
                    } else {
                        setTimeout(() => {
                            onSuccess(data);
                            onClose();
                        }, 2000);
                    }
                }
            } else {
                setMessage(data.message);
            }
        } catch (error) {
            setMessage('Erreur de connexion');
        }
        setLoading(false);
    };

    const confirmEmailChange = async () => {
        setLoading(true);
        try {
            const response = await fetch('http://localhost:8080/api/profile/confirm-email', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ confirmationOtp: formData.confirmationOtp })
            });
            const data = await response.json();
            
            if (data.success) {
                setMessage(data.message);
                setTimeout(() => {
                    onSuccess(data);
                    onClose();
                }, 2000);
            } else {
                setMessage(data.message);
            }
        } catch (error) {
            setMessage('Erreur de connexion');
        }
        setLoading(false);
    };

    const renderPasswordForm = () => (
        <div className="form-group">
            <input
                type="password"
                name="currentPassword"
                placeholder="Mot de passe actuel"
                value={formData.currentPassword}
                onChange={handleInputChange}
                required
            />
            <input
                type="password"
                name="newPassword"
                placeholder="Nouveau mot de passe"
                value={formData.newPassword}
                onChange={handleInputChange}
                required
            />
            <input
                type="password"
                name="confirmNewPassword"
                placeholder="Confirmer nouveau mot de passe"
                value={formData.confirmNewPassword}
                onChange={handleInputChange}
                required
            />
        </div>
    );

    const renderEmailForm = () => (
        <div className="form-group">
            <input
                type="email"
                name="newEmail"
                placeholder="Nouvel email"
                value={formData.newEmail}
                onChange={handleInputChange}
                pattern="[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$"
                title="Veuillez entrer un email valide"
                required
            />
        </div>
    );

    const renderPhoneForm = () => (
        <div className="form-group">
            <input
                type="tel"
                name="newPhone"
                placeholder="Nouveau téléphone"
                value={formData.newPhone}
                onChange={handleInputChange}
                pattern="[+]?[0-9]{8,15}"
                required
            />
        </div>
    );

    const renderAddressForm = () => (
        <div className="form-group">
            <input
                type="text"
                name="newAddress"
                placeholder="Nouvelle adresse"
                value={formData.newAddress}
                onChange={handleInputChange}
                required
            />
        </div>
    );

    const renderCountryForm = () => (
        <div className="form-group">
            <input
                type="text"
                name="newCountry"
                placeholder="Nouveau pays"
                value={formData.newCountry}
                onChange={handleInputChange}
                required
            />
        </div>
    );

    const renderEmailConfirmationForm = () => (
        <div className="form-group">
            <p>Un code de confirmation a été envoyé à <strong>{pendingEmail}</strong></p>
            <input
                type="text"
                name="confirmationOtp"
                placeholder="Code de confirmation (6 chiffres)"
                value={formData.confirmationOtp}
                onChange={handleInputChange}
                maxLength="6"
                required
            />
            <button 
                className="btn-primary" 
                onClick={confirmEmailChange}
                disabled={loading || formData.confirmationOtp.length !== 6}
            >
                {loading ? 'Confirmation...' : 'Confirmer l\'email'}
            </button>
        </div>
    );

    if (!isOpen) return null;

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <div className="modal-header">
                    <h3>Modification du profil</h3>
                    <button className="close-btn" onClick={onClose}>×</button>
                </div>

                <div className="modal-body">
                    {step === 1 ? (
                        <>
                            {updateType === 'password' && renderPasswordForm()}
                            {updateType === 'email' && renderEmailForm()}
                            {updateType === 'phone' && renderPhoneForm()}
                            {updateType === 'address' && renderAddressForm()}
                            {updateType === 'country' && renderCountryForm()}

                            <button 
                                className="btn-primary" 
                                onClick={submitUpdate}
                                disabled={loading}
                            >
                                {loading ? 'Modification...' : 'Confirmer'}
                            </button>
                        </>
                    ) : (
                        renderEmailConfirmationForm()
                    )}
                </div>

                {message && (
                    <div className={`message ${message.includes('succès') ? 'success' : 'error'}`}>
                        {message}
                    </div>
                )}
            </div>
        </div>
    );
};

export default ProfileUpdateModal;