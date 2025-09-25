import fr.marstech.mtlinkspray.repository.DashboardRepository
import fr.marstech.mtlinkspray.service.DashboardService
import fr.marstech.mtlinkspray.service.DashboardServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ServiceConfig {
    @Bean
    open fun dashboardService(dashboardRepository: DashboardRepository): DashboardService =
        DashboardServiceImpl(dashboardRepository)
}