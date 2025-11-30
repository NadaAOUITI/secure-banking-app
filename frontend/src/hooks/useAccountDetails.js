import { useState, useEffect } from 'react';
import { accountService } from '../services/accountService';

export const useAccountDetails = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchAccountDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const accountData = await accountService.getAccountDetails();
      setData(accountData);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAccountDetails();
  }, []);

  return {
    data,
    loading,
    error,
    refetch: fetchAccountDetails
  };
};
