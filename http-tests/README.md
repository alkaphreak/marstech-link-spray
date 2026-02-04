# HTTP API Tests

This directory contains comprehensive HTTP tests for the Marstech Link Spray application.

## Test Files

### Core API Tests

- **dashboard-api.http** – Dashboard CRUD operations
- **paste-api.http** – Pastebin functionality tests
- **shortener-api.http** – URL shortening service tests
- **spray-api.http** - Link spray functionality tests
- **utility-api.http** – UUID generation and utility endpoints
- **random-api.http** - Random number generator API tests

### Additional Test Categories

- **error-handling.http** - Error scenarios and edge cases
- **view-endpoints.http** - HTML view rendering tests
- **security-tests.http** – Security validation and penetration tests

## Running Tests

### Prerequisites

1. Start the application: `mvn spring-boot:run`
2. Ensure MongoDB is running and configured
3. Application should be accessible at `http://localhost:8096`

### Using IntelliJ IDEA / VS Code

1. Open any `.http` file
2. Click the green arrow next to each request
3. View responses in the integrated HTTP client

### Using curl (alternative)

Convert HTTP requests to curl commands for command-line testing.

## Test Coverage

### API Endpoints Tested

- ✅ GET `/api/dashboard/{id}`
- ✅ POST `/api/dashboard`
- ✅ PUT `/api/dashboard/{id}`
- ✅ POST `/api/paste`
- ✅ GET `/api/paste/{id}`
- ✅ GET `/api/url-shortener/shorten`
- ✅ GET `/api/spray`
- ✅ GET `/api/uuid`
- ✅ GET `/api/random` - random number generator endpoint

### View Endpoints Tested

- ✅ GET `/` (home)
- ✅ GET `/spray`
- ✅ POST `/spray`
- ✅ GET `/paste`
- ✅ POST `/paste`
- ✅ GET `/shortener`
- ✅ GET `/uuid`
- ✅ GET `/dashboard`

### Error Scenarios Tested

- ✅ 404 Not Found
- ✅ 400 Bad Request
- ✅ 405 Method Not Allowed
- ✅ 415 Unsupported Media Type
- ✅ Validation errors
- ✅ Invalid JSON payloads
- ✅ Missing required parameters

### Security Tests

- ✅ XSS prevention
- ✅ SQL injection attempts
- ✅ Path traversal attempts
- ✅ Large payload handling
- ✅ Unicode/UTF-8 support
- ✅ Header injection attempts

## Expected Responses

### Success Cases

- Dashboard operations: 200 OK with JSON response
- Paste operations: 200 OK with JSON response
- URL shortening: 200 OK with shortened URL string
- Spray operations: 200 OK with spray URL string
- UUID generation: 200 OK with UUID string
- Random number generation: 200 OK with JSON response containing `value`, `min`, `max`, and `timestamp`

### Error Cases

- Invalid requests: 400 Bad Request with JSON `{"error": "message"}`
- Not found: 404 Not Found with JSON `{"error": "message"}`
- Method not allowed: 405 Method Not Allowed
- Unsupported Media Type: 415 Unsupported Media Type
- Server errors: 500 Internal Server Error with JSON `{"error": "message"}`

## Notes

- All tests use `@baseUrl = http://localhost:8096`
- Tests include both positive and negative scenarios
- Security tests validate input sanitization
- View tests ensure proper HTML rendering
- Error tests verify proper error handling and responses
- All API error responses return JSON with `{"error": "message"}` format

## Random API - quick examples

The `random-api.http` contains the main smoke tests for the Random Number API. Example curl commands:

```bash
# valid: generate between 10 and 20
curl -sS 'http://localhost:8096/api/random?min=10&max=20' -H 'Accept: application/json'

# default min: min omitted -> defaults to 0
curl -sS 'http://localhost:8096/api/random?max=50' -H 'Accept: application/json'

# missing max: should return 400
curl -i 'http://localhost:8096/api/random' -H 'Accept: application/json'

# min > max: should return 400
curl -i 'http://localhost:8096/api/random?min=100&max=10' -H 'Accept: application/json'
```
