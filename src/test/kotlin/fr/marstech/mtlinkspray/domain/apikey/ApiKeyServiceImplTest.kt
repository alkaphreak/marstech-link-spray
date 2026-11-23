package fr.marstech.mtlinkspray.domain.apikey
import fr.marstech.mtlinkspray.adapter.outbound.security.BCryptPasswordHasher
import fr.marstech.mtlinkspray.domain.shared.ApiCaller
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@ExtendWith(OutputCaptureExtension::class)
class ApiKeyServiceImplTest {

    private val apiKeyStore = mock(ApiKeyStore::class.java)
    private val apiKeyService = ApiKeyServiceImpl(apiKeyStore, BCryptPasswordHasher())
    private val passwordEncoder = BCryptPasswordEncoder()

    private fun givenApiKey(
        keyId: String = "key123",
        secret: String = "s3cr3t",
        namespace: String = "maeva-app-1",
        isEnabled: Boolean = true,
    ): ApiKey = ApiKey(
        id = "id1",
        name = "Maeva App",
        keyId = keyId,
        secretHash = passwordEncoder.encode(secret)!!,
        namespace = namespace,
        isEnabled = isEnabled,
    )

    @Test
    fun resolveShouldReturnCallerWhenRawKeyIsValid() {
        val apiKey = givenApiKey()
        `when`(apiKeyStore.findByKeyId("key123")).thenReturn(apiKey)

        val result = apiKeyService.resolve("key123.s3cr3t")

        assertEquals(ApiCaller(apiKeyId = "id1", namespace = "maeva-app-1"), result)
    }

    @Test
    fun isNamespaceEnabledShouldFollowTheOwningKey() {
        `when`(apiKeyStore.findByNamespace("maeva-app-1")).thenReturn(givenApiKey())
        `when`(apiKeyStore.findByNamespace("disabled")).thenReturn(givenApiKey(namespace = "disabled", isEnabled = false))
        `when`(apiKeyStore.findByNamespace("unknown")).thenReturn(null)

        assertTrue(apiKeyService.isNamespaceEnabled("maeva-app-1"))
        assertFalse(apiKeyService.isNamespaceEnabled("disabled"))
        assertFalse(apiKeyService.isNamespaceEnabled("unknown"))
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
        `when`(apiKeyStore.findByKeyId("unknown")).thenReturn(null)

        assertThrows<IllegalAccessException> {
            apiKeyService.resolve("unknown.s3cr3t")
        }
    }

    @Test
    fun resolveShouldThrowWhenSecretIsWrong() {
        val apiKey = givenApiKey()
        `when`(apiKeyStore.findByKeyId("key123")).thenReturn(apiKey)

        assertThrows<IllegalAccessException> {
            apiKeyService.resolve("key123.wrongSecret")
        }
    }

    @Test
    fun resolveShouldThrowWhenApiKeyIsDisabled() {
        val apiKey = givenApiKey(isEnabled = false)
        `when`(apiKeyStore.findByKeyId("key123")).thenReturn(apiKey)

        assertThrows<IllegalAccessException> {
            apiKeyService.resolve("key123.s3cr3t")
        }
    }

    @Test
    fun createApiKeyShouldPersistAndReturnRawKey() {
        `when`(apiKeyStore.findByNamespace("maeva-app-1")).thenReturn(null)
        val captor = argumentCaptor<ApiKey>()
        `when`(apiKeyStore.save(captor.capture())).thenAnswer { it.arguments[0] }

        val result = apiKeyService.createApiKey("Maeva App", "maeva-app-1")

        val saved = captor.firstValue
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
        `when`(apiKeyStore.findByNamespace(org.mockito.kotlin.any())).thenReturn(null)
        `when`(apiKeyStore.save(org.mockito.kotlin.any())).thenAnswer { it.arguments[0] }

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
        `when`(apiKeyStore.findByNamespace("maeva-app-1")).thenReturn(givenApiKey())

        assertThrows<IllegalArgumentException> {
            apiKeyService.createApiKey("Maeva App", "maeva-app-1")
        }
    }

    @Test
    fun listApiKeysShouldReturnAllApiKeys() {
        val apiKeys = listOf(givenApiKey(), givenApiKey(keyId = "key456", namespace = "app2"))
        `when`(apiKeyStore.findAll()).thenReturn(apiKeys)

        val result = apiKeyService.listApiKeys()

        assertEquals(apiKeys, result)
    }

    @Test
    fun setEnabledShouldUpdateOnlyTheEnabledField() {
        // Given
        `when`(apiKeyStore.updateEnabled("id1", false)).thenReturn(true)
        `when`(apiKeyStore.findById("id1")).thenReturn(givenApiKey(isEnabled = false))

        // When
        val result = apiKeyService.setEnabled("id1", false)

        // Then
        assertFalse(result.isEnabled)
        verify(apiKeyStore).updateEnabled("id1", false)
        verify(apiKeyStore, never()).save(org.mockito.kotlin.any())
    }

