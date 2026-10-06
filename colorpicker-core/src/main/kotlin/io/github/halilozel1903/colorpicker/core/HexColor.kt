package io.github.halilozel1903.colorpicker.core

/** Why a hex string is or is not a color. */
public enum class HexValidation {
    /** `#RGB`, `#RRGGBB` or `#AARRGGBB`, with or without `#`, any case. */
    Valid,

    /** Nothing but whitespace or a `#`. */
    Empty,

    /** Contains a character other than 0-9, a-f, A-F (after an optional leading `#`). */
    InvalidCharacter,

    /** Hex digits only, but not 3, 6 or 8 of them. */
    InvalidLength,
}

/** Parses, validates and formats hex colors: `#RGB`, `#RRGGBB` and `#AARRGGBB` (alpha first, like Android). */
public object HexColor {

    private const val HEX_DIGITS = "0123456789abcdefABCDEF"

    /** The digit counts [parse] accepts. */
    public val validLengths: Set<Int> = setOf(3, 6, 8)

    /** Checks [text] and says why it is not a color. Leading and trailing whitespace and one leading `#` are ignored. */
    public fun validate(text: String): HexValidation {
        val digits = digitsOf(text)
        return when {
            digits.isEmpty() -> HexValidation.Empty
            digits.any { it !in HEX_DIGITS } -> HexValidation.InvalidCharacter
            digits.length !in validLengths -> HexValidation.InvalidLength
            else -> HexValidation.Valid
        }
    }

    /** True when [parse] returns a color for [text]. */
    public fun isValid(text: String): Boolean = validate(text) == HexValidation.Valid

    /** True when [text] is a valid `#AARRGGBB` string, so it carries its own alpha. */
    public fun hasAlpha(text: String): Boolean = isValid(text) && digitsOf(text).length == 8

    /**
     * Parses `#RGB`, `#RRGGBB` or `#AARRGGBB` (the `#` is optional, any case) to ARGB, or returns null.
     * Three and six digit colors are opaque.
     */
    public fun parse(text: String): Int? {
        if (!isValid(text)) return null
        val digits = digitsOf(text)
        return when (digits.length) {
            3 -> {
                val expanded = buildString(6) { digits.forEach { append(it).append(it) } }
                (0xFF000000L or expanded.toLong(16)).toInt()
            }
            6 -> (0xFF000000L or digits.toLong(16)).toInt()
            else -> digits.toLong(16).toInt()
        }
    }

    /**
     * Formats [argb] as `#RRGGBB`, or `#AARRGGBB` when [includeAlpha] (by default: when the color is not opaque).
     */
    public fun format(
        argb: Int,
        includeAlpha: Boolean = ColorMath.alpha(argb) != 255,
        withHash: Boolean = true,
        uppercase: Boolean = true,
    ): String {
        val digits = if (includeAlpha) {
            (argb.toLong() and 0xFFFFFFFFL).toString(16).padStart(8, '0')
        } else {
            (argb and 0xFFFFFF).toString(16).padStart(6, '0')
        }
        val cased = if (uppercase) digits.uppercase() else digits
        return if (withHash) "#$cased" else cased
    }

    /**
     * Cleans user input for a hex field: drops `#`, whitespace and any non hex character, upper cases the rest
     * and keeps at most [maxLength] digits.
     */
    public fun sanitize(input: String, maxLength: Int = 8): String =
        input.filter { it in HEX_DIGITS }.uppercase().take(maxLength)

    private fun digitsOf(text: String): String = text.trim().removePrefix("#")
}
