package fr.marstech.mtlinkspray.adapter.inbound.web.checklist
import fr.marstech.mtlinkspray.domain.checklist.ChecklistItemDraft
import fr.marstech.mtlinkspray.domain.checklist.ChecklistTemplateDraft
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChecklistTemplateRequestTest {

    @Test
    fun shouldMapEveryField() {
        val request = ChecklistTemplateRequest("Trip", listOf(ChecklistItemRequest("i1", "Passport", "valid")), 3)
        assertEquals(ChecklistTemplateDraft("Trip", listOf(ChecklistItemDraft("i1", "Passport", "valid")), 3), request.toDraft())
    }
}
