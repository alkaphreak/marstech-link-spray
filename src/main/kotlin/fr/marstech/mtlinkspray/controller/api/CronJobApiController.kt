package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.CronJobDto
import fr.marstech.mtlinkspray.dto.CronJobRequest
import fr.marstech.mtlinkspray.dto.CronRunDto
import fr.marstech.mtlinkspray.service.CronJobService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/** Cron jobs API (MLS-220): jobs belong to the namespace of the `X-Api-Key` that manages them. */
@Validated
@RestController
@RequestMapping("/api/cron/jobs")
class CronJobApiController(private val cronJobService: CronJobService) {

    @PostMapping
    fun createJob(
        @RequestHeader("X-Api-Key", required = false) apiKey: String?,
        @Valid @RequestBody request: CronJobRequest,
    ): ResponseEntity<CronJobDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(toDto(cronJobService.create(apiKey, request)))

    @GetMapping
    fun listJobs(@RequestHeader("X-Api-Key", required = false) apiKey: String?): List<CronJobDto> =
        cronJobService.list(apiKey).map(::toDto)

    @GetMapping("/{id}")
    fun getJob(@RequestHeader("X-Api-Key", required = false) apiKey: String?, @PathVariable id: String): CronJobDto =
        toDto(cronJobService.get(apiKey, id))

    @PutMapping("/{id}")
    fun updateJob(
        @RequestHeader("X-Api-Key", required = false) apiKey: String?,
        @PathVariable id: String,
        @Valid @RequestBody request: CronJobRequest,
    ): CronJobDto = toDto(cronJobService.update(apiKey, id, request))

    @DeleteMapping("/{id}")
    fun deleteJob(@RequestHeader("X-Api-Key", required = false) apiKey: String?, @PathVariable id: String): ResponseEntity<Void> {
        cronJobService.delete(apiKey, id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/runs")
    fun listRuns(
        @RequestHeader("X-Api-Key", required = false) apiKey: String?,
        @PathVariable id: String,
        @RequestParam(defaultValue = "50") @Min(1) @Max(500) limit: Int,
    ): List<CronRunDto> = cronJobService.runs(apiKey, id, limit).map(CronRunDto::fromEntity)

    private fun toDto(job: fr.marstech.mtlinkspray.entity.CronJobEntity) = CronJobDto.fromEntity(job, cronJobService.nextRunAt(job))
}
