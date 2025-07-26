# 🧪 Guia de Testes - LogScope API

Este guia fornece um passo a passo completo para testar todos os endpoints da LogScope API.

## 📋 Pré-requisitos

1. **Ferramentas necessárias:**
   - `curl` (linha de comando)
   - Postman, Insomnia ou similar
   - `jq` (para parsing JSON - opcional)

2. **Ambiente:**
   - LogScope API rodando em `http://localhost:8080`
   - MongoDB, Redis e RabbitMQ funcionando

3. **Variáveis de ambiente:**
```bash
export API_URL="http://localhost:8080"
export ADMIN_TOKEN=""  # Será preenchido após login
export SYSTEM_TOKEN="" # Será preenchido após obter token de sistema
```

## 🚀 Passo 1: Configuração Inicial

### 1.1 Verificar Status da API
```bash
curl -X GET "$API_URL/actuator/health"
```

**Resposta esperada:**
```json
{
  "status": "UP"
}
```

### 1.2 Registrar Primeira Empresa (Cliente)
```bash
curl -X POST "$API_URL/oauth/register-client" \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Empresa Teste",
    "contactEmail": "admin@teste.com",
    "initialAdminPassword": "senha123"
  }'
```

**Resposta esperada:**
```json
{
  "message": "Client registered successfully",
  "clientId": "12345678-1234-1234-1234-123456789012",
  "initialAdminUsername": "admin@teste.com",
  "jwtForNewClient": "eyJhbGciOiJIUzI1NiJ9..."
}
```

## 🔐 Passo 2: Autenticação

### 2.1 Login de Usuário Admin
```bash
ADMIN_LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/oauth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@teste.com",
    "password": "senha123"
  }')

echo $ADMIN_LOGIN_RESPONSE
export ADMIN_TOKEN=$(echo $ADMIN_LOGIN_RESPONSE | jq -r '.token')
echo "Admin Token: $ADMIN_TOKEN"
```

**Resposta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin@teste.com",
  "roles": ["ROLE_COMPANY_ADMIN"],
  "clientId": "12345678-1234-1234-1234-123456789012"
}
```

### 2.2 Obter Token de Sistema (Client Credentials)
```bash
SYSTEM_LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/oauth/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=SEU_CLIENT_ID&client_secret=SEU_CLIENT_SECRET")

echo $SYSTEM_LOGIN_RESPONSE
export SYSTEM_TOKEN=$(echo $SYSTEM_LOGIN_RESPONSE | jq -r '.token')
echo "System Token: $SYSTEM_TOKEN"
```

## 📝 Passo 3: Testando Endpoints de Logs

### 3.1 Enviar Log Simples
```bash
curl -X POST "$API_URL/api/v1/logs" \
  -H "Authorization: Bearer $SYSTEM_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "timestamp": "'$(date -Iseconds)'",
    "level": "INFO",
    "application": "test-app",
    "environment": "dev",
    "message": "Aplicação iniciada com sucesso",
    "hostname": "server-01",
    "metadata": {
      "version": "1.0.0",
      "startupTime": "2.5s"
    }
  }'
```

**Resposta esperada:** `201 Created`

### 3.2 Enviar Log de Erro
```bash
curl -X POST "$API_URL/api/v1/logs" \
  -H "Authorization: Bearer $SYSTEM_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "timestamp": "'$(date -Iseconds)'",
    "level": "ERROR",
    "application": "test-app",
    "environment": "prod",
    "message": "NullPointerException at UserController.java:42",
    "hostname": "server-01",
    "metadata": {
      "exception": "java.lang.NullPointerException",
      "userId": "user123",
      "requestId": "req-456"
    }
  }'
```

### 3.3 Enviar Múltiplos Logs (Script de Teste)
```bash
for i in {1..10}; do
  curl -X POST "$API_URL/api/v1/logs" \
    -H "Authorization: Bearer $SYSTEM_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"timestamp\": \"$(date -Iseconds)\",
      \"level\": \"$([ $((i % 3)) -eq 0 ] && echo 'ERROR' || echo 'INFO')\",
      \"application\": \"test-app\",
      \"environment\": \"prod\",
      \"message\": \"Log de teste número $i\",
      \"hostname\": \"server-0$((i % 3 + 1))\",
      \"metadata\": {
        \"logNumber\": $i,
        \"batch\": \"test-batch\"
      }
    }"
  echo "Log $i enviado"
  sleep 0.5
