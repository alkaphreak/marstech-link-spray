package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.IpInfoResponse

fun interface IpService {

    /** [ip] must be an IP literal: it is never resolved as a host name. */
    fun lookup(ip: String): IpInfoResponse
}
