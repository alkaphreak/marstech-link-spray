package fr.marstech.mtlinkspray.domain.paste

import fr.marstech.mtlinkspray.domain.shared.ClientContext

interface PasteService {
    fun createPaste(draft: PasteDraft, client: ClientContext): String
    fun isPasswordProtected(id: String): Boolean
    fun getPaste(id: String, password: String?): Paste
    fun deletePaste(id: String)
}
