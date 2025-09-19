package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.service.PasteService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/paste")
class PasteViewController(private val pasteService: PasteService) {

    @GetMapping("/{pasteId}")
    fun viewPaste(
        @PathVariable pasteId: String,
        @RequestParam(required = false) password: String?,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        return try {
            val paste = pasteService.getPaste(pasteId, password)
            model.addAttribute("paste", paste)
            "pasteView"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", e.message)
            "redirect:/paste/error"
        }
    }

    @GetMapping("/create")
    fun showCreateForm(model: Model): String {
        model.addAttribute(
            "pasteRequest", PasteRequest(
                "",
                "",
                "",
                null,
                "10m",
                false,
            )
        )
        return "createPaste"
    }

    @PostMapping
    fun createPaste(
        @ModelAttribute pasteRequest: PasteRequest,
        redirectAttributes: RedirectAttributes,
        httpServletRequest: HttpServletRequest
    ): String {
        val pasteId = pasteService.createPaste(pasteRequest, httpServletRequest)
        return "redirect:/paste/$pasteId"
    }

    @PostMapping("/{pasteId}/delete")
    fun deletePaste(
        @PathVariable pasteId: String, redirectAttributes: RedirectAttributes
    ): String {
        pasteService.deletePaste(pasteId)
        redirectAttributes.addFlashAttribute("message", "Paste deleted")
        return "redirect:/"
    }

    @GetMapping("/error")
    fun showErrorPage(): String = "error"
}