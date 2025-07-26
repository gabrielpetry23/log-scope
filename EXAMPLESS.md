# LogScope API - Exemplos de Uso

Este documento contém exemplos práticos de como integrar e usar a LogScope API.

## 🚀 Integração com Aplicações

### Java Spring Boot

```java
@Component
@Slf4j
public class LogScopeClient {
    
    private final RestTemplate restTemplate;
    private final String logScopeUrl;
    private final String apiToken;
    
    public LogScopeClient() {
        this.restTemplate = new RestTemplate();
        this.logScopeUrl = "http://localhost:8080/api/v1/logs";
        this.apiToken = "your-jwt-token";
    }
    
    public void sendLog(String level, String message, Map<String, Object> metadata) {
        LogRequest request = LogRequest.builder()
            .timestamp(LocalDateTime.now())
            .level(level)
            .application("my-spring-app")
            .environment("prod")
            .message(message)
            .hostname(getHostname())
            .metadata(metadata)
            .build();
            
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);
        
        HttpEntity<LogRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            restTemplate.postForEntity(logScopeUrl, entity, Void.class);
        } catch (Exception e) {
            log.error("Failed to send log to LogScope", e);
        }
    }
    
    // Método para capturar exceptions automaticamente
    @EventListener
    public void handleException(ExceptionEvent event) {
        Map<String, Object> metadata = Map.of(
            "exception", event.getException().getClass().getSimpleName(),
            "stackTrace", getStackTrace(event.getException()),
            "userId", getCurrentUserId(),
            "sessionId", getCurrentSessionId()
        );
        
        sendLog("ERROR", event.getException().getMessage(), metadata);
    }
}

@Data
@Builder
class LogRequest {
    private LocalDateTime timestamp;
    private String level;
    private String application;
    private String environment;
    private String message;
    private String hostname;
    private Map<String, Object> metadata;
}
```

### Node.js/Express

```javascript
const axios = require('axios');
const os = require('os');

class LogScopeClient {
    constructor(apiUrl = 'http://localhost:8080/api/v1/logs', token) {
        this.apiUrl = apiUrl;
        this.token = token;
        this.application = process.env.APP_NAME || 'nodejs-app';
        this.environment = process.env.NODE_ENV || 'development';
    }

    async sendLog(level, message, metadata = {}) {
        const logData = {
            timestamp: new Date().toISOString(),
            level: level.toUpperCase(),
            application: this.application,
            environment: this.environment,
            message: message,
            hostname: os.hostname(),
            metadata: {
                ...metadata,
                pid: process.pid,
                memory: process.memoryUsage(),
                uptime: process.uptime()
            }
        };

        try {
            await axios.post(this.apiUrl, logData, {
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });
        } catch (error) {
            console.error('Failed to send log to LogScope:', error.message);
        }
    }

    // Middleware para Express
    errorMiddleware() {
        return (err, req, res, next) => {
            const metadata = {
                method: req.method,
                url: req.url,
                userAgent: req.get('User-Agent'),
                ip: req.ip,
                userId: req.user?.id,
                headers: req.headers
            };

            this.sendLog('ERROR', err.message, metadata);
            next(err);
        };
    }

    // Logger customizado
    createLogger() {
        return {
            info: (message, meta) => this.sendLog('INFO', message, meta),
            warn: (message, meta) => this.sendLog('WARN', message, meta),
            error: (message, meta) => this.sendLog('ERROR', message, meta),
            debug: (message, meta) => this.sendLog('DEBUG', message, meta)
        };
    }
}

// Uso
const logScope = new LogScopeClient('http://localhost:8080/api/v1/logs', 'your-token');
const logger = logScope.createLogger();

// Em uma rota
app.get('/users/:id', async (req, res) => {
    try {
        const user = await getUserById(req.params.id);
        logger.info('User retrieved successfully', { userId: req.params.id });
        res.json(user);
    } catch (error) {
        logger.error('Failed to retrieve user', { 
            userId: req.params.id, 
            error: error.message 
        });
        res.status(500).json({ error: 'Internal server error' });
    }
});

// Middleware de erro
app.use(logScope.errorMiddleware());
```

