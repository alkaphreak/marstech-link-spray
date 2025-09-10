#  Dev Database Management Script for Marstech Link Spray
project_root_dir=$(dirname "$(dirname "$(realpath "$0")")")
# shellcheck disable=SC2164
cd "$project_root_dir"
docker_compose_file="docker-compose.dev.yml"

case "$1" in
    start)
        echo "Starting dev MongoDB database..."
        docker compose -f $docker_compose_file up -d
        echo "Waiting for MongoDB to be ready..."
        sleep 5
        echo "Dev database is ready at mongodb://devuser:devpass@localhost:27019/link-spray-dev"
        ;;
    stop)
        echo "Stopping dev MongoDB database..."
        docker compose -f $docker_compose_file down
        ;;
    restart)
        echo "Restarting dev MongoDB database..."
        docker compose -f $docker_compose_file restart
        ;;
    clean)
        echo "Cleaning dev database (removing volumes)..."
        docker compose -f $docker_compose_file down -v
        ;;
    logs)
        docker compose -f $docker_compose_file logs -f marstech-mongodb-dev
        ;;
    status)
        docker compose -f $docker_compose_file ps
        ;;
    run-app)
        echo "Starting application with dev profile..."
        mvn spring-boot:run -Dspring-boot.run.profiles=dev
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|clean|logs|status|run-app}"
        echo ""
        echo "Commands:"
        echo "  start    - Start the dev MongoDB database"
        echo "  stop     - Stop the dev MongoDB database"
        echo "  restart  - Restart the dev MongoDB database"
        echo "  clean    - Stop and remove database volumes"
        echo "  logs     - Show database logs"
        echo "  status   - Show container status"
        echo "  run-app  - Run application with dev profile"
        exit 1
        ;;
esac

exit 0;