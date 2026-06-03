import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://180.235.121.245:8060/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor to attach the language parameter to all requests
// Always force English for API calls — the UI is in English.
// The backend falls back to Hindi/Tamil based on lang param, which we never want.
api.interceptors.request.use((config) => {
  if (!config.params) {
    config.params = {};
  }
  config.params.lang = 'en';

  return config;
}, (error) => {
  return Promise.reject(error);
});

export const evaluateEligibility = async (profile) => {
  const response = await api.post('/evaluate', profile);
  return response.data;
};

export const fetchSchemes = async (params) => {
  const response = await api.get('/schemes', { params });
  return response.data;
};

export const fetchUpdates = async () => {
  const response = await api.get('/updates');
  return response.data;
};

// Saved Schemes & Monitoring
export const saveScheme = async (schemeId, userId) => {
  const response = await api.post(`/schemes/${schemeId}/save`, { user_id: userId });
  return response.data;
};

export const unsaveScheme = async (schemeId, userId) => {
  const response = await api.delete(`/schemes/${schemeId}/save`, { params: { user_id: userId } });
  return response.data;
};

export const getSavedSchemes = async (userId) => {
  const response = await api.get(`/saved_schemes`, { params: { user_id: userId } });
  return response.data;
};

// Admin endpoints
export const addScheme = async (schemeData) => {
  const response = await api.post('/admin/add-scheme', schemeData);
  return response.data;
};

export const updateScheme = async (id, schemeData) => {
  const response = await api.put(`/admin/update-scheme/${id}`, schemeData);
  return response.data;
};

export const deleteScheme = async (id) => {
  const response = await api.delete(`/admin/delete-scheme/${id}`);
  return response.data;
};

export const fetchLogs = async () => {
  const response = await api.get('/admin/logs');
  return response.data;
};

export default api;