### Python/FastAPI

```python
import asyncio
import aiohttp
import json
import socket
import os
import traceback
from datetime import datetime
from typing import Dict, Any, Optional
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

class LogScopeClient:
    def __init__(self, api_url: str = "http://localhost:8080/api/v1/logs", token: str = None):
        self.api_url = api_url
        self.token = token
        self.application = os.getenv("APP_NAME", "python-app")
        self.environment = os.getenv("ENV", "development")
        self.hostname = socket.gethostname()

    async def send_log(self, level: str, message: str, metadata: Dict[str, Any] = None):
        if metadata is None:
            metadata = {}

        log_data = {
            "timestamp": datetime.now().isoformat(),
            "level": level.upper(),
            "application": self.application,
            "environment": self.environment,
            "message": message,
            "hostname": self.hostname,
            "metadata": metadata
        }

        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }

        try:
            async with aiohttp.ClientSession() as session:
                async with session.post(
                    self.api_url, 
                    json=log_data, 
                    headers=headers
                ) as response:
                    if response.status != 201:
                        print(f"Failed to send log: {response.status}")
        except Exception as e:
            print(f"Error sending log to LogScope: {e}")

    async def log_exception(self, exc: Exception, context: Dict[str, Any] = None):
        if context is None:
            context = {}

        metadata = {
            **context,
            "exception_type": type(exc).__name__,
            "traceback": traceback.format_exc()
        }

        await self.send_log("ERROR", str(exc), metadata)

# Inicialização
log_client = LogScopeClient(token="your-token")

# FastAPI App
app = FastAPI()

# Middleware para logging automático
@app.middleware("http")
async def log_requests(request, call_next):
    start_time = datetime.now()
    
    try:
        response = await call_next(request)
        duration = (datetime.now() - start_time).total_seconds()
        
        await log_client.send_log("INFO", f"{request.method} {request.url.path}", {
            "method": request.method,
            "path": request.url.path,
            "status_code": response.status_code,
            "duration_seconds": duration,
            "client_ip": request.client.host
        })
        
        return response
    except Exception as e:
        await log_client.log_exception(e, {
            "method": request.method,
            "path": request.url.path,
            "client_ip": request.client.host
        })
        raise

# Exemplo de endpoint
@app.get("/users/{user_id}")
async def get_user(user_id: int):
    try:
        # Simular busca de usuário
        if user_id == 999:
            raise ValueError("User not found")
        
        await log_client.send_log("INFO", "User retrieved", {"user_id": user_id})
        return {"id": user_id, "name": "John Doe"}
    
    except ValueError as e:
        await log_client.log_exception(e, {"user_id": user_id})
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        await log_client.log_exception(e, {"user_id": user_id})
        raise HTTPException(status_code=500, detail="Internal server error")
```

## 📊 Scripts de Administração

### Script de Configuração Inicial

