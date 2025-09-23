package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.dto.PasteResponse
import fr.marstech.mtlinkspray.enums.ViewNameEnum.PASTE
import fr.marstech.mtlinkspray.service.PasteService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/paste")
class PasteViewController(private val pasteService: PasteService) : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(PASTE)

    @GetMapping("/{pasteId}")
    fun viewPaste(
        @PathVariable pasteId: String,
    ): ModelAndView = getModelAndView().let { modelAndView ->
        pasteService.isPassordProtected(pasteId).let { isProtected ->
            modelAndView.addObject("isProtected", isProtected)
            when {
                isProtected -> modelAndView.addObject("pasteId", pasteId)
                else -> pasteService.getPaste(pasteId, null)
                    .let { modelAndView.addObject("paste", PasteResponse.fromEntity(it)) }
            }
        }.addObject("create", false)
    }

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