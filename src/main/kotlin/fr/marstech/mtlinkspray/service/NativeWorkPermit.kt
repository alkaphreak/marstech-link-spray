package fr.marstech.mtlinkspray.service

import org.springframework.stereotype.Component
import java.util.concurrent.Semaphore

/**
 * One native workload (tesseract, cwebp, caesiumclt) at a time: each one next to the JVM
 * fills most of the 512 MB instance (ADR 0002, MLS-108).
 */
@Component
class NativeWorkPermit : Semaphore(1)
