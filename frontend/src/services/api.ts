import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || '';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 responses
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export interface Price {
  id: number;
  ticker: string;
  symbolName: string;
  price: number;
  volume: number;
  movingAverage5: number | null;
  movingAverage20: number | null;
  percentChange: number | null;
  timestamp: string;
}

export interface Alert {
  id: number;
  ticker: string;
  symbolName: string;
  alertType: string;
  message: string;
  triggerValue: number;
  thresholdValue: number | null;
  isRead: boolean;
  timestamp: string;
}

export interface Symbol {
  id: number;
  ticker: string;
  name: string;
  type: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  message: string;
}

// Auth API
export const authApi = {
  login: (username: string, password: string) =>
    api.post<AuthResponse>('/api/auth/login', { username, password }),

  register: (username: string, email: string, password: string) =>
    api.post<AuthResponse>('/api/auth/register', { username, email, password }),
};

// Prices API
export const pricesApi = {
  getLatest: () => api.get<Price[]>('/api/prices'),

  getByTicker: (ticker: string) => api.get<Price>(`/api/prices/${ticker}`),

  getHistory: (ticker: string, limit = 100) =>
    api.get<Price[]>(`/api/prices/${ticker}/history?limit=${limit}`),

  getVariations: (ticker: string, hours = 24) =>
    api.get<Price[]>(`/api/prices/${ticker}/variations?hours=${hours}`),
};

// Alerts API
export const alertsApi = {
  getRecent: (hours = 24) => api.get<Alert[]>(`/api/alerts?hours=${hours}`),

  getUnread: () => api.get<Alert[]>('/api/alerts/unread'),

  getUnreadCount: () => api.get<{ count: number }>('/api/alerts/unread/count'),

  markAsRead: (id: number) => api.put(`/api/alerts/${id}/read`),

  markAllAsRead: () => api.put('/api/alerts/read-all'),
};

// Symbols API
export const symbolsApi = {
  getAll: () => api.get<Symbol[]>('/api/symbols'),

  getByTicker: (ticker: string) => api.get<Symbol>(`/api/symbols/${ticker}`),
};

export default api;

