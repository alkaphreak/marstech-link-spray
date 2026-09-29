package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.entity.ApiKeyEntity
import fr.marstech.mtlinkspray.repository.ApiKeyRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

class ApiKeyServiceImplTest {

    private val apiKeyRepository = mock(ApiKeyRepository::class.java)
    private val apiKeyService = ApiKeyServiceImpl(apiKeyRepository)
    private val passwordEncoder = BCryptPasswordEncoder()

    private fun givenApiKey(
        keyId: String = "key123",
        secret: String = "s3cr3t",
        namespace: String = "maeva-app-1",
        isEnabled: Boolean = true,
    ): ApiKeyEntity = ApiKeyEntity(
        id = "id1",
        name = "Maeva App",
        keyId = keyId,
        secretHash = passwordEncoder.encode(secret)!!,
        namespace = namespace,
        isEnabled = isEnabled,
    )

    @Test
    fun resolveShouldReturnApiKeyWhenRawKeyIsValid() {
        val apiKey = givenApiKey()
        `when`(apiKeyRepository.findByKeyId("key123")).thenReturn(Optional.of(apiKey))

        val result = apiKeyService.resolve("key123.s3cr3t")

        assertEquals(apiKey, result)
    }

    @Test
    fun resolveShouldThrowWhenRawKeyIsNull() {
        assertThrows<IllegalAccessException> {
            apiKeyService.resolve(null)
        }
    }

    @Test
    fun resolveShouldThrowWhenRawKeyIsBlank() {
        assertThrows<IllegalAccessException> {
            apiKeyService.resolve("   ")
        }
    }

    @Test
    fun resolveShouldThrowWhenRawKeyIsMalformed() {
        assertThrows<IllegalAccessException> {
            apiKeyService.resolve("no-dot-separator")
        }
    }

    @Test
    fun resolveShouldThrowWhenKeyIdIsUnknown() {
        `when`(apiKeyRepository.findByKeyId("unknown")).thenReturn(Optional.empty())

        assertThrows<IllegalAccessException> {
            apiKeyService.resolve("unknown.s3cr3t")
        }
    }

    @Test
    fun resolveShouldThrowWhenSecretIsWrong() {
        val apiKey = givenApiKey()
        `when`(apiKeyRepository.findByKeyId("key123")).thenReturn(Optional.of(apiKey))

        assertThrows<IllegalAccessException> {
            apiKeyService.resolve("key123.wrongSecret")
        }
    }

    @Test
    fun resolveShouldThrowWhenApiKeyIsDisabled() {
        val apiKey = givenApiKey(isEnabled = false)
        `when`(apiKeyRepository.findByKeyId("key123")).thenReturn(Optional.of(apiKey))

        assertThrows<IllegalAccessException> {
            apiKeyService.resolve("key123.s3cr3t")
        }
    }

    @Test
    fun createApiKeyShouldPersistAndReturnRawKey() {
        `when`(apiKeyRepository.findByNamespace("maeva-app-1")).thenReturn(Optional.empty())
        val captor = ArgumentCaptor.forClass(ApiKeyEntity::class.java)
        `when`(apiKeyRepository.save(captor.capture())).thenAnswer { it.arguments[0] }

        val result = apiKeyService.createApiKey("Maeva App", "maeva-app-1")

        val saved = captor.value
        assertEquals("Maeva App", saved.name)
        assertEquals("maeva-app-1", saved.namespace)
        assertTrue(saved.isEnabled)
        assertEquals(saved, result.apiKey)

        val (keyId, secret) = result.rawKey.split(".", limit = 2)
        assertEquals(saved.keyId, keyId)
        assertTrue(passwordEncoder.matches(secret, saved.secretHash))
    }

    @Test
    fun createApiKeyShouldGenerateDistinctKeysOnEachCall() {
        `when`(apiKeyRepository.findByNamespace(org.mockito.kotlin.any())).thenReturn(Optional.empty())
        `when`(apiKeyRepository.save(org.mockito.kotlin.any())).thenAnswer { it.arguments[0] }

        val first = apiKeyService.createApiKey("App1", "ns1")
        val second = apiKeyService.createApiKey("App2", "ns2")

        assertNotEquals(first.rawKey, second.rawKey)
        assertNotEquals(first.apiKey.keyId, second.apiKey.keyId)
    }

    @Test
    fun createApiKeyShouldThrowWhenNameIsBlank() {
        assertThrows<IllegalArgumentException> {
            apiKeyService.createApiKey("   ", "maeva-app-1")
        }
    }

    @Test
    fun createApiKeyShouldThrowWhenNamespaceIsBlank() {
        assertThrows<IllegalArgumentException> {
            apiKeyService.createApiKey("Maeva App", "   ")
        }
    }

    @Test
    fun createApiKeyShouldThrowWhenNamespaceAlreadyExists() {
        `when`(apiKeyRepository.findByNamespace("maeva-app-1")).thenReturn(Optional.of(givenApiKey()))

        assertThrows<IllegalArgumentException> {
            apiKeyService.createApiKey("Maeva App", "maeva-app-1")
        }
    }

    @Test
    fun listApiKeysShouldReturnAllApiKeys() {
        val apiKeys = listOf(givenApiKey(), givenApiKey(keyId = "key456", namespace = "app2"))
        `when`(apiKeyRepository.findAll()).thenReturn(apiKeys)

        val result = apiKeyService.listApiKeys()

        assertEquals(apiKeys, result)
    }

    @Test
    fun setEnabledShouldToggleAndSaveApiKey() {
        val apiKey = givenApiKey(isEnabled = true)
        `when`(apiKeyRepository.findById("id1")).thenReturn(Optional.of(apiKey))
        `when`(apiKeyRepository.save(apiKey)).thenReturn(apiKey)

        val result = apiKeyService.setEnabled("id1", false)

        assertFalse(result.isEnabled)
        verify(apiKeyRepository).save(apiKey)
    }

    @Test
    fun setEnabledShouldThrowWhenApiKeyNotFound() {
        `when`(apiKeyRepository.findById("unknown")).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            apiKeyService.setEnabled("unknown", false)
        }
    }
}
