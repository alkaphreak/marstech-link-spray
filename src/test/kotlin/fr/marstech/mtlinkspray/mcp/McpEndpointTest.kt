package fr.marstech.mtlinkspray.mcp

import fr.marstech.mtlinkspray.service.CronJobService
import fr.marstech.mtlinkspray.service.DnsService
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * /mcp through the full context (MLS-266, ADR 0004): registration, schemas, error mapping and the
 * stateless transport, which Sol4McpToolsTest cannot see since it calls the bean directly.
 */
@SpringBootTest
@AutoConfigureMockMvc
internal class McpEndpointTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    // No outbound DNS from tests
    @MockitoBean
    private lateinit var dnsService: DnsService

    // No MongoDB in the test profile: its startup index creation waits 3 x 30 s otherwise
    @MockitoBean
    private lateinit var cronJobService: CronJobService

    private fun rpc(body: String): ResultActions = mockMvc.perform(
        post("/mcp")
            .contentType("application/json")
            .header("Accept", "application/json, text/event-stream")
            .content(body)
    )

    @Test
    fun initializeShouldAnswerWithoutASession() {
        rpc("""{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"test","version":"1"}}}""")
            .andExpect(status().isOk)
            .andExpect(header().doesNotExist("Mcp-Session-Id"))
            .andExpect(jsonPath("$.result.serverInfo.name").value("sol4-space"))
    }

    @Test
    fun toolsListShouldExposeTheSixToolsWithTheirRequiredParameters() {
        rpc("""{"jsonrpc":"2.0","id":2,"method":"tools/list"}""")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.tools", hasSize<Any>(6)))
            .andExpect(
                jsonPath("$.result.tools[*].name", containsInAnyOrder(
                    "uuid", "random_number", "dns_lookup", "dns_reverse", "ip_lookup", "qr_code",
                ))
            )
            .andExpect(jsonPath("$.result.tools[?(@.name == 'dns_lookup')].inputSchema.required[0]").value("name"))
            .andExpect(jsonPath("$.result.tools[?(@.name == 'ip_lookup')].inputSchema.required[0]").value("ip"))
            .andExpect(jsonPath("$.result.tools[?(@.name == 'random_number')].inputSchema.required[0]").value("max"))
            .andExpect(jsonPath("$.result.tools[?(@.name == 'qr_code')].inputSchema.required[0]").value("text"))
    }

    @Test
    fun toolsCallShouldAnswerAValidCall() {
        rpc("""{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"random_number","arguments":{"min":5,"max":5}}}""")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.isError").value(false))
            .andExpect(jsonPath("$.result.content[0].text").value(containsString("\"value\":5")))
    }

    @Test
    fun toolsCallShouldReturnIsErrorWithTheRestMessageOnInvalidInput() {
        rpc("""{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"dns_lookup","arguments":{"name":"not a domain"}}}""")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.isError").value(true))
            .andExpect(jsonPath("$.result.content[0].text").value(containsString("Name must be a valid domain name")))
    }
}
