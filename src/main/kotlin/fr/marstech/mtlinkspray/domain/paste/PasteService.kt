package fr.marstech.mtlinkspray.domain.paste
import fr.marstech.mtlinkspray.adapter.inbound.web.paste.PasteRequest
import fr.marstech.mtlinkspray.adapter.outbound.mongo.paste.PasteEntity
import jakarta.servlet.http.HttpServletRequest

interface PasteService {
    fun createPaste(request: PasteRequest, httpServletRequest: HttpServletRequest): String
    fun isPasswordProtected(id: String): Boolean
    fun getPaste(id: String, password: String?): PasteEntity
    fun deletePaste(id: String)
}