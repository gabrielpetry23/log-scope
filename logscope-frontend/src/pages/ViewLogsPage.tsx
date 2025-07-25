import React, { useState, useEffect, useCallback } from 'react';
import LogTable from '../components/LogTable';
import { getLogs } from '../services/api';
import { useAuth } from '../components/AuthProvider';
import { useNavigate } from 'react-router-dom';
import debounce from 'lodash.debounce';
import { LogResponse, LogFilters } from '../types';

const ViewLogsPage: React.FC = () => {
    const [logs, setLogs] = useState<LogResponse[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string>('');
    const [filters, setFilters] = useState<LogFilters>({});
    const { isAuthenticated, hasRole } = useAuth();
    const navigate = useNavigate();

    const debouncedGetLogs = useCallback(
        debounce(async (currentFilters: LogFilters) => {
            setLoading(true);
            setError('');
            try {
                const fetchedLogs = await getLogs(currentFilters);
                setLogs(fetchedLogs);
            } catch (err: any) {
                setError(`Erro ao carregar logs: ${err.response?.data?.message || err.message}`);
            } finally {
                setLoading(false);
            }
        }, 500),
        []
    );

    useEffect(() => {
        const formattedFilters: LogFilters = { ...filters };
        
        Object.keys(formattedFilters).forEach(key => {
            const value = formattedFilters[key as keyof LogFilters];
            if (value === '' || value === null || value === undefined) {
                delete formattedFilters[key as keyof LogFilters];
            }
        });
        
        if (formattedFilters.start) {
            let dateStr = formattedFilters.start;
            if (dateStr.length === 16) {
                dateStr += ':00';
            }
            formattedFilters.start = dateStr;
        }
        if (formattedFilters.end) {
            let dateStr = formattedFilters.end;
            if (dateStr.length === 16) {
                dateStr += ':00';
            }
            formattedFilters.end = dateStr;
        }

        console.log('Filtros formatados:', formattedFilters);
        
        if (formattedFilters.start && formattedFilters.end) {
            console.log('🔍 Ambas as datas presentes');
            console.log('Start:', formattedFilters.start);
            console.log('End:', formattedFilters.end);
            
            const startDate = new Date(formattedFilters.start);
            const endDate = new Date(formattedFilters.end);
            console.log('Start Date object:', startDate);
            console.log('End Date object:', endDate);
            console.log('Start válida:', !isNaN(startDate.getTime()));
            console.log('End válida:', !isNaN(endDate.getTime()));
            
            if (startDate >= endDate) {
                console.warn('⚠️ ATENÇÃO: Data de início é maior ou igual à data de fim!');
                console.warn('Start >= End:', startDate.toISOString(), '>=', endDate.toISOString());
                setError('Data de início deve ser anterior à data de fim');
                setLoading(false);
                return;
            }
        }
        
        debouncedGetLogs(formattedFilters);
    }, [filters, debouncedGetLogs]);

    const handleFilterChange = (newFilters: LogFilters) => {
        setFilters(newFilters);
    };

    if (!isAuthenticated) {
        navigate('/login');
        return null;
    }
    
    const canViewLogs = hasRole(['COMPANY_ADMIN', 'COMPANY_VIEWER', 'GLOBAL_ADMIN', 'GLOBAL_SUPPORT']);
    if (!canViewLogs) {
        return (
            <div style={{ textAlign: 'center', marginTop: '50px' }}>
                <h2>Acesso Negado</h2>
                <p>Você não tem permissão para visualizar logs.</p>
                <button onClick={() => navigate('/logs/register')} style={{ padding: '10px 15px', backgroundColor: '#6c757d', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', marginTop: '10px' }}>
                    Cadastrar Log
                </button>
            </div>
        );
    }

    return (
        <div style={{ padding: '20px' }}>
            {error && <p style={{ color: 'red', textAlign: 'center' }}>{error}</p>}
            <LogTable logs={logs} loading={loading} onFilterChange={handleFilterChange} filters={filters} />
        </div>
    );
};

export default ViewLogsPage;