package fr.marstech.mtlinkspray.domain.abuse
import jakarta.servlet.http.HttpServletRequest

fun interface ReportAbuseService {
    fun reportAbuse(
        inputAbuseDecsription: String,
        httpServletRequest: HttpServletRequest
    )
}
