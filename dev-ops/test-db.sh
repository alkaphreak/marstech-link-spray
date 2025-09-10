# Test Database Management Script for Marstech Link Spray

project_root_dir=$(dirname "$(dirname "$(realpath "$0")")")
cd "$project_root_dir" || { echo "Failed to change directory to project root"; exit 1; }
docker_compose_file="docker-compose.test.yml"

case "$1" in
    start)
        echo "Starting test MongoDB database..."
        docker compose -f $docker_compose_file up -d
        echo "Waiting for MongoDB to be ready..."
        sleep 5
        echo "Test database is ready at mongodb://testuser:testpass@localhost:27018/link-spray-test"
        ;;
    stop)
        echo "Stopping test MongoDB database..."
        docker compose -f $docker_compose_file down
        ;;
    restart)
        echo "Restarting test MongoDB database..."
        docker compose -f $docker_compose_file restart
        ;;
    clean)
        echo "Cleaning test database (removing volumes)..."
        docker compose -f $docker_compose_file down -v
        ;;
    logs)
        docker compose -f $docker_compose_file logs -f marstech-mongodb-test
        ;;
    status)
        docker compose -f $docker_compose_file ps
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