import React from 'react';
import { LogResponse, LogFilters } from '../types';

interface LogTableProps {
    logs: LogResponse[];
    loading: boolean;
    onFilterChange: (filters: LogFilters) => void;
    filters: LogFilters;
}

const LogTable: React.FC<LogTableProps> = ({ logs, loading, onFilterChange, filters }) => {
    const formatTimestamp = (isoString: string | undefined): string => {
        if (!isoString) return '';
        try {
            const date = new Date(isoString);
            return date.toLocaleString();
        } catch (e) {
            return isoString;
        }
    };

    const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        onFilterChange({ ...filters, [name]: value });
    };

    return (
        <div style={{ margin: '20px auto', maxWidth: '90%', overflowX: 'auto' }}>
            <h2 style={{ textAlign: 'center' }}>Logs do Sistema</h2>

            <div style={{ display: 'flex', gap: '10px', marginBottom: '20px', flexWrap: 'wrap', justifyContent: 'center' }}>
                <input
                    type="text"
                    name="messageContains"
                    placeholder="Mensagem contém..."
                    value={filters.messageContains || ''}
                    onChange={handleFilterChange}
                    style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                />
                <select
                    name="level"
                    value={filters.level || ''}
                    onChange={handleFilterChange}
                    style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                >
                    <option value="">Todos os Níveis</option>
                    <option value="INFO">INFO</option>
                    <option value="WARN">WARN</option>
                    <option value="ERROR">ERROR</option>
                    <option value="DEBUG">DEBUG</option>
                </select>
                <input
                    type="datetime-local"
                    name="start"
                    value={filters.start || ''}
                    onChange={handleFilterChange}
                    style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                />
                <input
                    type="datetime-local"
                    name="end"
                    value={filters.end || ''}
                    onChange={handleFilterChange}
                    style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                />
            </div>

            {loading ? (
                <p style={{ textAlign: 'center' }}>Carregando logs...</p>
            ) : logs.length === 0 ? (
                <p style={{ textAlign: 'center' }}>Nenhum log encontrado com os filtros aplicados.</p>
            ) : (
                <table style={{ width: '100%', borderCollapse: 'collapse', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                    <thead>
                        <tr style={{ backgroundColor: '#f2f2f2' }}>
                            <th style={{ padding: '12px', border: '1px solid #ddd', textAlign: 'left' }}>ID</th>
                            <th style={{ padding: '12px', border: '1px solid #ddd', textAlign: 'left' }}>Mensagem</th>
                            <th style={{ padding: '12px', border: '1px solid #ddd', textAlign: 'left' }}>Nível</th>
                            <th style={{ padding: '12px', border: '1px solid #ddd', textAlign: 'left' }}>Origem</th>
                            <th style={{ padding: '12px', border: '1px solid #ddd', textAlign: 'left' }}>Timestamp</th>
                        </tr>
                    </thead>
                    <tbody>
                        {logs.map((log, index) => (
                            <tr key={log.id} style={{ backgroundColor: index % 2 === 0 ? '#ffffff' : '#f9f9f9' }}>
                                <td style={{ padding: '10px', border: '1px solid #ddd' }}>{log.id}</td>
                                <td style={{ padding: '10px', border: '1px solid #ddd' }}>{log.message}</td>
                                <td style={{ padding: '10px', border: '1px solid #ddd' }}>{log.level}</td>
                                <td style={{ padding: '10px', border: '1px solid #ddd' }}>{log.source}</td>
                                <td style={{ padding: '10px', border: '1px solid #ddd' }}>{formatTimestamp(log.timestamp)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default LogTable;