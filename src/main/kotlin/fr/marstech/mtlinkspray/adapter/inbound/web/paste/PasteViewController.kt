package fr.marstech.mtlinkspray.adapter.inbound.web.paste
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ThymeleafViewControllerInterface
import fr.marstech.mtlinkspray.adapter.outbound.mongo.signature.WriteSignatureEntity.ResourceType
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum.PASTE
import fr.marstech.mtlinkspray.domain.paste.PasteDraft
import fr.marstech.mtlinkspray.domain.paste.PasteService
import fr.marstech.mtlinkspray.domain.signature.WriteSignatureService
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.clientContext
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.getFilteredPort
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.getHost
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.getScheme
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
class PasteViewController(
    private val pasteService: PasteService,
    private val writeSignatureService: WriteSignatureService,
) : ThymeleafViewControllerInterface {

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
                    .let { modelAndView.addObject("paste", PasteResponse.from(it)) }
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
        @RequestParam(required = false) fp: String?,
        httpServletRequest: HttpServletRequest
    ): String {
        val pasteId = pasteService.createPaste(PasteDraft(content = inputPasteBinTextArea), clientContext(httpServletRequest))
        writeSignatureService.record(ResourceType.PASTE, pasteId, fp, httpServletRequest)
        return getRedirectUrl("/paste/$pasteId")
    }
}