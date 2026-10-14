package com.swordfish.lemuroid.corruptor

import kotlin.random.Random

object RomCorruptor {

    const val NIGHTMARE_TILT = 0
    const val NIGHTMARE_RANDOM = 1
    const val BITWISE = 2
    const val BITSHIFT = 3
    const val LERP = 4
    const val MERGE = 5
    const val BLASTUNIT = 6

    val ALGORITHM_NAMES = listOf(
        "Nightmare Tilt",
        "Nightmare Random",
        "Bitwise Logic",
        "Bit Shift / Rotate",
        "Lerp Gradient",
        "Merge / Cross-ROM",
        "BlastUnit Freeze",
    )

    const val UNKNOWN = 0
    const val NES = 1
    const val SNES = 2
    const val GB = 3
    const val GBC = 4
    const val GBA = 5

    @Volatile var pristine: ByteArray? = null
        private set
    @Volatile var mutated: ByteArray? = null
        private set
    @Volatile var platform: Int = UNKNOWN
        private set

    var enabled: Boolean = false
    var algorithm: Int = NIGHTMARE_TILT
    var intensity: Int = 20
    var stride: Int = 1
    var startOffset: Int = 0
    var endOffset: Int = 0
    var headerProtect: Boolean = true

    var lastCorruptMs: Long = 0
        private set

    fun platformName(): String = when (platform) {
        NES -> "NES"
        SNES -> "SNES"
        GB -> "Game Boy"
        GBC -> "Game Boy Color"
        GBA -> "Game Boy Advance"
        else -> "Unknown"
    }

    fun setSource(bytes: ByteArray) {
        pristine = bytes.copyOf()
        endOffset = bytes.size
        platform = detectPlatform(bytes)
    }

    fun corrupt() {
        val src = pristine ?: return
        val out = src.copyOf()
        val t0 = System.currentTimeMillis()
        when (algorithm) {
            NIGHTMARE_TILT -> nightmareTilt(out)
            NIGHTMARE_RANDOM -> nightmareRandom(out)
            BITWISE -> bitwise(out)
            BITSHIFT -> bitShift(out)
            LERP -> lerp(out)
            MERGE -> nightmareTilt(out)
            BLASTUNIT -> blastUnit(out)
        }
        lastCorruptMs = System.currentTimeMillis() - t0
        mutated = out
    }

    private fun nightmareTilt(b: ByteArray) {
        forEachTarget(b) { i ->
            val k = (intensity % 8) + 1
            b[i] = (b[i] + k).toByte()
        }
    }

    private fun nightmareRandom(b: ByteArray) {
        forEachTarget(b) { i -> b[i] = Random.nextInt(256).toByte() }
    }

    private fun bitwise(b: ByteArray) {
        val key = 0x20
        forEachTarget(b) { i -> b[i] = (b[i].toInt() xor key).toByte() }
    }

    private fun bitShift(b: ByteArray) {
        forEachTarget(b) { i ->
            val v = b[i].toInt() and 0xFF
            b[i] = (((v shl 1) or (v ushr 7)) and 0xFF).toByte()
        }
    }

    private fun lerp(b: ByteArray) {
        forEachTarget(b) { i ->
            val v = b[i].toInt() and 0xFF
            b[i] = (v + (0xFF - v) / 4).toByte()
        }
    }

    private fun blastUnit(b: ByteArray) {
        forEachTarget(b) { i -> b[i] = 0xFF.toByte() }
    }

    private inline fun forEachTarget(b: ByteArray, action: (Int) -> Unit) {
        val end = minOf(endOffset, b.size)
        var i = startOffset.coerceAtLeast(0)
        while (i < end) {
            if (!isProtected(i, b.size) && Random.nextInt(100) < intensity) {
                action(i)
            }
            i += stride.coerceAtLeast(1)
        }
    }

    private fun detectPlatform(b: ByteArray): Int {
        if (b.size >= 16 &&
            b[0] == 0x4E.toByte() && b[1] == 0x45.toByte() &&
            b[2] == 0x53.toByte() && b[3] == 0x1A.toByte()
        ) return NES

        if (b.size > 0xB2 && b[0xB2] == 0x96.toByte()) return GBA

        if (b.size >= 0x150 &&
            b[0x104] == 0xCE.toByte() && b[0x105] == 0xED.toByte()
        ) {
            val cgb = b[0x143].toInt() and 0xFF
            return if (cgb == 0x80 || cgb == 0xC0) GBC else GB
        }

        if (b.size % 1024 == 512) return SNES
        return UNKNOWN
    }

    private fun isProtected(offset: Int, size: Int): Boolean {
        if (!headerProtect) return false
        return when (platform) {
            NES -> offset < 0x10
            SNES -> offset >= size - 0x40
            GB, GBC -> offset < 0x60 || (offset in 0x100 until 0x150)
            GBA -> offset < 0xC0
            else -> false
        }
    }
}