done
```

### 3.4 Consultar Logs
```bash
# Todos os logs
curl -X GET "$API_URL/api/v1/logs" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Logs com filtros
START_TIME=$(date -d '1 hour ago' -Iseconds)
END_TIME=$(date -Iseconds)

curl -X GET "$API_URL/api/v1/logs?level=ERROR&start=${START_TIME}&end=${END_TIME}&messageContains=Exception" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Logs por aplicação
curl -X GET "$API_URL/api/v1/logs/application/test-app" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## 🚨 Passo 4: Testando Regras de Alerta

### 4.1 Criar Regra de Alerta
```bash
RULE_RESPONSE=$(curl -s -X POST "$API_URL/api/v1/rules" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Erro Crítico NPE",
    "application": "test-app",
    "environment": "prod",
    "level": "ERROR",
    "matchPattern": "NullPointerException",
    "threshold": 3,
    "intervalSeconds": 300,
    "notificationChannels": ["email"],
    "enabled": true
  }')

echo $RULE_RESPONSE
export RULE_ID=$(echo $RULE_RESPONSE | jq -r '.id')
echo "Rule ID: $RULE_ID"
```

### 4.2 Listar Regras
```bash
curl -X GET "$API_URL/api/v1/rules" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### 4.3 Atualizar Regra
```bash
curl -X PUT "$API_URL/api/v1/rules/$RULE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Erro Crítico NPE - Atualizada",
    "application": "test-app",
    "environment": "prod",
    "level": "ERROR",
    "matchPattern": "NullPointerException",
    "threshold": 2,
    "intervalSeconds": 180,
    "notificationChannels": ["email", "telegram"],
    "enabled": true
  }'
```

### 4.4 Criar Mais Regras de Exemplo
```bash
# Regra para falhas de autenticação
curl -X POST "$API_URL/api/v1/rules" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Falhas de Autenticação",
    "application": "auth-service",
    "environment": "prod",
    "level": "WARN",
    "matchPattern": "authentication failed",
    "threshold": 5,
    "intervalSeconds": 120,
    "notificationChannels": ["email"],
    "enabled": true
  }'

# Regra para timeouts de database
curl -X POST "$API_URL/api/v1/rules" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Database Timeout",
    "application": "order-service",
    "environment": "prod",
    "level": "ERROR",
    "matchPattern": "timeout",
    "threshold": 1,
    "intervalSeconds": 60,
    "notificationChannels": ["email"],
    "enabled": true
  }'
```

## 🧪 Passo 5: Testando Alert Test

### 5.1 Testar Regra Existente
```bash
curl -X POST "$API_URL/api/v1/alerts/test" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "application": "test-app",
    "logSample": "java.lang.NullPointerException: Cannot invoke method getUser()",
    "level": "ERROR",
    "environment": "prod"
  }'
```

**Resposta esperada:**
```json
{
  "wouldTrigger": true,
  "matchedRule": "rule_12345",
  "allMatchedRules": ["rule_12345"]
}
```

### 5.2 Testar Log que NÃO Deve Disparar
```bash
curl -X POST "$API_URL/api/v1/alerts/test" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "application": "test-app",
    "logSample": "User logged in successfully",
    "level": "INFO",
    "environment": "prod"
  }'
```

**Resposta esperada:**
```json
{
  "wouldTrigger": false,
  "matchedRule": null,
  "allMatchedRules": []
}
```

### 5.3 Testar com Regex (se implementado)
```bash
curl -X POST "$API_URL/api/v1/alerts/test" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "application": "test-app",
    "logSample": "Database connection timeout after 30 seconds",
    "level": "ERROR",
    "environment": "prod"
  }'
```

## 📊 Passo 6: Testando Alertas Históricos

### 6.1 Disparar Alertas Automaticamente
```bash
# Enviar logs que devem disparar alertas
for i in {1..5}; do
  curl -X POST "$API_URL/api/v1/logs" \
    -H "Authorization: Bearer $SYSTEM_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"timestamp\": \"$(date -Iseconds)\",
      \"level\": \"ERROR\",
      \"application\": \"test-app\",
      \"environment\": \"prod\",
      \"message\": \"NullPointerException occurred in service method $i\",
      \"hostname\": \"server-01\",
      \"metadata\": {
        \"errorCount\": $i
      }
    }"
  echo "Error log $i sent"
  sleep 2
done
```

### 6.2 Verificar Histórico de Alertas
```bash
# Todos os alertas
curl -X GET "$API_URL/api/v1/alerts/history" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Alertas por período
START_TIME=$(date -d '1 hour ago' -Iseconds)
END_TIME=$(date -Iseconds)

curl -X GET "$API_URL/api/v1/alerts/history?start=${START_TIME}&end=${END_TIME}" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Alertas por regra específica
curl -X GET "$API_URL/api/v1/alerts/rule/$RULE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## 👥 Passo 7: Testando Gerenciamento de Usuários

### 7.1 Criar Novo Usuário na Empresa
```bash
curl -X POST "$API_URL/api/v1/company-admin/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "viewer@teste.com",
    "password": "senha123",
    "roles": ["COMPANY_VIEWER"]
  }'
```

### 7.2 Listar Usuários da Empresa
```bash
curl -X GET "$API_URL/api/v1/company-admin/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### 7.3 Testar Login do Novo Usuário
```bash
VIEWER_LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/oauth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "viewer@teste.com",
    "password": "senha123"
  }')

echo $VIEWER_LOGIN_RESPONSE
export VIEWER_TOKEN=$(echo $VIEWER_LOGIN_RESPONSE | jq -r '.token')
```

### 7.4 Testar Permissões do Viewer
```bash
# Deve funcionar (viewer pode ver logs)
curl -X GET "$API_URL/api/v1/logs" \
  -H "Authorization: Bearer $VIEWER_TOKEN"

# Deve falhar (viewer não pode criar regras)
curl -X POST "$API_URL/api/v1/rules" \
  -H "Authorization: Bearer $VIEWER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Teste",
    "application": "test",
    "level": "ERROR",
    "matchPattern": "test",
    "threshold": 1,
    "intervalSeconds": 60,
    "notificationChannels": ["email"],
    "enabled": true
  }'
```

## 🔧 Passo 8: Testando Endpoints de Administração

### 8.1 Métricas e Health Checks
```bash
# Health check detalhado
curl -X GET "$API_URL/actuator/health" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Métricas da aplicação
curl -X GET "$API_URL/actuator/metrics" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Informações da aplicação
curl -X GET "$API_URL/actuator/info" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## 🗑️ Passo 9: Testando Limpeza

### 9.1 Deletar Regra
```bash
curl -X DELETE "$API_URL/api/v1/rules/$RULE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### 9.2 Verificar se Foi Deletada
```bash
curl -X GET "$API_URL/api/v1/rules" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## 📝 Passo 10: Casos de Teste de Erro

### 10.1 Testar Validações
```bash
# Log sem campos obrigatórios
curl -X POST "$API_URL/api/v1/logs" \
  -H "Authorization: Bearer $SYSTEM_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "level": "",
    "message": ""
  }'
```

### 10.2 Testar Autenticação Inválida
```bash
# Token inválido
curl -X GET "$API_URL/api/v1/logs" \
  -H "Authorization: Bearer token-invalido"

# Sem token
curl -X GET "$API_URL/api/v1/logs"
```

### 10.3 Testar Permissões
```bash
# Viewer tentando criar regra (deve falhar)
curl -X POST "$API_URL/api/v1/rules" \
  -H "Authorization: Bearer $VIEWER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Teste Falha",
    "application": "test",
    "level": "ERROR",
    "matchPattern": "test",
    "threshold": 1,
    "intervalSeconds": 60,
    "notificationChannels": ["email"],
    "enabled": true
  }'
```

## 📊 Script de Teste Completo

Aqui está um script bash que executa todos os testes:

```bash
#!/bin/bash
# test-all-endpoints.sh

set -e

API_URL="http://localhost:8080"
COMPANY_NAME="Empresa Teste"
ADMIN_EMAIL="admin@teste.com"
ADMIN_PASSWORD="senha123"

echo "🚀 Iniciando testes completos da LogScope API..."

# 1. Health Check
echo "📋 1. Verificando status da API..."
curl -s "$API_URL/actuator/health" | jq '.'

# 2. Registrar empresa
echo "📝 2. Registrando empresa..."
REGISTER_RESPONSE=$(curl -s -X POST "$API_URL/oauth/register-client" \
  -H "Content-Type: application/json" \
  -d "{
    \"companyName\": \"$COMPANY_NAME\",
    \"contactEmail\": \"$ADMIN_EMAIL\",
    \"initialAdminPassword\": \"$ADMIN_PASSWORD\"
  }")

echo "Resposta do registro: $REGISTER_RESPONSE"

# 3. Login admin
echo "🔐 3. Fazendo login do admin..."
ADMIN_LOGIN=$(curl -s -X POST "$API_URL/oauth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$ADMIN_EMAIL\",
    \"password\": \"$ADMIN_PASSWORD\"
  }")

ADMIN_TOKEN=$(echo $ADMIN_LOGIN | jq -r '.token')
echo "Token obtido: ${ADMIN_TOKEN:0:50}..."

# 4. Criar regra de alerta
echo "📋 4. Criando regra de alerta..."
RULE_RESPONSE=$(curl -s -X POST "$API_URL/api/v1/rules" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Teste NPE",
    "application": "test-app",
    "environment": "prod",
    "level": "ERROR",
    "matchPattern": "NullPointerException",
    "threshold": 2,
    "intervalSeconds": 300,
    "notificationChannels": ["email"],
    "enabled": true
  }')

RULE_ID=$(echo $RULE_RESPONSE | jq -r '.id')
echo "Regra criada: $RULE_ID"

# 5. Testar alert test
echo "🧪 5. Testando alert test..."
TEST_RESPONSE=$(curl -s -X POST "$API_URL/api/v1/alerts/test" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "application": "test-app",
    "logSample": "java.lang.NullPointerException: test error",
    "level": "ERROR",
    "environment": "prod"
  }')

echo "Resultado do teste: $TEST_RESPONSE"

# 6. Enviar logs de teste
echo "📝 6. Enviando logs de teste..."
for i in {1..3}; do
  curl -s -X POST "$API_URL/api/v1/logs" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"timestamp\": \"$(date -Iseconds)\",
      \"level\": \"ERROR\",
      \"application\": \"test-app\",
      \"environment\": \"prod\",
      \"message\": \"NullPointerException occurred in test $i\",
      \"hostname\": \"server-01\"
    }"
  echo "Log $i enviado"
  sleep 1
done

# 7. Verificar logs
echo "📊 7. Verificando logs..."
LOGS_RESPONSE=$(curl -s -X GET "$API_URL/api/v1/logs" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

LOGS_COUNT=$(echo $LOGS_RESPONSE | jq '. | length')
echo "Total de logs encontrados: $LOGS_COUNT"

# 8. Verificar alertas
echo "🚨 8. Verificando alertas..."
sleep 5  # Aguardar processamento
ALERTS_RESPONSE=$(curl -s -X GET "$API_URL/api/v1/alerts/history" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

ALERTS_COUNT=$(echo $ALERTS_RESPONSE | jq '. | length')
echo "Total de alertas: $ALERTS_COUNT"

echo "✅ Testes completos finalizados!"
```

Para executar o script:

```bash
chmod +x test-all-endpoints.sh
./test-all-endpoints.sh
```

Este guia cobre todos os endpoints principais da LogScope API e fornece casos de teste abrangentes para validar toda a funcionalidade do sistema.
