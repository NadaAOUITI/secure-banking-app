import React from 'react';
import OnboardingFlow from '../../components/onboarding/OnboardingFlow';
import './OnboardingPage.css';

const OnboardingPage = () => {
  return (
    <div className="onboarding-page">
      <div className="onboarding-background">
        <div className="background-pattern"></div>
      </div>
      <OnboardingFlow />
    </div>
  );
};

export default OnboardingPage;
