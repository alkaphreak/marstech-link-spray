package fr.marstech.mtlinkspray.domain.devtools
import fr.marstech.mtlinkspray.adapter.inbound.web.devtools.IpInfoResponse

fun interface IpService {

    /** [ip] must be an IP literal: it is never resolved as a host name. */
    fun lookup(ip: String): IpInfoResponse
}
