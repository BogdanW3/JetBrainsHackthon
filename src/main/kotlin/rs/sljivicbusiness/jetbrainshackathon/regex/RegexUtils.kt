package rs.sljivicbusiness.jetbrainshackathon.regex

object RegexUtils {

    fun tryUnescapeString(regex: String): String {
        val (stripped, escaped) = stripDelimiters(regex)

        if (!escaped) return stripped

        return UNESCAPE_MAP.entries.fold(stripped) { acc, (escapedChar, char) ->
            acc.replace(escapedChar, char)
        }
    }

    fun looksLikeRegex(text: String): Boolean =
        REGEX_HEURISTICS.any { text.contains(it) } ||
                text.startsWith("^") ||
                text.endsWith("$")

    private fun stripDelimiters(input: String): Pair<String, Boolean> =
        when {
            // Python raw string: r"..." or r'...'
            input.startsWith("r\"") && input.endsWith("\"") ->
                input.substring(2, input.length - 1) to false

            input.startsWith("r'") && input.endsWith("'") ->
                input.substring(2, input.length - 1) to false

            // JavaScript regex literal: /.../
            input.startsWith("/") && input.endsWith("/") ->
                input.substring(1, input.length - 1) to false

            // Quoted strings: "...", '...', `...`
            (input.startsWith("\"") && input.endsWith("\"")) ||
                    (input.startsWith("'") && input.endsWith("'")) ||
                    (input.startsWith("`") && input.endsWith("`")) ->
                input.substring(1, input.length - 1) to true

            else ->
                input to true
        }

    private val UNESCAPE_MAP = mapOf(
        "\\^" to "^",
        "\\$" to "$",
        "\\." to ".",
        "\\*" to "*",
        "\\+" to "+",
        "\\?" to "?",
        "\\[" to "[",
        "\\]" to "]",
        "\\(" to "(",
        "\\)" to ")",
        "\\{" to "{",
        "\\}" to "}",
        "\\|" to "|",
        "\\\\" to "\\"
    )

    private val REGEX_HEURISTICS = listOf(
        "\\d",
        "\\w",
        "\\s",
        ".*",
        "]{",
        ")["
    )
}
