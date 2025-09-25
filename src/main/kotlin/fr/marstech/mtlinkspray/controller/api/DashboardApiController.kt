package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.DashboardDto
import fr.marstech.mtlinkspray.service.DashboardService
import jakarta.validation.constraints.NotBlank
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/dashboard")
open class DashboardApiController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/{id}")
    fun getDashboard(
        @PathVariable(name = "id") @NotBlank id: String
    ): DashboardDto = dashboardService.getDashboard(id)

    @PostMapping
    fun createDashboard(
        dashboardDto: DashboardDto
    ): DashboardDto = dashboardService.createDashboard(dashboardDto)

    @PutMapping("/{id}")
    fun updateDashboard(
        @PathVariable(name = "id") @NotBlank id: String,
        @RequestBody dashboardDto: DashboardDto
    ): DashboardDto = dashboardService.updateDashboard(id, dashboardDto)
}
