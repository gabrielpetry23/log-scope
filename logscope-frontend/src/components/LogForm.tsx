import React, { useState } from 'react';
import { LogRequest } from '../types';

interface LogFormProps {
    onSubmit: (logData: LogRequest) => void;
    loading: boolean;
}

const LogForm: React.FC<LogFormProps> = ({ onSubmit, loading }) => {
    const [message, setMessage] = useState<string>('');
    const [level, setLevel] = useState<string>('INFO');
    const [source, setSource] = useState<string>('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSubmit({ message, level, source });
        setMessage('');
        setLevel('INFO');
        setSource('');
    };

    return (
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '10px', maxWidth: '400px', margin: '20px auto', padding: '20px', border: '1px solid #ccc', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
            <h2 style={{ textAlign: 'center' }}>Cadastrar Novo Log</h2>
            <div>
                <label htmlFor="message">Mensagem:</label>
                <textarea
                    id="message"
                    value={message}
                    onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setMessage(e.target.value)}
                    required
                    rows={4}
                    style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
                />
            </div>
            <div>
                <label htmlFor="level">Nível:</label>
                <select
                    id="level"
                    value={level}
                    onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setLevel(e.target.value)}
                    required
                    style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
                >
                    <option value="INFO">INFO</option>
                    <option value="WARN">WARN</option>
                    <option value="ERROR">ERROR</option>
                    <option value="DEBUG">DEBUG</option>
                </select>
            </div>
            <div>
                <label htmlFor="source">Origem:</label>
                <input
                    type="text"
                    id="source"
                    value={source}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSource(e.target.value)}
                    required
                    style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
                />
            </div>
            <button type="submit" disabled={loading} style={{ padding: '10px 15px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                {loading ? 'Cadastrando...' : 'Cadastrar Log'}
            </button>
        </form>
    );
};

export default LogForm;