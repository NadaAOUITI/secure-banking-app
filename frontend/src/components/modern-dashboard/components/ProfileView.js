import React, { useState } from 'react';
import { useAccountDetails } from '../../../hooks/useAccountDetails';
import ProfileUpdateModal from '../../ProfileUpdateModal';
import OtpVerificationModal from '../../OtpVerificationModal';

const ProfileView = ({ user }) => {
  const { data: accountData, loading, error } = useAccountDetails();
  const [showOtpModal, setShowOtpModal] = useState(false);
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [updateType, setUpdateType] = useState('');
  const [isEditMode, setIsEditMode] = useState(false);

  if (loading) return <div className="loading-spinner">Chargement...</div>;
  if (error) return <div className="error-message">{error}</div>;

  const profileSections = [
    {
      id: 'personal',
      title: 'Informations personnelles',
      icon: '👤',
      fields: [
        { key: 'firstName', label: 'Prénom', value: accountData?.firstName },
        { key: 'lastName', label: 'Nom', value: accountData?.lastName },
        { key: 'email', label: 'Email', value: accountData?.email }
      ]
    },
    {
      id: 'contact',
      title: 'Coordonnées',
      icon: '📍',
      fields: [
        { key: 'phone', label: 'Téléphone', value: accountData?.phone || 'Non renseigné' },
        { key: 'address', label: 'Adresse', value: accountData?.address || 'Non renseignée' },
        { key: 'country', label: 'Pays', value: accountData?.country || 'Non renseigné' }
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

  return (
    <div className="profile-view">
      <div className="profile-header">
        <div className="profile-avatar">
          <span>{accountData?.firstName?.[0]}{accountData?.lastName?.[0]}</span>
        </div>
        <h2>{accountData?.firstName} {accountData?.lastName}</h2>
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
            </div>
            
            <div className="section-fields">
              {section.fields.map(field => (
                <div key={field.key} className="field-row">
                  <label>{field.label}</label>
                  <span>{field.value}</span>
                  {!isEditMode && section.id === 'contact' && (
                    <button 
                      className="edit-btn"
                      onClick={() => setShowOtpModal(true)}
                    >
                      ✏️
                    </button>
                  )}
                  {!isEditMode && section.id === 'security' && field.key === 'password' && (
                    <button 
                      className="edit-btn"
                      onClick={() => setShowOtpModal(true)}
                    >
                      ✏️
                    </button>
                  )}
                  {!isEditMode && section.id === 'personal' && field.key === 'email' && (
                    <button 
                      className="edit-btn"
                      onClick={() => setShowOtpModal(true)}
                    >
                      ✏️
                    </button>
                  )}
                  {isEditMode && section.id === 'contact' && field.key === 'phone' && (
                    <button 
                      className="edit-btn"
                      onClick={() => {
                        setUpdateType('phone');
                        setShowUpdateModal(true);
                      }}
                    >
                      ✏️
                    </button>
                  )}
                  {isEditMode && section.id === 'contact' && field.key === 'address' && (
                    <button 
                      className="edit-btn"
                      onClick={() => {
                        setUpdateType('address');
                        setShowUpdateModal(true);
                      }}
                    >
                      ✏️
                    </button>
                  )}
                  {isEditMode && section.id === 'contact' && field.key === 'country' && (
                    <button 
                      className="edit-btn"
                      onClick={() => {
                        setUpdateType('country');
                        setShowUpdateModal(true);
                      }}
                    >
                      ✏️
                    </button>
                  )}
                  {isEditMode && section.id === 'security' && field.key === 'password' && (
                    <button 
                      className="edit-btn"
                      onClick={() => {
                        setUpdateType('password');
                        setShowUpdateModal(true);
                      }}
                    >
                      ✏️
                    </button>
                  )}
                  {isEditMode && section.id === 'personal' && field.key === 'email' && (
                    <button 
                      className="edit-btn"
                      onClick={() => {
                        setUpdateType('email');
                        setShowUpdateModal(true);
                      }}
                    >
                      ✏️
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>


      
      <OtpVerificationModal 
        isOpen={showOtpModal}
        onClose={() => setShowOtpModal(false)}
        onSuccess={() => {
          setShowOtpModal(false);
          setIsEditMode(true);
        }}
      />
      
      <ProfileUpdateModal 
        isOpen={showUpdateModal}
        onClose={() => {
          setShowUpdateModal(false);
          setUpdateType('');
        }}
        updateType={updateType}
        onSuccess={(data) => {
          console.log('Profil mis à jour:', data);
          setShowUpdateModal(false);
          setUpdateType('');
          setIsEditMode(false);
          if (data.logoutRequired) {
            return;
          }
        }}
      />
    </div>
  );
};

export default ProfileView;