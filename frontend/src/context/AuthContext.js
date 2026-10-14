import { createContext } from 'react';

export const AuthContext = createContext(null);
export { useAuth } from './useAuth';
export default AuthContext;

