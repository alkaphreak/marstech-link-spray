package fr.marstech.mtlinkspray.domain.abuse
import fr.marstech.mtlinkspray.domain.shared.ClientContext

fun interface ReportAbuseService {
    fun reportAbuse(
        inputAbuseDecsription: String,
        client: ClientContext,
    )
}
