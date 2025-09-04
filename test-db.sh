#!/bin/bash

# Test Database Management Script for Marstech Link Spray

case "$1" in
    start)
        echo "Starting test MongoDB database..."
        docker-compose -f docker-compose.test.yml up -d
        echo "Waiting for MongoDB to be ready..."
        sleep 5
        echo "Test database is ready at mongodb://testuser:testpass@localhost:27018/link-spray-test"
        ;;
    stop)
        echo "Stopping test MongoDB database..."
        docker-compose -f docker-compose.test.yml down
        ;;
    restart)
        echo "Restarting test MongoDB database..."
        docker-compose -f docker-compose.test.yml restart
        ;;
    clean)
        echo "Cleaning test database (removing volumes)..."
        docker-compose -f docker-compose.test.yml down -v
        ;;
    logs)
        docker-compose -f docker-compose.test.yml logs -f mongodb-test
        ;;
    status)
        docker-compose -f docker-compose.test.yml ps
        ;;
    run-app)
        echo "Starting application with test profile..."
        mvn spring-boot:run -Dspring-boot.run.profiles=test
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|clean|logs|status|run-app}"
        echo ""
        echo "Commands:"
        echo "  start    - Start the test MongoDB database"
        echo "  stop     - Stop the test MongoDB database"
        echo "  restart  - Restart the test MongoDB database"
        echo "  clean    - Stop and remove database volumes"
        echo "  logs     - Show database logs"
        echo "  status   - Show container status"
        echo "  run-app  - Run application with test profile"
        exit 1
        ;;
esac