```bash
#!/bin/bash
# setup-logscope.sh

set -e

LOGSCOPE_URL="http://localhost:8080"
COMPANY_NAME="Minha Empresa"
ADMIN_EMAIL="admin@empresa.com"
ADMIN_PASSWORD="senha123"

echo "🚀 Configurando LogScope API..."

# Registrar empresa e criar admin
echo "📝 Registrando empresa..."
REGISTER_RESPONSE=$(curl -s -X POST "${LOGSCOPE_URL}/oauth/register-client" \
  -H "Content-Type: application/json" \
  -d "{
    \"companyName\": \"${COMPANY_NAME}\",
    \"contactEmail\": \"${ADMIN_EMAIL}\",
    \"initialAdminPassword\": \"${ADMIN_PASSWORD}\"
  }")

echo "Resposta do registro: $REGISTER_RESPONSE"

# Fazer login e obter token
echo "🔐 Fazendo login..."
LOGIN_RESPONSE=$(curl -s -X POST "${LOGSCOPE_URL}/oauth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"${ADMIN_EMAIL}\",
    \"password\": \"${ADMIN_PASSWORD}\"
  }")

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo "❌ Erro ao obter token de autenticação"
  exit 1
fi

echo "✅ Token obtido com sucesso"

# Criar regras de exemplo
echo "📋 Criando regras de alerta..."

# Regra para erros críticos
curl -s -X POST "${LOGSCOPE_URL}/api/v1/rules" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Erros Críticos",
    "application": "any",
    "level": "ERROR",
    "matchPattern": "Exception",
    "threshold": 5,
    "intervalSeconds": 300,
    "notificationChannels": ["email"],
    "enabled": true
  }' > /dev/null

echo "✅ Regra 'Erros Críticos' criada"

# Regra para falhas de autenticação
curl -s -X POST "${LOGSCOPE_URL}/api/v1/rules" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Falhas de Autenticação",
    "application": "auth-service",
    "level": "WARN",
    "matchPattern": "authentication failed",
    "threshold": 10,
    "intervalSeconds": 180,
    "notificationChannels": ["email"],
    "enabled": true
  }' > /dev/null

echo "✅ Regra 'Falhas de Autenticação' criada"

echo "🎉 Configuração concluída com sucesso!"
echo "🌐 LogScope API: ${LOGSCOPE_URL}"
echo "👤 Admin: ${ADMIN_EMAIL}"
echo "🔑 Token: ${TOKEN}"
```

### Script de Geração de Logs de Teste

```python
#!/usr/bin/env python3
# generate-test-logs.py

import asyncio
import aiohttp
import random
import json
from datetime import datetime, timedelta
from typing import List

class LogGenerator:
    def __init__(self, api_url: str, token: str):
        self.api_url = f"{api_url}/api/v1/logs"
        self.token = token
        
    async def generate_logs(self, count: int = 100):
        applications = ["user-service", "order-service", "payment-service", "auth-service"]
        levels = ["INFO", "WARN", "ERROR", "DEBUG"]
        environments = ["dev", "staging", "prod"]
        
        messages = {
            "INFO": [
                "User logged in successfully",
                "Order processed",
                "Payment completed",
                "Cache hit for user data"
            ],
            "WARN": [
                "Slow database query detected",
                "Memory usage above 80%",
                "Connection pool near capacity",
                "Rate limit approaching"
            ],
            "ERROR": [
                "NullPointerException in UserController",
                "Database connection failed",
                "Payment processing timeout",
                "Authentication service unavailable"
            ],
            "DEBUG": [
                "Processing request for user",
                "Executing SQL query",
                "Cache miss for key",
                "Validation completed"
            ]
        }
        
        async with aiohttp.ClientSession() as session:
            tasks = []
            for i in range(count):
                task = self.send_random_log(session, applications, levels, environments, messages)
                tasks.append(task)
                
                # Send in batches to avoid overwhelming the server
                if len(tasks) >= 10:
                    await asyncio.gather(*tasks)
                    tasks = []
                    await asyncio.sleep(0.1)
            
            # Send remaining logs
            if tasks:
                await asyncio.gather(*tasks)
    
    async def send_random_log(self, session, applications, levels, environments, messages):
        level = random.choice(levels)
        application = random.choice(applications)
        environment = random.choice(environments)
        message = random.choice(messages[level])
        
        # Simulate some time distribution
        timestamp = datetime.now() - timedelta(
            seconds=random.randint(0, 3600),
            microseconds=random.randint(0, 999999)
        )
        
        log_data = {
            "timestamp": timestamp.isoformat(),
            "level": level,
            "application": application,
            "environment": environment,
            "message": message,
            "hostname": f"server-{random.randint(1, 10):02d}",
            "metadata": {
                "requestId": f"req-{random.randint(1000, 9999)}",
                "userId": f"user-{random.randint(1, 1000)}",
                "executionTime": random.randint(10, 5000),
                "memoryUsage": random.randint(100, 1000)
            }
        }
        
        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }
        
        try:
            async with session.post(self.api_url, json=log_data, headers=headers) as response:
                if response.status == 201:
                    print(f"✅ Log sent: {level} - {message[:50]}...")
                else:
                    print(f"❌ Failed to send log: {response.status}")
        except Exception as e:
            print(f"❌ Error: {e}")

async def main():
    import sys
    
    if len(sys.argv) != 3:
        print("Usage: python generate-test-logs.py <API_URL> <TOKEN>")
        print("Example: python generate-test-logs.py http://localhost:8080 your-jwt-token")
        sys.exit(1)
    
    api_url = sys.argv[1]
    token = sys.argv[2]
    
    generator = LogGenerator(api_url, token)
    
    print("🚀 Generating test logs...")
    await generator.generate_logs(50)
    print("✅ Test logs generation completed!")

if __name__ == "__main__":
    asyncio.run(main())
```

