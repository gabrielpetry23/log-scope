import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { loginUser as apiLogin, logoutUser } from '../services/api';
import { AuthResult, UserRole } from '../types';

interface AuthContextType {
    isAuthenticated: boolean;
    userRoles: UserRole[];
    hasRole: (requiredRoles: UserRole | UserRole[]) => boolean;
    login: (username: string, password: string) => Promise<AuthResult>;
    logout: () => void;
    loading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

interface AuthProviderProps {
    children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
    const [userRoles, setUserRoles] = useState<UserRole[]>([]);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        const token = localStorage.getItem('authToken');
        const rolesString = localStorage.getItem('userRoles');
        if (token && rolesString) {
            try {
                setIsAuthenticated(true);
                setUserRoles(JSON.parse(rolesString) as UserRole[]);
            } catch (e) {
                console.error("Erro ao parsear roles do localStorage", e);
                logoutUser();
            }
        }
        setLoading(false);
    }, []);

    const login = async (username: string, password: string): Promise<AuthResult> => {
        setLoading(true);
        const result = await apiLogin(username, password);
        if (result.success && result.roles) {
            setIsAuthenticated(true);
            setUserRoles(result.roles as UserRole[]);
        } else {
            setIsAuthenticated(false);
            setUserRoles([]);
        }
        setLoading(false);
        return result;
    };

    const logout = (): void => {
        logoutUser();
        setIsAuthenticated(false);
        setUserRoles([]);
    };

    const hasRole = (requiredRoles: UserRole | UserRole[]): boolean => {
        if (!userRoles || userRoles.length === 0) return false;
        const rolesToCheck = Array.isArray(requiredRoles) ? requiredRoles : [requiredRoles];
        return rolesToCheck.some(role => userRoles.includes(role));
    };

    const contextValue: AuthContextType = { isAuthenticated, userRoles, hasRole, login, logout, loading };

    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};