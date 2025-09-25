package fr.marstech.mtlinkspray.service

import jakarta.servlet.http.HttpServletRequest

interface ReportAbuseService {
    fun reportAbuse(
        inputAbuseDecsription: String,
        httpServletRequest: HttpServletRequest
    )
}
