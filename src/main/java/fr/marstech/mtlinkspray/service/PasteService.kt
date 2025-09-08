package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.entity.Paste

interface PasteService {
    fun createPaste(request: PasteRequest): String
    fun getPaste(id: String, password: String?): Paste?
    fun deletePaste(id: String)
}