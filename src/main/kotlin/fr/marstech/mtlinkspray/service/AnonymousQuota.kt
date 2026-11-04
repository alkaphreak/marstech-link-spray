package fr.marstech.mtlinkspray.service

import org.springframework.http.HttpStatus.TOO_MANY_REQUESTS
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/** Per-IP request quota for anonymous callers of a native-workload API (OCR, image compression). */
class AnonymousQuota(private val name: String, private val quota: Int, private val windowMillis: Long) {

    private val counts = ConcurrentHashMap<String, AtomicInteger>()
    @Volatile
    private var windowStart = System.currentTimeMillis()

    fun count(clientIp: String) {
        val now = System.currentTimeMillis()
        // ponytail: fixed window cleared for every IP at once, sliding window or MLS-161 (Redis) if boundary bursts matter
        if (now - windowStart >= windowMillis) {
            counts.clear()
            windowStart = now
        }
        if (counts.computeIfAbsent(clientIp) { AtomicInteger() }.incrementAndGet() > quota) {
            throw ResponseStatusException(TOO_MANY_REQUESTS, "Anonymous $name quota of $quota reached: retry later or send an X-Api-Key")
        }
    }
}
