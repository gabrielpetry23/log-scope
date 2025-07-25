// App.tsx
import React, { ReactNode } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom';
import RegisterLogPage from './pages/RegisterLogPage';
import ViewLogsPage from './pages/ViewLogsPage';
import LoginPage from './pages/LoginPage';
import { AuthProvider, useAuth } from './components/AuthProvider';
import { UserRole } from './types';

interface PrivateRouteProps {
    children: ReactNode;
    requiredRoles?: UserRole[];
}

const PrivateRoute: React.FC<PrivateRouteProps> = ({ children, requiredRoles }) => {
    const { isAuthenticated, loading, hasRole } = useAuth();

    if (loading) {
        return <div style={{ textAlign: 'center', marginTop: '50px' }}>Carregando autenticação...</div>;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (requiredRoles && !hasRole(requiredRoles)) {
        return (
            <div style={{ textAlign: 'center', marginTop: '50px' }}>
                <h2>Acesso Negado</h2>
                <p>Você não tem permissão para acessar esta página.</p>
                <Link to="/logs/view" style={{ display: 'inline-block', marginTop: '15px', padding: '10px 20px', backgroundColor: '#007bff', color: 'white', textDecoration: 'none', borderRadius: '4px' }}>
                    Ir para Página Inicial
                </Link>
            </div>
        );
    }

    return <>{children}</>;
};

const AppHeader: React.FC = () => {
    const { isAuthenticated, hasRole, logout } = useAuth();

    return (
        <nav style={{ padding: '15px 20px', backgroundColor: '#333', color: 'white', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div style={{ display: 'flex', gap: '20px' }}>
                <Link to="/" style={{ color: 'white', textDecoration: 'none', fontWeight: 'bold' }}>LogScope</Link>
                {isAuthenticated && hasRole(['COMPANY_ADMIN', 'COMPANY_VIEWER', 'GLOBAL_ADMIN', 'GLOBAL_SUPPORT']) && (
                    <Link to="/logs/view" style={{ color: 'white', textDecoration: 'none' }}>Ver Logs</Link>
                )}
                {isAuthenticated && hasRole(['COMPANY_SYSTEM', 'COMPANY_ADMIN', 'GLOBAL_ADMIN']) && (
                    <Link to="/logs/register" style={{ color: 'white', textDecoration: 'none' }}>Cadastrar Log</Link>
                )}
            </div>
            <div>
                {isAuthenticated ? (
                    <button onClick={logout} style={{ background: 'none', border: '1px solid white', color: 'white', padding: '8px 12px', borderRadius: '4px', cursor: 'pointer' }}>
                        Logout
                    </button>
                ) : (
                    <Link to="/login" style={{ color: 'white', textDecoration: 'none' }}>Login</Link>
                )}
            </div>
        </nav>
    );
};

const App: React.FC = () => {
    return (
        <Router>
            <AuthProvider>
                <AppHeader />
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/" element={<Navigate to="/logs/view" replace />} />

                    <Route
                        path="/logs/register"
                        element={
                            <PrivateRoute requiredRoles={['COMPANY_SYSTEM', 'COMPANY_ADMIN', 'GLOBAL_ADMIN']}>
                                <RegisterLogPage />
                            </PrivateRoute>
                        }
                    />
                    <Route
                        path="/logs/view"
                        element={
                            <PrivateRoute requiredRoles={['COMPANY_ADMIN', 'COMPANY_VIEWER', 'GLOBAL_ADMIN', 'GLOBAL_SUPPORT']}>
                                <ViewLogsPage />
                            </PrivateRoute>
                        }
                    />

                    <Route path="*" element={<h1 style={{ textAlign: 'center', marginTop: '50px' }}>Página Não Encontrada</h1>} />
                </Routes>
            </AuthProvider>
        </Router>
    );
};

export default App;