### Monitoramento com Script Shell

```bash
#!/bin/bash
# monitor-logscope.sh

LOGSCOPE_URL="http://localhost:8080"
TOKEN="your-jwt-token"

echo "📊 LogScope Monitoring Dashboard"
echo "================================"

# Health check
echo "🏥 Health Status:"
HEALTH=$(curl -s "${LOGSCOPE_URL}/actuator/health" | jq -r '.status')
echo "   Status: $HEALTH"

# Recent logs count
echo -e "\n📝 Recent Logs (last hour):"
START_TIME=$(date -d '1 hour ago' -Iseconds)
END_TIME=$(date -Iseconds)

LOGS_RESPONSE=$(curl -s -G "${LOGSCOPE_URL}/api/v1/logs" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d "start=${START_TIME}" \
  -d "end=${END_TIME}")

LOGS_COUNT=$(echo "$LOGS_RESPONSE" | jq '. | length')
echo "   Total logs: $LOGS_COUNT"

# Count by level
ERROR_COUNT=$(echo "$LOGS_RESPONSE" | jq '[.[] | select(.level == "ERROR")] | length')
WARN_COUNT=$(echo "$LOGS_RESPONSE" | jq '[.[] | select(.level == "WARN")] | length')
INFO_COUNT=$(echo "$LOGS_RESPONSE" | jq '[.[] | select(.level == "INFO")] | length')

echo "   ERROR: $ERROR_COUNT"
echo "   WARN:  $WARN_COUNT"
echo "   INFO:  $INFO_COUNT"

# Recent alerts
echo -e "\n🚨 Recent Alerts (last hour):"
ALERTS_RESPONSE=$(curl -s -G "${LOGSCOPE_URL}/api/v1/alerts/history" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d "start=${START_TIME}" \
  -d "end=${END_TIME}")

ALERTS_COUNT=$(echo "$ALERTS_RESPONSE" | jq '. | length')
echo "   Total alerts: $ALERTS_COUNT"

if [ "$ALERTS_COUNT" -gt 0 ]; then
    echo "   Recent alerts:"
    echo "$ALERTS_RESPONSE" | jq -r '.[] | "   - \(.message)"'
fi

echo -e "\n✅ Monitoring completed"
```

## 🔧 Utilitários

### Client HTTP Simples

```bash
#!/bin/bash
# logscope-client.sh

LOGSCOPE_URL="${LOGSCOPE_URL:-http://localhost:8080}"
TOKEN="${LOGSCOPE_TOKEN:-}"

if [ -z "$TOKEN" ]; then
    echo "❌ Erro: Defina a variável LOGSCOPE_TOKEN"
    exit 1
fi

send_log() {
    local level="$1"
    local application="$2"
    local message="$3"
    local environment="${4:-dev}"
    
    curl -X POST "${LOGSCOPE_URL}/api/v1/logs" \
        -H "Authorization: Bearer ${TOKEN}" \
        -H "Content-Type: application/json" \
        -d "{
            \"timestamp\": \"$(date -Iseconds)\",
            \"level\": \"${level}\",
            \"application\": \"${application}\",
            \"environment\": \"${environment}\",
            \"message\": \"${message}\",
            \"hostname\": \"$(hostname)\"
        }"
}

# Uso: ./logscope-client.sh INFO my-app "Application started" prod
send_log "$@"
```

Estes exemplos mostram como integrar facilmente a LogScope API em diferentes tecnologias e cenários de uso.
