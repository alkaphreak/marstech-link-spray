package fr.marstech.mtlinkspray.enums

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ViewNameEnumTest {

    @Test
    fun shouldReturnOnlyItemsInMenuSortedByOrder() {
        val inMenu = ViewNameEnum.inMenu()
        assertFalse(inMenu.isEmpty(), "inMenu() should not be empty")

        // All returned items must have isInMenu == true
        inMenu.forEach { assertTrue(it.isInMenu, "${it.name} should be in menu") }

        // The list must be sorted by orderInMenu ascending
        for (i in 1 until inMenu.size) {
            assertTrue(
                inMenu[i - 1].orderInMenu <= inMenu[i].orderInMenu,
                "inMenu list must be sorted by orderInMenu"
            )
        }

        // Expect that the first visible item is SPRAY (order 0)
        assertEquals(ViewNameEnum.SPRAY, inMenu.first())
    }

    @Test
    fun shouldExcludeNonMenuItems() {
        val inMenuNames = ViewNameEnum.inMenu().map { it.name }.toSet()

        assertFalse(ViewNameEnum.HOME.isInMenu, "HOME should not be in menu")
        assertFalse(ViewNameEnum.ERROR.isInMenu, "ERROR should not be in menu")

        assertFalse(inMenuNames.contains(ViewNameEnum.HOME.name), "inMenu() must not contain HOME")
        assertFalse(inMenuNames.contains(ViewNameEnum.ERROR.name), "inMenu() must not contain ERROR")
    }

    @Test
    fun everyEntryHasNonBlankViewNameAndViewPage() {
        // Use entries instead of values() as recommended since Kotlin 1.9
        for (entry in ViewNameEnum.entries) {
            assertNotNull(entry.viewName, "${entry.name}.viewName should not be null")
            assertNotNull(entry.viewPage, "${entry.name}.viewPage should not be null")
            assertTrue(entry.viewName.isNotBlank(), "${entry.name}.viewName should not be blank")
            assertTrue(entry.viewPage.isNotBlank(), "${entry.name}.viewPage should not be blank")
        }
    }
}
