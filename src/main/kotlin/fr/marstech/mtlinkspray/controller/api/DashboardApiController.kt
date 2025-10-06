package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.DashboardDto
import fr.marstech.mtlinkspray.service.DashboardService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
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
        @PathVariable(name = "id") @NotBlank(message = "ID cannot be blank") id: String
    ): DashboardDto = dashboardService.getDashboard(id)

    @PostMapping
    fun createDashboard(
        @RequestBody @Valid dashboardDto: DashboardDto
    ): DashboardDto = dashboardService.createDashboard(dashboardDto)

    @PutMapping("/{id}")
    @Validated
    fun updateDashboard(
        @PathVariable(name = "id") @NotBlank(message = "ID cannot be blank") id: String,
        @RequestBody @Valid dashboardDto: DashboardDto
    ): DashboardDto = dashboardService.updateDashboard(id, dashboardDto)
}
