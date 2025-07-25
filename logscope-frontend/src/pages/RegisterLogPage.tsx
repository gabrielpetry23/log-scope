import React, { useState } from 'react';
import LogForm from '../components/LogForm';
import { createLog } from '../services/api';
import { useAuth } from '../components/AuthProvider';
import { useNavigate } from 'react-router-dom';
import { LogRequest } from '../types';

const RegisterLogPage: React.FC = () => {
    const [statusMessage, setStatusMessage] = useState<string>('');
    const [loading, setLoading] = useState<boolean>(false);
    const { isAuthenticated, hasRole } = useAuth();
    const navigate = useNavigate();

    if (!isAuthenticated) {
        navigate('/login');
        return null;
    }
    const canCreateLog = hasRole(['COMPANY_SYSTEM', 'COMPANY_ADMIN', 'GLOBAL_ADMIN']);
    if (!canCreateLog) {
        return (
            <div style={{ textAlign: 'center', marginTop: '50px' }}>
                <h2>Acesso Negado</h2>
                <p>Você não tem permissão para cadastrar logs.</p>
                <button onClick={() => navigate('/logs/view')} style={{ padding: '10px 15px', backgroundColor: '#6c757d', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', marginTop: '10px' }}>
                    Ver Logs
                </button>
            </div>
        );
    }

    const handleCreateLog = async (logData: LogRequest) => {
        setLoading(true);
        setStatusMessage('');
        try {
            await createLog(logData);
            setStatusMessage('Log cadastrado com sucesso!');
        } catch (error: any) {
            setStatusMessage(`Erro ao cadastrar log: ${error.response?.data?.message || error.message}`);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '20px' }}>
            <LogForm onSubmit={handleCreateLog} loading={loading} />
            {statusMessage && (
                <p style={{ marginTop: '20px', color: statusMessage.includes('sucesso') ? 'green' : 'red' }}>
                    {statusMessage}
                </p>
            )}
        </div>
    );
};

export default RegisterLogPage;