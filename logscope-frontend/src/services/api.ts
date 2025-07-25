import axios from 'axios';
import { LogRequest, LogResponse, AuthResult, LogFilters, UserRole } from '../types';

const API_BASE_URL = 'http://localhost:8080';

const api = axios.create({
    baseURL: `${API_BASE_URL}/api/v1`,
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('authToken');
        if (token) {
            config.headers = config.headers || {};
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export const loginUser = async (username: string, password: string): Promise<AuthResult> => {
    try {
        interface JwtResponseDTO {
            token: string;
            username: string;
            roles: string[];
            clientId: string;
        }
        
        const response = await axios.post<JwtResponseDTO>(`${API_BASE_URL}/oauth/login`, { 
            username, 
            password 
        });
        
        const { token, roles } = response.data;
        
        const cleanRoles = roles.map(role => role.replace(/^ROLE_/, ''));
        
        localStorage.setItem('authToken', token);
        localStorage.setItem('userRoles', JSON.stringify(cleanRoles));
        return { success: true, token, roles: cleanRoles };
    } catch (error: any) {
        console.error('Erro no login:', error.response?.data || error.message);
        return { 
            success: false, 
            message: error.response?.data?.message || 'Credenciais inválidas' 
        };
    }
};

export const createLog = async (logData: LogRequest): Promise<void> => {
    try {
        await api.post('/logs', logData);
    } catch (error: any) {
        console.error('Erro ao cadastrar log:', error.response?.data || error.message);
        throw error;
    }
};

export const getLogs = async (filters: LogFilters = {}): Promise<LogResponse[]> => {
    try {
        const cleanFilters: Record<string, any> = {};
        Object.entries(filters).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                cleanFilters[key] = value;
            }
        });
        
        console.log('Enviando request para logs com filtros:', cleanFilters);
        console.log('URL que será chamada:', `${API_BASE_URL}/api/v1/logs?${new URLSearchParams(cleanFilters).toString()}`);
        
        const response = await api.get<LogResponse[]>('/logs', { params: cleanFilters });
        return response.data;
    } catch (error: any) {
        console.error('Erro ao obter logs:', error.response?.data || error.message);
        console.error('Status:', error.response?.status);
        console.error('Headers enviados:', error.config?.headers);
        throw error;
    }
};

export const logoutUser = (): void => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userRoles');
};

export default api;