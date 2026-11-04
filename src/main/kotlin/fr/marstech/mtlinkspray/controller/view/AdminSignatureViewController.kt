package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.ADMIN_SIGNATURES
import fr.marstech.mtlinkspray.service.WriteSignatureService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

/**
 * Admin lookup of write signatures by resource (short code, paste id, spray URL), from an abuse
 * report or a legal request (MLS-254 phase B). Protected by `SecurityConfig` ("/admin" tree).
 */
@Controller
@RequestMapping("/admin/signatures")
class AdminSignatureViewController(
    private val writeSignatureService: WriteSignatureService
) : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(ADMIN_SIGNATURES)

    @GetMapping
    fun getView(@RequestParam(required = false) resourceId: String?): ModelAndView = getModelAndView()
        .addObject("resourceId", resourceId)
        .addObject(
            "signatures",
            resourceId?.takeIf { it.isNotBlank() }?.let(writeSignatureService::findByResourceId).orEmpty()
        )
}
