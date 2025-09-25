package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.view.PasteViewController
import fr.marstech.mtlinkspray.dto.PasteResponse
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.PasteEntity
import fr.marstech.mtlinkspray.enums.ViewNameEnum
import fr.marstech.mtlinkspray.service.PasteService
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.servlet.view.InternalResourceViewResolver

class PasteViewControllerTest {

    @Mock
    private lateinit var pasteService: PasteService

    @InjectMocks
    private lateinit var pasteViewController: PasteViewController

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        val viewResolver = InternalResourceViewResolver()
        viewResolver.setPrefix("/WEB-INF/views/")
        viewResolver.setSuffix(".html")
        mockMvc = MockMvcBuilders.standaloneSetup(pasteViewController)
            .setViewResolvers(viewResolver)
            .build()
    }

    @Test
    fun getShowCreateFormReturnsCreateView() {
        mockMvc.perform(get("/paste"))
            .andExpect(status().isOk)
            .andExpect(model().attribute("create", true))
            .andExpect(view().name(ViewNameEnum.PASTE.viewName))
    }

    @Test
    fun getViewPasteReturnsProtectedViewWhenPasswordProtected() {
        `when`(pasteService.isPassordProtected("abc")).thenReturn(true)
        mockMvc.perform(get("/paste/abc"))
            .andExpect(status().isOk)
            .andExpect(model().attribute("isProtected", true))
            .andExpect(model().attribute("pasteId", "abc"))
            .andExpect(model().attribute("create", false))
            .andExpect(view().name(ViewNameEnum.PASTE.viewName))
    }

    @Test
    fun getViewPasteReturnsPasteViewWhenNotPasswordProtected() {
        val pasteId = "abc123"
        val content = "Hello World"
        val entity = PasteEntity(
            id = pasteId,
            content = content,
            author = HistoryItem("user1")
        )
        val response = PasteResponse.fromEntity(entity)

        `when`(pasteService.isPassordProtected("xyz")).thenReturn(false)
        `when`(pasteService.getPaste("xyz", null)).thenReturn(entity)

        mockMvc.perform(get("/paste/xyz"))
            .andExpect(status().isOk)
            .andExpect(model().attribute("isProtected", false))
            .andExpect(model().attribute("paste", response))
            .andExpect(model().attribute("create", false))
            .andExpect(view().name(ViewNameEnum.PASTE.viewName))
    }

    @Test
    fun postCreatePasteRedirectsToNewPaste() {
        val request = mock(HttpServletRequest::class.java)
        `when`(
            pasteService.createPaste(any(), any())
        ).thenReturn("newid")
        mockMvc = MockMvcBuilders.standaloneSetup(
            PasteViewController(pasteService)
        ).build()

        mockMvc.perform(
            post("/paste")
                .param("inputPasteBinTextArea", "test content")
                .requestAttr("jakarta.servlet.http.HttpServletRequest", request)
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/paste/newid"))
    }
}