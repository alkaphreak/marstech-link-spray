---
status: proposed
date: 2026-11-04
ticket: https://marstech.myjetbrains.com/youtrack/issue/MLS-215
---

# Compress images with external CLI encoders (cwebp, caesiumclt), capped by pixel count

## Context and Problem Statement

sol4.space stores uploaded images (`/api/images`, MLS-203) but never compresses them. Four tickets
ask for compression (MLS-108, MLS-118, MLS-192, MLS-215), each with its own codec list. Which codecs
and libraries should MLS use, given that it runs in a 512 MB DigitalOcean container where the JVM
already takes about 460 MB (`-XX:MaxRAMPercentage=45` heap plus about 230 MB off-heap, see the
`Dockerfile`)?

## Considered Options

- External CLI encoders called through `ProcessBuilder`, as `OcrServiceImpl` already does with Tesseract
- JVM libraries wrapping native code (webp-imageio, JNI bindings for AVIF or JPEG XL)
- Pure Java ImageIO re-encoding (JPEG quality, PNG)

## Decision Outcome

Chosen option: "External CLI encoders", because they reuse a pattern already in production, keep
native memory out of the JVM process, and their memory grows with the pixel count, so a per-encoder
pixel cap keeps them inside the container budget.

| Format       | Encoder                                   | Install                                      | Verdict  |
|--------------|-------------------------------------------|----------------------------------------------|----------|
| WebP         | `cwebp` 1.2.2                             | `apt-get install webp` (jammy)               | Adopt    |
| JPEG, PNG    | `caesiumclt` 1.5.0 (libcaesium, MLS-118)  | static musl binary from GitHub releases      | Adopt    |
| PNG lossless | `oxipng` 10.2.1                           | static musl binary from GitHub releases      | Reject   |
| AVIF         | `avifenc` 0.9.3                           | `apt-get install libavif-bin` (jammy)        | Defer    |
| JPEG XL      | none stable                               | not packaged for jammy                       | Out      |
| WebP2        | none                                      | experimental upstream                        | Out      |

Rules for the implementation ticket:

- Cap inputs per encoder, reading the dimensions from the image header without decoding the pixels.
  The JVM can reach about 460 MB, leaving about 50 MB for a subprocess, which shares the container's
  memory limit. Provisional caps from the encoder-only figures below: `cwebp` 8 megapixels (36 MB at
  7.2 MP), `caesiumclt` 3 megapixels (32 MB at 2.8 MP, 44 MB at 4 MP, 73 MB at 7.2 MP).
- Confirm or lower these caps with a combined measurement (the application running in a 512 MB
  container plus one encode) before enabling the endpoint: a timeout does not protect the JVM from a
  container OOM kill.
- Share one permit between compression and OCR (`OcrServiceImpl` holds its own `Semaphore(1)`,
  80 to 110 MB for Tesseract plus PDF rendering): no two native workloads may run at the same time.
- Kill the encoder process after a timeout, and return the original image when it fails.

### Consequences

- Good: one pattern (`ProcessBuilder`, binaries in the `Dockerfile`) for OCR and compression, no
  native code inside the JVM, and caesiumclt covers MLS-118 without a Rust build.
- Bad: about 50 MB of headroom is left in the container, so JPEG and PNG optimization is limited to
  small images, and large photos and AVIF wait for the instance sizing decision of MLS-238.
- Bad: two binaries are downloaded at image build time and must be pinned and updated by hand.

### Measurements

2026-11-04, `eclipse-temurin:25-jre-jammy`, `--memory 512m --cpus 1`, linux/amd64. Settings close to
Squoosh defaults: `cwebp -q 75`, `avifenc -s 6 --min 20 --max 40`, `caesiumclt -q 80`, `oxipng -o 2`.
Peak memory is the encoder process alone, without the JVM.

| Input                          | Encoder    | Output    | Time   | Peak memory      |
|--------------------------------|------------|-----------|--------|------------------|
| Kodak kodim23 PNG, 545 KB      | cwebp      | 23 KB     | 0.1 s  | 5 MB             |
|                                | avifenc    | 17 KB     | 0.6 s  | 29 MB            |
|                                | caesiumclt | 180 KB    | 0.4 s  | 19 MB            |
|                                | oxipng     | 535 KB    | 0.8 s  | 12 MB            |
| UI icon PNG 512 px, 26 KB      | cwebp      | 6 KB      | 0.1 s  | 10 MB            |
|                                | caesiumclt | 5 KB      | 0.1 s  | 11 MB            |
|                                | oxipng     | 13 KB     | 0.3 s  | 9 MB             |
| Photo JPEG 2.8 MP, 871 KB      | cwebp      | 192 KB    | 0.6 s  | 16 MB            |
|                                | caesiumclt | 307 KB    | 1.4 s  | 32 MB            |
| Photo JPEG 4.0 MP, 1.4 MB      | cwebp      | 337 KB    | 1.0 s  | 22 MB            |
|                                | caesiumclt | 479 KB    | 3.3 s  | 44 MB            |
| Photo JPEG 7.2 MP, 2.2 MB      | cwebp      | 487 KB    | 0.9 s  | 36 MB            |
|                                | avifenc    | 315 KB    | 5.5 s  | 157 MB           |
|                                | caesiumclt | 766 KB    | 1.3 s  | 73 MB            |
| Photo JPEG 46 MP, 14 MB        | cwebp      | 2.0 MB    | 7.5 s  | 214 MB           |
|                                | avifenc    | killed    | 1.0 s  | over 509 MB      |
|                                | caesiumclt | 3.7 MB    | 10.6 s | 433 MB           |

## Pros and Cons of the Options

### JVM libraries wrapping native code

- Good: no process to spawn per request.
- Bad: webp-imageio, the usual WebP binding, has been archived since June 2023, and the AVIF and
  JPEG XL bindings are experimental.
- Bad: native memory is allocated inside the JVM process, so an encoder spike kills the whole service.

### Pure Java ImageIO re-encoding

- Good: no extra binary.
- Bad: no WebP or AVIF writer, and decoding into a `BufferedImage` puts the full pixel buffer on the
  heap (about 180 MB for 46 MP), which the 230 MB heap cannot absorb.

### oxipng (rejected)

- Bad: 2 % gain on a photographic PNG and 50 % on an icon, where caesiumclt already reaches 80 %;
  a third binary is not worth it.

### AVIF (deferred)

- Good: smallest output on every input.
- Bad: 157 MB and 5.5 s for 7 MP on one CPU, and killed at 46 MP. Revisit once MLS-238 settles the
  instance size.
