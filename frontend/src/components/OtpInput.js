import React, { useState, useEffect } from 'react';

const OtpInput = ({ onVerify, onResend, expirySeconds = 60 }) => {
    const [otp, setOtp] = useState('');
    const [timeLeft, setTimeLeft] = useState(expirySeconds);
    const [canResend, setCanResend] = useState(false);

    useEffect(() => {
        if (timeLeft > 0) {
            const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
            return () => clearTimeout(timer);
        } else {
            setCanResend(true);
        }
    }, [timeLeft]);

    const handleResend = () => {
        setTimeLeft(expirySeconds);
        setCanResend(false);
        setOtp('');
        onResend();
    };

    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    return (
        <div className="otp-input-container">
            <div className="otp-timer">
                {timeLeft > 0 ? (
                    <p>Code expire dans <strong>{formatTime(timeLeft)}</strong></p>
                ) : (
                    <p style={{color: 'red'}}>Code expiré</p>
                )}
            </div>
            
            <input
                type="text"
                placeholder="Code de sécurité (6 chiffres)"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                maxLength="6"
                className="otp-input"
            />
            
            <div className="otp-actions">
                <button 
                    onClick={() => onVerify(otp)}
                    disabled={otp.length !== 6 || timeLeft === 0}
                    className="btn-primary"
                >
                    Vérifier
                </button>
                
                {canResend && (
                    <button 
                        onClick={handleResend}
                        className="btn-secondary"
                    >
                        Renvoyer le code
                    </button>
                )}
            </div>
        </div>
    );
};

export default OtpInput;
