package fr.marstech.mtlinkspray.adapter.inbound.web.dashboard
import fr.marstech.mtlinkspray.adapter.inbound.web.common.API_KEY_HEADER
import fr.marstech.mtlinkspray.domain.dashboard.DashboardService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/dashboard")
class DashboardApiController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/{id}")
    @Validated
    fun getDashboard(
        @PathVariable @NotBlank(message = "ID cannot be blank") id: String
    ): DashboardDto = DashboardDto.from(dashboardService.getDashboard(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDashboard(
        @RequestBody @Valid dashboardDto: DashboardDto,
        @RequestHeader(API_KEY_HEADER, required = false) apiKey: String?,
    ): DashboardDto = DashboardDto.from(dashboardService.createDashboard(dashboardDto.toDashboard(), apiKey))

    @PutMapping("/{id}")
    @Validated
    fun updateDashboard(
        @PathVariable @NotBlank(message = "ID cannot be blank") id: String,
        @RequestBody @Valid dashboardDto: DashboardDto,
        @RequestHeader(API_KEY_HEADER, required = false) apiKey: String?,
    ): DashboardDto = DashboardDto.from(dashboardService.updateDashboard(id, dashboardDto.toDashboard(), apiKey))
}
