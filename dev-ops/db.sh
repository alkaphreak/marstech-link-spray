#!/bin/bash
# Unified Database Management Script for Marstech Link Spray

if [[ "$(uname)" == "Darwin" ]]; then
    if ! pgrep -x "Docker\ Desktop" > /dev/null; then
        echo "Starting Docker Desktop..."
        open -a Docker
        sleep 5
    else
        echo "Docker Desktop is already running."
    fi
fi

ENV="$1"
CMD="$2"

if [[ "$ENV" != "dev" && "$ENV" != "test" ]]; then
    echo "Invalid environment: $ENV"
    echo "Usage: $0 {dev|test} {start|stop|restart|clean|logs|status|run-app}"
    exit 1
fi

project_root_dir=$(dirname "$(dirname "$(realpath "$0")")")
cd "$project_root_dir" || { echo "Failed to change directory to project root"; exit 1; }

if [[ "$ENV" == "dev" ]]; then
    docker_compose_file="docker-compose.dev.yml"
    db_url="mongodb://devuser:devpass@localhost:27019/link-spray-dev"
    db_service="marstech-mongodb-dev"
    profile="dev"
elif [[ "$ENV" == "test" ]]; then
    docker_compose_file="docker-compose.test.yml"
    db_url="mongodb://testuser:testpass@localhost:27018/link-spray-test"
    db_service="marstech-mongodb-test"
    profile="test"
fi

case "$CMD" in
    start)
        # Check if the db container is already running
        if docker compose -f $docker_compose_file ps | grep "$db_service" | grep "Up" > /dev/null; then
            echo "$ENV MongoDB database is already running."
        else
            echo "Starting $ENV MongoDB database..."
            docker compose -f $docker_compose_file up -d
            echo "Waiting for MongoDB to be ready..."
            sleep 5
            echo "$ENV database is ready at $db_url"
        fi
        ;;
    stop)
        echo "Stopping $ENV MongoDB database..."
        docker compose -f $docker_compose_file down
        ;;
    restart)
        echo "Restarting $ENV MongoDB database..."
        docker compose -f $docker_compose_file restart
        ;;
    clean)
        echo "Cleaning $ENV database (removing volumes)..."
        docker compose -f $docker_compose_file down -v
        ;;
    logs)
        docker compose -f $docker_compose_file logs -f $db_service
        ;;
    status)
        docker compose -f $docker_compose_file ps
        ;;
    run-app)
        echo "Starting application with $ENV profile..."
        mvn spring-boot:run -Dspring-boot.run.profiles=$profile
        ;;
    *)
        echo "Usage: $0 {dev|test} {start|stop|restart|clean|logs|status|run-app}"
        echo ""
        echo "Commands:"
        echo "  start    - Start the $ENV MongoDB database"
        echo "  stop     - Stop the $ENV MongoDB database"
        echo "  restart  - Restart the $ENV MongoDB database"
        echo "  clean    - Stop and remove database volumes"
        echo "  logs     - Show database logs"
        echo "  status   - Show container status"
        echo "  run-app  - Run application with $ENV profile"
        exit 1
        ;;
esac

exit 0