import React from 'react';
import './ProgressBar.css';

const ProgressBar = ({ currentStep, totalSteps }) => {
  const progress = (currentStep / totalSteps) * 100;

  return (
    <div className="progress-container">
      <div className="progress-bar">
        <div 
          className="progress-fill" 
          style={{ width: `${progress}%` }}
        ></div>
      </div>
      <div className="step-indicator">
        Étape {currentStep} sur {totalSteps}
      </div>
    </div>
  );
};

export default ProgressBar;
