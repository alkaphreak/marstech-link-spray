package fr.marstech.mtlinkspray.domain.nativetools
import org.springframework.stereotype.Service
import java.util.concurrent.Semaphore

/**
 * One native workload (tesseract, cwebp, caesiumclt) at a time: each one next to the JVM
 * fills most of the 512 MB instance (ADR 0002, MLS-108).
 */
@Service
class NativeWorkPermit : Semaphore(1)
