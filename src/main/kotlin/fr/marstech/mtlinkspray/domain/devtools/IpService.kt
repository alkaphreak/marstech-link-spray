package fr.marstech.mtlinkspray.domain.devtools

fun interface IpService {

    /** [ip] must be an IP literal: it is never resolved as a host name. */
    fun lookup(ip: String): IpInfo
}
