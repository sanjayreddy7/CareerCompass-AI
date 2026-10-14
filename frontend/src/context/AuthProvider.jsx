import { useCallback, useEffect, useMemo, useState } from 'react';
import AuthContext from './AuthContext';

function getInitialToken() {
  try {
    return localStorage.getItem('token') || null;
  } catch {
    return null;
  }
}

function getInitialUser() {
  try {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(getInitialToken);
  const [user, setUser] = useState(getInitialUser);
  const [loading] = useState(false);

  // Synchronize state across tabs or when external 401 interceptor triggers logout
  useEffect(() => {
    function handleStorage(event) {
      if (event.key === 'token') {
        setToken(event.newValue);
      }
      if (event.key === 'user') {
        try {
          setUser(event.newValue ? JSON.parse(event.newValue) : null);
        } catch {
          setUser(null);
        }
      }
    }

    function handleAuthLogout() {
      setToken(null);
      setUser(null);
    }

    window.addEventListener('storage', handleStorage);
    window.addEventListener('auth-logout', handleAuthLogout);

    return () => {
      window.removeEventListener('storage', handleStorage);
      window.removeEventListener('auth-logout', handleAuthLogout);
    };
  }, []);

  const login = useCallback((newToken, newUser) => {
    if (newToken) {
      localStorage.setItem('token', newToken);
      setToken(newToken);
    }
    if (newUser) {
      localStorage.setItem('user', JSON.stringify(newUser));
      setUser(newUser);
    }
  }, []);

  const updateUser = useCallback((updatedUser) => {
    if (updatedUser) {
      localStorage.setItem('user', JSON.stringify(updatedUser));
      setUser(updatedUser);
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }, []);

  const value = useMemo(() => ({
    token,
    user,
    loading,
    isAuthenticated: Boolean(token),
    login,
    updateUser,
    logout,
  }), [token, user, loading, login, updateUser, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
