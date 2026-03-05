package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.RandomNumberResponse

interface RandomNumberService {

    fun generateRandom(min: String?, max: String?): RandomNumberResponse
}
