package fr.marstech.mtlinkspray.controller.api

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class UuidApiControllerTest {

    var uuidApiController: UuidApiController = UuidApiController()

    @Test
    fun testGetUuid() {
        val uuid1 = uuidApiController.getUuid()
        val uuid2 = uuidApiController.getUuid()
        Assertions.assertAll(
            { Assertions.assertNotNull(uuid1) },
            { Assertions.assertNotNull(uuid2) },
            { Assertions.assertNotEquals(uuid1, uuid2) },
            { Assertions.assertEquals(uuid1, UUID.fromString(uuid1).toString()) },
            { Assertions.assertEquals(uuid2, UUID.fromString(uuid2).toString()) })
    }

    @Test
    fun testUuidFormat() {
        val uuid = uuidApiController.getUuid()
        val uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".toRegex()
        Assertions.assertTrue(uuidPattern.matches(uuid), "UUID format is incorrect")
        Assertions.assertEquals(36, uuid.length, "UUID length should be 36")
    }

    @Test
    fun testMultipleUuidsAreUnique() {
        val count = 100
        val uuids = (0 until count).map { uuidApiController.getUuid() }.toSet()
        Assertions.assertEquals(count, uuids.size, "All UUIDs should be unique")
    }
}
