package fr.marstech.mtlinkspray.service

/**
 * Evicts a public URL from the CDN cache in front of MLS (Cloudflare, MLS-237), so a deleted
 * public image stops being served before its `max-age` expires.
 */
interface CachePurgeService {

    /**
     * Purges the public URL of [path] (e.g. `/api/images/{id}`). Best effort: never throws.
     */
    fun purge(path: String)
}
