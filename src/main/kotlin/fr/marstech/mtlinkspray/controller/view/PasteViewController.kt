package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.dto.PasteResponse
import fr.marstech.mtlinkspray.enums.ViewNameEnum.PASTE
import fr.marstech.mtlinkspray.service.PasteService
import fr.marstech.mtlinkspray.utils.NetworkUtils.getFilteredPort
import fr.marstech.mtlinkspray.utils.NetworkUtils.getHost
import fr.marstech.mtlinkspray.utils.NetworkUtils.getScheme
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets

@Controller
@RequestMapping("/paste")
class PasteViewController(private val pasteService: PasteService) : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(PASTE)

    @GetMapping("/{pasteId}")
    fun viewPaste(
        @PathVariable pasteId: String,
        httpServletRequest: HttpServletRequest,
    ): ModelAndView = getModelAndView().let { modelAndView ->
        pasteService.isPasswordProtected(pasteId).let { isProtected ->
            modelAndView.addObject("isProtected", isProtected)
            when {
                isProtected -> modelAndView.addObject("pasteId", pasteId)
                else -> pasteService.getPaste(pasteId, null)
                    .let { modelAndView.addObject("paste", PasteResponse.fromEntity(it)) }
            }
        }.addObject("create", false)
            .addObject(
                "pasteBinUrl", UriComponentsBuilder.newInstance()
                    .scheme(getScheme(httpServletRequest))
                    .host(getHost(httpServletRequest))
                    .port(getFilteredPort(httpServletRequest))
                    .path("/paste/$pasteId")
                    .build()
                    .encode()
                    .toString()
            )
    }

    @GetMapping("/{pasteId}/raw", produces = [TEXT_PLAIN_VALUE])
    @ResponseBody
    fun viewPasteRaw(
        @PathVariable pasteId: String,
        @RequestParam(required = false) password: String?
    ): ResponseEntity<String> = try {
        pasteService.getPaste(pasteId, password)
            .let { paste ->
                plainText(OK)
                    .header(CONTENT_DISPOSITION, "inline; filename=\"paste-$pasteId.txt\"")
                    .body(paste.content)
            }
    } catch (_: NoSuchElementException) {
        plainText(NOT_FOUND).body("Not found: paste $pasteId does not exist")
    } catch (_: IllegalAccessException) {
        plainText(UNAUTHORIZED).body("Unauthorized: password required or incorrect")
    }

    private fun plainText(status: HttpStatus): ResponseEntity.BodyBuilder =
        ResponseEntity.status(status)
            .contentType(MediaType("text", "plain", StandardCharsets.UTF_8))
            .header("X-Content-Type-Options", "nosniff")

    @GetMapping
    fun showCreateForm(): ModelAndView = getModelAndView().addObject("create", true)

    @PostMapping
    fun createPaste(
        @RequestParam(name = "inputPasteBinTextArea") inputPasteBinTextArea: String,
        httpServletRequest: HttpServletRequest
    ): String = PasteRequest(
        content = inputPasteBinTextArea,
    ).let {
        pasteService.createPaste(it, httpServletRequest)
    }.let {
        getRedirectUrl("/paste/$it")
    }
}