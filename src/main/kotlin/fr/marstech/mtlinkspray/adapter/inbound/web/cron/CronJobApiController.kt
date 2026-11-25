package fr.marstech.mtlinkspray.adapter.inbound.web.cron
import fr.marstech.mtlinkspray.adapter.inbound.web.common.API_KEY_HEADER
import fr.marstech.mtlinkspray.domain.cron.CronJob
import fr.marstech.mtlinkspray.domain.cron.CronJobService
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
        @RequestHeader(API_KEY_HEADER, required = false) apiKey: String?,
        @Valid @RequestBody request: CronJobRequest,
    ): ResponseEntity<CronJobDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(toDto(cronJobService.create(apiKey, request.toDraft())))

    @GetMapping
    fun listJobs(@RequestHeader(API_KEY_HEADER, required = false) apiKey: String?): List<CronJobDto> =
        cronJobService.list(apiKey).map(::toDto)

    @GetMapping("/{id}")
    fun getJob(@RequestHeader(API_KEY_HEADER, required = false) apiKey: String?, @PathVariable id: String): CronJobDto =
        toDto(cronJobService.get(apiKey, id))

    @PutMapping("/{id}")
    fun updateJob(
        @RequestHeader(API_KEY_HEADER, required = false) apiKey: String?,
        @PathVariable id: String,
        @Valid @RequestBody request: CronJobRequest,
    ): CronJobDto = toDto(cronJobService.update(apiKey, id, request.toDraft()))

    @DeleteMapping("/{id}")
    fun deleteJob(@RequestHeader(API_KEY_HEADER, required = false) apiKey: String?, @PathVariable id: String): ResponseEntity<Void> {
        cronJobService.delete(apiKey, id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/runs")
    fun listRuns(
        @RequestHeader(API_KEY_HEADER, required = false) apiKey: String?,
        @PathVariable id: String,
        @RequestParam(defaultValue = "50") @Min(1) @Max(500) limit: Int,
    ): List<CronRunDto> = cronJobService.runs(apiKey, id, limit).map(CronRunDto::from)

    private fun toDto(job: CronJob) = CronJobDto.from(job, cronJobService.nextRunAt(job))
}
