# Test Database Setup

This document describes how to set up and use the test MongoDB database for Marstech Link Spray.

## Quick Start

```bash
# Start test database
./test-db.sh start

# Run application with test profile
./test-db.sh run-app

# Stop test database
./test-db.sh stop
```

## Database Details

- **Host**: localhost
- **Port**: 27018 (different from production port 27017)
- **Database**: link-spray-test
- **Username**: testuser
- **Password**: testpass
- **Connection URI**: `mongodb://testuser:testpass@localhost:27018/link-spray-test`

## Available Commands

| Command                | Description                                    |
|------------------------|------------------------------------------------|
| `./test-db.sh start`   | Start the test MongoDB database                |
| `./test-db.sh stop`    | Stop the test MongoDB database                 |
| `./test-db.sh restart` | Restart the test MongoDB database              |
| `./test-db.sh clean`   | Stop and remove database volumes (fresh start) |
| `./test-db.sh logs`    | Show database logs                             |
| `./test-db.sh status`  | Show container status                          |
| `./test-db.sh run-app` | Run application with test profile              |

## Manual Application Start

If you prefer to start the application manually with the test database:

```bash
# Start test database first
./test-db.sh start

# Run with test profile
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

## Collections

The test database includes these pre-created collections:

- `linkItem` - Stores shortened links and spray configurations
- `abuseReport` - Stores abuse reports
- `dashboardEntity` - Dashboard data
- `mtLinkSprayCollectionItem` - Collection items

## Environment Variables

The test configuration uses these settings:

- Server runs on port **8097** (different from production port 8096)
- MongoDB connection configured for test database
- Mail settings disabled for testing

## Docker Compose

The test database uses `docker-compose.test.yml` which:

- Runs MongoDB 8 in a container
- Exposes port 27018
- Creates persistent volume for data
- Initializes database with required collections
- Uses authentication (testuser/testpass)

## Cleanup

To completely remove the test database and start fresh:

```bash
./test-db.sh clean
```

This will stop the container and remove all data volumes.