package fr.marstech.mtlinkspray.domain.shared

/**
 * The client app behind a valid API key: [apiKeyId] owns what the app stores (images,
 * dashboards), [namespace] scopes what it lists (checklists, cron jobs).
 */
data class ApiCaller(
    val apiKeyId: String,
    val namespace: String,
)
