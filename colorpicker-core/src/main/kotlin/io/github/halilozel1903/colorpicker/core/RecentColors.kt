package io.github.halilozel1903.colorpicker.core

/**
 * An immutable list of recently used colors, most recent first, without duplicates and at most [maxSize] long.
 *
 * ```
 * var recent = RecentColors(maxSize = 8)
 * recent = recent.add(0xFF6750A4.toInt())   // moves an existing color to the front instead of duplicating it
 * ```
 */
public class RecentColors(colors: List<Int> = emptyList(), public val maxSize: Int = DEFAULT_MAX_SIZE) {

    init {
        require(maxSize > 0) { "maxSize must be positive, was $maxSize" }
    }

    /** The colors as ARGB, most recent first. */
    public val colors: List<Int> = colors.distinct().take(maxSize)

    public val size: Int get() = colors.size

    public fun isEmpty(): Boolean = colors.isEmpty()

    public operator fun contains(argb: Int): Boolean = argb in colors

    /** Puts [argb] first, removing an earlier copy, and drops the oldest color when the list is full. */
    public fun add(argb: Int): RecentColors = RecentColors(listOf(argb) + colors.filter { it != argb }, maxSize)

    /** Adds [argbs] in order, so the last one ends up first. */
    public fun addAll(argbs: Iterable<Int>): RecentColors = argbs.fold(this) { recent, argb -> recent.add(argb) }

    /** Removes [argb] if present. */
    public fun remove(argb: Int): RecentColors = RecentColors(colors.filter { it != argb }, maxSize)

    /** An empty list with the same [maxSize]. */
    public fun clear(): RecentColors = RecentColors(emptyList(), maxSize)

    /** The same colors with a different [maxSize], trimming the oldest ones if needed. */
    public fun withMaxSize(maxSize: Int): RecentColors = RecentColors(colors, maxSize)

    override fun equals(other: Any?): Boolean =
        other is RecentColors && other.colors == colors && other.maxSize == maxSize

    override fun hashCode(): Int = 31 * colors.hashCode() + maxSize

    override fun toString(): String =
        "RecentColors(${colors.joinToString { HexColor.format(it) }}, maxSize=$maxSize)"

    public companion object {
        public const val DEFAULT_MAX_SIZE: Int = 12
    }
}
