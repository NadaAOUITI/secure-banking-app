import React, { useState } from 'react';
import PersonalInfoStep from './steps/PersonalInfoStep';
import EmailVerificationStep from './steps/EmailVerificationStep';
import IdentityVerificationStep from './steps/IdentityVerificationStep';
import AccountSetupStep from './steps/AccountSetupStep';
import ProductSelectionStep from './steps/ProductSelectionStep';
import ConfirmationStep from './steps/ConfirmationStep';
import ProgressBar from './ProgressBar';
import './OnboardingFlow.css';

const OnboardingFlow = () => {
  const [currentStep, setCurrentStep] = useState(1);
  const [formData, setFormData] = useState({
    personalInfo: {},
    emailVerification: {},
    identityVerification: {},
    accountSetup: {},
    productSelection: {}
  });

  const steps = [
    { id: 1, title: 'Informations personnelles', component: PersonalInfoStep },
    { id: 2, title: 'Vérification email', component: EmailVerificationStep },
    { id: 3, title: 'Vérification identité', component: IdentityVerificationStep },
    { id: 4, title: 'Configuration compte', component: AccountSetupStep },
    { id: 5, title: 'Sélection produits', component: ProductSelectionStep },
    { id: 6, title: 'Confirmation', component: ConfirmationStep }
  ];

  const handleNext = (stepData) => {
    const stepKey = Object.keys(formData)[currentStep - 1];
    setFormData(prev => ({
      ...prev,
      [stepKey]: stepData
    }));
    setCurrentStep(prev => prev + 1);
  };

  const handleBack = () => {
    setCurrentStep(prev => prev - 1);
  };

  const CurrentStepComponent = steps[currentStep - 1]?.component;

  return (
    <div className="onboarding-container">
      <div className="onboarding-header">
        <h1>Créer votre compte bancaire</h1>
        <ProgressBar currentStep={currentStep} totalSteps={steps.length} />
      </div>

      <div className="onboarding-content">
        {CurrentStepComponent && (
          <CurrentStepComponent
            data={formData[Object.keys(formData)[currentStep - 1]]}
            onNext={handleNext}
            onBack={currentStep > 1 ? handleBack : null}
            allData={formData}
          />
        )}
      </div>
    </div>
  );
};

export default OnboardingFlow;
