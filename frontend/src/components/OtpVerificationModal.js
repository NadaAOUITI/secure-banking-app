import React, { useState } from 'react';
import './ProfileUpdateModal.css';
import OtpInput from './OtpInput';

const OtpVerificationModal = ({ isOpen, onClose, onSuccess }) => {
    const [otp, setOtp] = useState('');
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');

    const initiateUpdate = async () => {
        setLoading(true);
        try {
            const response = await fetch('https://localhost:8080/api/profile/initiate-update', {
                method: 'POST',
                credentials: 'include'
            });
            const data = await response.json();
            
            if (data.success) {
                setMessage('Code de sécurité envoyé par email');
            } else {
                setMessage(data.message);
            }
        } catch (error) {
            setMessage('Erreur de connexion');
        }
        setLoading(false);
    };

    const verifyOtp = async () => {
        setLoading(true);
        try {
            const response = await fetch('https://localhost:8080/api/profile/update', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ otp })
            });
            const data = await response.json();
            
            if (data.success) {
                onSuccess();
                onClose();
            } else {
                setMessage(data.message);
            }
        } catch (error) {
            setMessage('Erreur de connexion');
        }
        setLoading(false);
    };

    if (!isOpen) return null;

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <div className="modal-header">
                    <h3>Vérification de sécurité</h3>
                    <button className="close-btn" onClick={onClose}>×</button>
                </div>

                <div className="modal-body">
                    {!message ? (
                        <>
                            <p>Un code de sécurité sera envoyé à votre email pour autoriser les modifications.</p>
                            <button 
                                className="btn-primary" 
                                onClick={initiateUpdate}
                                disabled={loading}
                            >
                                {loading ? 'Envoi...' : 'Envoyer le code'}
                            </button>
                        </>
                    ) : (
                        <>
                            <p>Entrez le code de sécurité reçu par email :</p>
                            <OtpInput 
                                onVerify={verifyOtp}
                                onResend={initiateUpdate}
                                expirySeconds={60}
                            />
                        </>
                    )}

                    {message && (
                        <div className={`message ${message.includes('envoyé') ? 'success' : 'error'}`}>
                            {message}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default OtpVerificationModal;