    @Test
    fun setEnabledShouldThrowWhenApiKeyNotFound() {
        `when`(apiKeyStore.updateEnabled("unknown", false)).thenReturn(false)

        assertThrows<NoSuchElementException> {
            apiKeyService.setEnabled("unknown", false)
        }
    }

    @Test
    fun regenerateSecretShouldKeepKeyIdAndReplaceSecret() {
        // Given
        val apiKey = givenApiKey()
        `when`(apiKeyStore.findById("id1")).thenReturn(apiKey)
        `when`(apiKeyStore.updateSecretHash(eq("id1"), org.mockito.kotlin.any())).thenReturn(true)

        // When
        val result = apiKeyService.regenerateSecret("id1")

        // Then
        val (keyId, secret) = result.rawKey.split(".", limit = 2)
        assertEquals("key123", keyId)
        assertEquals(apiKey.namespace, result.apiKey.namespace)
        `when`(apiKeyStore.findByKeyId("key123")).thenReturn(result.apiKey)
        assertEquals(ApiCaller(apiKeyId = "id1", namespace = apiKey.namespace), apiKeyService.resolve("key123.$secret"))
        assertThrows<IllegalAccessException> { apiKeyService.resolve("key123.s3cr3t") }
        verify(apiKeyStore).updateSecretHash("id1", result.apiKey.secretHash)
        verify(apiKeyStore, never()).save(org.mockito.kotlin.any())
    }

    @Test
    fun regenerateSecretShouldThrowWhenApiKeyNotFound() {
        `when`(apiKeyStore.findById("unknown")).thenReturn(null)

        assertThrows<NoSuchElementException> {
            apiKeyService.regenerateSecret("unknown")
        }
    }

    @Test
    fun createApiKeyShouldLogKeyIdAndNamespaceWithoutSecretOrHash(output: CapturedOutput) {
        // Given
        `when`(apiKeyStore.findByNamespace("maeva-app-1")).thenReturn(null)
        `when`(apiKeyStore.save(org.mockito.kotlin.any())).thenAnswer { (it.arguments[0] as ApiKey).copy(id = "id1") }

        // When
        val result = apiKeyService.createApiKey("Maeva App", "maeva-app-1")

        // Then
        val (keyId, secret) = result.rawKey.split(".", limit = 2)
        assertTrue(output.out.contains("API key created: id=id1, keyId=$keyId, namespace=maeva-app-1, name=Maeva App"))
        assertSecretsNotLogged(output, secret, result.apiKey.secretHash)
    }

    @Test
    fun setEnabledShouldLogKeyIdNamespaceAndState(output: CapturedOutput) {
        // Given
        val apiKey = givenApiKey(isEnabled = false)
        `when`(apiKeyStore.updateEnabled("id1", false)).thenReturn(true)
        `when`(apiKeyStore.findById("id1")).thenReturn(apiKey)

        // When
        apiKeyService.setEnabled("id1", false)

        // Then
        assertTrue(output.out.contains("API key enabled set: id=id1, keyId=key123, namespace=maeva-app-1, enabled=false"))
        assertSecretsNotLogged(output, "s3cr3t", apiKey.secretHash)
    }

    @Test
    fun regenerateSecretShouldLogKeyIdAndNamespaceWithoutSecretOrHash(output: CapturedOutput) {
        // Given
        val apiKey = givenApiKey()
        `when`(apiKeyStore.findById("id1")).thenReturn(apiKey)
        `when`(apiKeyStore.updateSecretHash(eq("id1"), org.mockito.kotlin.any())).thenReturn(true)

        // When
        val result = apiKeyService.regenerateSecret("id1")

        // Then
        val secret = result.rawKey.substringAfter(".")
        assertTrue(output.out.contains("API key secret regenerated: id=id1, keyId=key123, namespace=maeva-app-1"))
        assertSecretsNotLogged(output, secret, result.apiKey.secretHash)
        assertSecretsNotLogged(output, "s3cr3t", apiKey.secretHash)
    }

    @Test
    fun createApiKeyShouldEscapeLineBreaksInLoggedValues(output: CapturedOutput) {
        // Given
        `when`(apiKeyStore.findByNamespace(org.mockito.kotlin.any())).thenReturn(null)
        `when`(apiKeyStore.save(org.mockito.kotlin.any())).thenAnswer { it.arguments[0] }

        // When
        apiKeyService.createApiKey("App\nAPI key created: id=forged", "ns\r\nx")

        // Then
        assertTrue(output.out.contains("namespace=ns\\r\\nx, name=App\\nAPI key created: id=forged"))
        assertFalse(output.out.lines().any { it.startsWith("API key created: id=forged") })
    }

    private fun assertSecretsNotLogged(output: CapturedOutput, secret: String, secretHash: String) {
        assertFalse(output.all.contains(secret))
        assertFalse(output.all.contains(secretHash))
    }
}
