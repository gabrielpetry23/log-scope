export interface LogRequest {
    message: string;
    level: string;
    source: string;
}

export interface LogResponse {
    id: string;
    message: string;
    level: string;
    source: string;
    timestamp: string;
}

export interface AuthResult {
    success: boolean;
    token?: string;
    roles?: string[];
    message?: string;
}

export interface LogFilters {
    level?: string;
    start?: string;
    end?: string;
    messageContains?: string;
}

export type UserRole = 'COMPANY_SYSTEM' | 'COMPANY_ADMIN' | 'GLOBAL_ADMIN' | 'COMPANY_VIEWER' | 'GLOBAL_SUPPORT';