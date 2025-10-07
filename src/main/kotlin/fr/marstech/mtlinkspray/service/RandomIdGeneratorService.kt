package fr.marstech.mtlinkspray.service

interface RandomIdGeneratorService {

    fun generateRandomId(): String

    fun getGeneratedFreeId(): String
    fun getGeneratedFreeIdWithoutCache(): String
    fun getGeneratedFreeIdWithCache(): String
}
