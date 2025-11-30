import React from 'react';

const TransactionChart = () => {
  return (
    <div className="transaction-chart">
      <div className="chart-container">
        <svg width="100%" height="120" viewBox="0 0 300 120">
          <defs>
            <linearGradient id="chartGradient" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#667eea" stopOpacity="0.8"/>
              <stop offset="100%" stopColor="#667eea" stopOpacity="0.1"/>
            </linearGradient>
          </defs>
          
          {/* Chart line */}
          <path 
            d="M20,80 Q60,40 100,60 T180,50 Q220,30 280,45" 
            stroke="#667eea" 
            strokeWidth="3" 
            fill="none"
          />
          
          {/* Chart area */}
          <path 
            d="M20,80 Q60,40 100,60 T180,50 Q220,30 280,45 L280,100 L20,100 Z" 
            fill="url(#chartGradient)"
          />
          
          {/* Data points */}
          <circle cx="100" cy="60" r="4" fill="#667eea"/>
          <circle cx="180" cy="50" r="4" fill="#667eea"/>
          <circle cx="280" cy="45" r="4" fill="#667eea"/>
        </svg>
      </div>
      
      <div className="chart-info">
        <div className="balance-indicator">
          <span className="current-balance">11 563,736 DT</span>
          <span className="balance-date">12 nov</span>
        </div>
      </div>
    </div>
  );
};

export default TransactionChart;
