import React, { useState } from 'react';

const ProfileView = ({ user }) => {
  const [editMode, setEditMode] = useState(null);
  const [showOtpModal, setShowOtpModal] = useState(false);

  const profileSections = [
    {
      id: 'personal',
      title: 'Informations personnelles',
      icon: '👤',
      fields: [
        { key: 'firstName', label: 'Prénom', value: user?.firstName },
        { key: 'lastName', label: 'Nom', value: user?.lastName },
        { key: 'email', label: 'Email', value: user?.email }
      ]
    },
    {
      id: 'contact',
      title: 'Coordonnées',
      icon: '📍',
      fields: [
        { key: 'phone', label: 'Téléphone', value: '+216 XX XXX XXX' },
        { key: 'address', label: 'Adresse', value: 'Tunis, Tunisie' }
      ]
    },
    {
      id: 'security',
      title: 'Sécurité',
      icon: '🔒',
      fields: [
        { key: 'password', label: 'Mot de passe', value: '••••••••••••' }
      ]
    }
  ];

  const handleEdit = (sectionId) => {
    setEditMode(sectionId);
  };

  const handleSave = (sectionId) => {
    setShowOtpModal(true);
    setEditMode(null);
  };

  return (
    <div className="profile-view">
      <div className="profile-header">
        <div className="profile-avatar">
          <span>{user?.firstName?.[0]}{user?.lastName?.[0]}</span>
        </div>
        <h2>{user?.firstName} {user?.lastName}</h2>
        <p>Membre depuis 2024</p>
      </div>

      <div className="profile-sections">
        {profileSections.map(section => (
          <div key={section.id} className="profile-section">
            <div className="section-header">
              <div className="section-title">
                <span className="section-icon">{section.icon}</span>
                <h3>{section.title}</h3>
              </div>
              <button 
                className="edit-btn"
                onClick={() => editMode === section.id ? handleSave(section.id) : handleEdit(section.id)}
              >
                {editMode === section.id ? '💾' : '✏️'}
              </button>
            </div>
            
            <div className="section-fields">
              {section.fields.map(field => (
                <div key={field.key} className="field-row">
                  <label>{field.label}</label>
                  {editMode === section.id ? (
                    <input 
                      type={field.key === 'password' ? 'password' : 'text'}
                      defaultValue={field.key === 'password' ? '' : field.value}
                      placeholder={field.key === 'password' ? 'Nouveau mot de passe' : ''}
                    />
                  ) : (
                    <span>{field.value}</span>
                  )}
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      {showOtpModal && (
        <div className="otp-modal">
          <div className="modal-content">
            <h3>Vérification OTP</h3>
            <p>Un code de vérification a été envoyé à votre email</p>
            <input type="text" placeholder="Code à 6 chiffres" maxLength="6" />
            <div className="modal-actions">
              <button onClick={() => setShowOtpModal(false)}>Annuler</button>
              <button className="confirm-btn">Confirmer</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfileView;