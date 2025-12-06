# HPMS API Examples

Quick reference for using the HPMS REST API.

## Base URL

```
http://localhost:8080/api
```

## Authentication

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "admin",
  "role": "ROLE_ADMIN",
  "message": "Authentication successful"
}
```

### Using the Token

Include the JWT token in all subsequent requests:

```bash
curl -X GET http://localhost:8080/api/patients \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## API Endpoints

### Health Check

```bash
# Check if the service is running
curl http://localhost:8080/api/auth/health
```

### Patients

#### Get All Patients (ADMIN, DOCTOR, STAFF)

```bash
curl http://localhost:8080/api/patients \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Get Patient by ID

```bash
curl http://localhost:8080/api/patients/pat-123 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Create Patient (ADMIN, STAFF)

```bash
curl -X POST http://localhost:8080/api/patients \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-15",
    "gender": "Male",
    "phone": "5551234567",
    "email": "john.doe@example.com",
    "address": "123 Main St, City, State"
  }'
```

#### Search Patients

```bash
curl "http://localhost:8080/api/patients/search?lastName=Doe" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Appointments

#### Get All Appointments (ADMIN, DOCTOR, STAFF)

```bash
curl http://localhost:8080/api/appointments \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Create Appointment

```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "pat-123",
    "staffId": "doc-456",
    "scheduledAt": "2024-12-15T10:00:00",
    "reason": "Annual checkup"
  }'
```

#### Cancel Appointment

```bash
curl -X PATCH http://localhost:8080/api/appointments/appt-123/cancel \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Complete Appointment (ADMIN, DOCTOR, STAFF)

```bash
curl -X PATCH http://localhost:8080/api/appointments/appt-123/complete \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Get Appointments by Status

```bash
curl http://localhost:8080/api/appointments/status/SCHEDULED \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Error Responses

### 401 Unauthorized

```json
{
  "timestamp": "2024-12-06T06:45:48.767887211",
  "status": 401,
  "error": "Authentication Failed",
  "message": "Invalid username or password",
  "path": "/api/auth/login"
}
```

### 403 Forbidden

```json
{
  "timestamp": "2024-12-06T06:45:48.767887211",
  "status": 403,
  "error": "Access Denied",
  "message": "You don't have permission to access this resource",
  "path": "/api/admin/users"
}
```

### 404 Not Found

```json
{
  "timestamp": "2024-12-06T06:45:48.767887211",
  "status": 404,
  "error": "Resource Not Found",
  "message": "Patient not found with id : 'invalid-id'",
  "path": "/api/patients/invalid-id"
}
```

### 400 Bad Request (Validation Error)

```json
{
  "timestamp": 1733472348767,
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "firstName": "First name is required",
    "email": "Email should be valid"
  },
  "path": "/api/patients"
}
```

## Testing with cURL

### Save Token to Variable

```bash
# Login and save token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | \
  jq -r '.token')

# Use saved token
curl http://localhost:8080/api/patients \
  -H "Authorization: Bearer $TOKEN"
```

## Testing with Postman

1. Import the API into Postman using Swagger:
   - Go to `http://localhost:8080/swagger-ui.html`
   - Export OpenAPI spec
   - Import into Postman

2. Set up environment variables:
   - `BASE_URL`: `http://localhost:8080/api`
   - `TOKEN`: (will be set after login)

3. Create a login request and use Tests to save token:
```javascript
pm.test("Login successful", function () {
    var jsonData = pm.response.json();
    pm.environment.set("TOKEN", jsonData.token);
});
```

4. Use `{{TOKEN}}` in Authorization header for other requests

## Default Users for Testing

| Username | Password   | Role    | Use Case                      |
|----------|------------|---------|-------------------------------|
| admin    | admin123   | ADMIN   | Full system access            |
| doctor   | doctor123  | DOCTOR  | Medical staff operations      |
| staff    | staff123   | STAFF   | Administrative operations     |
| patient  | patient123 | PATIENT | Patient self-service features |

## Rate Limiting (Future Enhancement)

Currently not implemented, but recommended for production:
- Login endpoint: 5 requests per minute
- Other endpoints: 100 requests per minute

## API Documentation

Interactive API documentation is available at:
```
http://localhost:8080/swagger-ui.html
```

Try out endpoints directly from the Swagger UI!
