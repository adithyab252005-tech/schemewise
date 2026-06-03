import { useState, useCallback } from 'react';
import api, { saveScheme, unsaveScheme, getSavedSchemes } from '../services/api';
import { sanitizeProfileForEvaluation } from '../utils/profileSanitizer';

export const useSchemes = () => {
    const [schemes, setSchemes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [schemeDetails, setSchemeDetails] = useState(null);

    const fetchAllSchemes = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.get('/schemes');
            setSchemes(response.data);
            return response.data;
        } catch (err) {
            setError(err.message || 'Failed to fetch schemes');
            return null;
        } finally {
            setLoading(false);
        }
    }, []);

    const fetchSchemeDetails = useCallback(async (id) => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.get(`/schemes/${id}`);
            setSchemeDetails(response.data);
            return response.data;
        } catch (err) {
            setError(err.message || 'Failed to fetch scheme details');
            return null;
        } finally {
            setLoading(false);
        }
    }, []);

    const fetchUpdates = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.get('/updates');
            return response.data; // Note: doesn't set global scheme state, usually just for updates page
        } catch (err) {
            setError(err.message || 'Failed to fetch updates');
            return null;
        } finally {
            setLoading(false);
        }
    }, []);

    const evaluateProfile = useCallback(async (profileData) => {
        setLoading(true);
        setError(null);
        try {
            // Always sanitize: handles ZK-encrypted fields, null guards, and field name mapping
            const clean = sanitizeProfileForEvaluation(profileData);
            const response = await api.post('/evaluate', clean);
            return response.data; // Returns scored schemes
        } catch (err) {
            const msg = err?.response?.data?.detail || err.message || 'Failed to evaluate profile';
            setError(msg);
            return null;
        } finally {
            setLoading(false);
        }
    }, []);

    const evaluateTrajectory = useCallback(async (profileData) => {
        try {
            const response = await api.post('/trajectory', profileData);
            return response.data;
        } catch (err) {
            return null;
        }
    }, []);

    const saveUserScheme = useCallback(async (schemeId, userId) => {
        setLoading(true);
        setError(null);
        try {
            const response = await saveScheme(schemeId, userId);
            return response;
        } catch (err) {
            setError(err.message || 'Failed to save scheme');
            return null;
        } finally {
            setLoading(false);
        }
    }, []);

    const unsaveUserScheme = useCallback(async (schemeId, userId) => {
        setLoading(true);
        setError(null);
        try {
            const response = await unsaveScheme(schemeId, userId);
            return response;
        } catch (err) {
            setError(err.message || 'Failed to remove scheme');
            return null;
        } finally {
            setLoading(false);
        }
    }, []);

    const fetchSavedSchemes = useCallback(async (userId) => {
        setLoading(true);
        setError(null);
        try {
            const response = await getSavedSchemes(userId);
            return response;
        } catch (err) {
            setError(err.message || 'Failed to fetch saved schemes');
            return null;
        } finally {
            setLoading(false);
        }
    }, []);

    const compareSchemes = useCallback(async (sidA, sidB, profile) => {
        setLoading(true);
        setError(null);
        try {
            const clean = sanitizeProfileForEvaluation(profile);
            const response = await api.post('/chat/compare', {
                scheme_id_a: sidA,
                scheme_id_b: sidB,
                user_profile: clean
            });
            return response.data;
        } catch (err) {
            setError(err.message || 'Failed to compare schemes');
            return null;
        } finally {
            setLoading(false);
        }
    }, []);

    return {
        schemes,
        schemeDetails,
        loading,
        error,
        fetchAllSchemes,
        fetchSchemeDetails,
        fetchUpdates,
        evaluateProfile,
        evaluateTrajectory,
        saveUserScheme,
        unsaveUserScheme,
        fetchSavedSchemes,
        compareSchemes
    };
};
