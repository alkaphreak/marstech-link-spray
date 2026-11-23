package fr.marstech.mtlinkspray.domain.abuse

fun interface AbuseReportStore {
    /** Stores a new report; reports are never rewritten. */
    fun create(report: AbuseReport): AbuseReport
}
