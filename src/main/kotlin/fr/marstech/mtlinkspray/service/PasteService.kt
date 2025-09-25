package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.entity.PasteEntity
import jakarta.servlet.http.HttpServletRequest

interface PasteService {
    fun createPaste(request: PasteRequest, httpServletRequest: HttpServletRequest): String
    fun isPassordProtected(id: String): Boolean
    fun getPaste(id: String, password: String?): PasteEntity
    fun deletePaste(id: String)
}