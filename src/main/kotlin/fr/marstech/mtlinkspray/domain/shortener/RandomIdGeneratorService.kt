package fr.marstech.mtlinkspray.domain.shortener
interface RandomIdGeneratorService {

    fun generateRandomId(): String

    fun getGeneratedFreeId(): String
    fun getGeneratedFreeIdWithoutCache(): String
    fun getGeneratedFreeIdWithCache(): String
}
