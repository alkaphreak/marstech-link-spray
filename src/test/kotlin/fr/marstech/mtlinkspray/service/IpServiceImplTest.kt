package fr.marstech.mtlinkspray.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

/** Fixtures: MaxMind's GeoLite2 test databases (MIT / Apache 2.0), same layout as DB-IP Lite. */
class IpServiceImplTest {

    private val service = IpServiceImpl(File(javaClass.getResource("/geoip")!!.toURI()).path)

    @Test
    fun shouldReturnCountryOfIpv4() {
        // When
        val info = service.lookup("81.2.69.160")

        // Then
        assertEquals(4, info.version)
        assertEquals("GB", info.countryCode)
        assertEquals("United Kingdom", info.countryName)
    }

    @Test
    fun shouldReturnAsnAndFlagDatacenter() {
        // When
        val info = service.lookup("1.0.0.1")

        // Then
        assertEquals(15169L, info.asn)
        assertEquals("Google Inc.", info.organization)
        assertTrue(info.datacenter)
    }

    @Test
    fun shouldNotFlagConsumerIsp() {
        // When
        val info = service.lookup("1.128.0.1")

        // Then
        assertEquals(1221L, info.asn)
        assertFalse(info.datacenter)
    }

    @Test
    fun shouldReturnIpv6WithoutDataWhenUnknown() {
        // When
        val info = service.lookup("2001:db8::1")

        // Then
        assertEquals(6, info.version)
        assertEquals("2001:db8:0:0:0:0:0:1", info.ip)
        assertNull(info.countryCode)
        assertNull(info.asn)
    }

    @Test
    fun shouldAnswerWithoutDatabases() {
        // Given
        val bare = IpServiceImpl("/nonexistent")

        // When
        val info = bare.lookup("81.2.69.160")

        // Then
        assertEquals("81.2.69.160", info.ip)
        assertNull(info.countryCode)
        assertFalse(info.datacenter)
    }

    @Test
    fun shouldRejectHostName() {
        assertThrows<IllegalArgumentException> { service.lookup("example.com") }
    }
}
