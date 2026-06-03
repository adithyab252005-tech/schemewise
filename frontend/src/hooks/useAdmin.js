import { useState, useCallback } from 'react';
import api from '../services/api';

export const useAdmin = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchLogs = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.get('/admin/logs');
            return response.data;
        } catch (err) {
            setError(err.message || 'Failed to fetch logs');
            return null;
        } finally {
            setLoading(false);
        }
    }, []);

    const addScheme = async (schemeData) => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.post('/admin/add-scheme', schemeData);
            return response.data;
        } catch (err) {
            setError(err.message || 'Failed to add scheme');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const updateScheme = async (id, schemeData) => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.put(`/admin/update-scheme/${id}`, schemeData);
            return response.data;
        } catch (err) {
            setError(err.message || 'Failed to update scheme');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const deleteScheme = async (id) => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.delete(`/admin/delete-scheme/${id}`);
            return response.data;
        } catch (err) {
            setError(err.message || 'Failed to delete scheme');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const testSlm = async (testData) => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.post('/admin/test-slm', testData);
            return response.data;
        } catch (err) {
            setError(err.message || 'Failed to test SLM');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    return {
        loading,
        error,
        fetchLogs,
        addScheme,
        updateScheme,
        deleteScheme,
        testSlm
    };
};
