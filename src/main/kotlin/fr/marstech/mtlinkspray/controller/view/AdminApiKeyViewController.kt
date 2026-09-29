package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.ADMIN_API_KEYS
import fr.marstech.mtlinkspray.service.ApiKeyService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.NotBlank
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes

/**
 * Admin page to manage API keys for client apps (MLS-203). Protected by HTTP Basic auth
 * (see `SecurityConfig`, restricted to the "/admin" path tree). Key creation is admin-only, never
 * self-service, per MLS-203 decision.
 */
@Controller
@RequestMapping("/admin/api-keys")
class AdminApiKeyViewController(
    private val apiKeyService: ApiKeyService
) : ThymeleafViewControllerInterface {

    @GetMapping
    fun getView(httpServletRequest: HttpServletRequest): ModelAndView =
        getModelAndView(httpServletRequest).addObject("apiKeys", apiKeyService.listApiKeys())

    @PostMapping
    fun createApiKey(
        @RequestParam @NotBlank name: String,
        @RequestParam @NotBlank namespace: String,
        redirectAttributes: RedirectAttributes,
    ): String {
        val result = apiKeyService.createApiKey(name, namespace)
        redirectAttributes.addFlashAttribute("newRawKey", result.rawKey)
        redirectAttributes.addFlashAttribute("newKeyName", result.apiKey.name)
        return getRedirectUrl("/admin/api-keys")
    }

    @PostMapping("/{id}/toggle")
    fun toggleApiKey(
        @PathVariable @NotBlank id: String,
        @RequestParam isEnabled: Boolean,
    ): String {
        apiKeyService.setEnabled(id, isEnabled)
        return getRedirectUrl("/admin/api-keys")
    }

    override fun getModelAndView(): ModelAndView = getModelAndView(ADMIN_API_KEYS)

    fun getModelAndView(httpServletRequest: HttpServletRequest): ModelAndView =
        getModelAndView(ADMIN_API_KEYS, httpServletRequest)
}
