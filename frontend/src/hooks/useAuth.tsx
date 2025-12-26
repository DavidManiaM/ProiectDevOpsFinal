import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { authApi } from '../services/api';

interface AuthContextType {
  isAuthenticated: boolean;
  username: string | null;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [username, setUsername] = useState<string | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const storedUsername = localStorage.getItem('username');
    if (token) {
      setIsAuthenticated(true);
      setUsername(storedUsername);
    }
  }, []);

  const login = async (username: string, password: string) => {
    const response = await authApi.login(username, password);
    localStorage.setItem('token', response.data.token);
    localStorage.setItem('username', response.data.username);
    setIsAuthenticated(true);
    setUsername(response.data.username);
  };

  const register = async (username: string, email: string, password: string) => {
    const response = await authApi.register(username, email, password);
    localStorage.setItem('token', response.data.token);
    localStorage.setItem('username', response.data.username);
    setIsAuthenticated(true);
    setUsername(response.data.username);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    setIsAuthenticated(false);
    setUsername(null);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, username, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

