package rs.sljivicbusiness.jetbrainshackathon.regex
object RegexUtils {

    public fun tryUnescapeString(regex: String): String {
        var unescapedRegex = regex
        var escaped = true

        // check whether the string is already unescaped, e.g. if it's a python or JS raw string

        // Python raw string: r"..." or r'...'
        if (regex.startsWith("r\"") && regex.endsWith("\"")) {
            unescapedRegex = unescapedRegex.substring(2, unescapedRegex.length - 1)
            escaped = false
        } else if (regex.startsWith("r'") && regex.endsWith("'")) {
            unescapedRegex = unescapedRegex.substring(2, unescapedRegex.length - 1)
            escaped = false
        }
        // JavaScript raw string: /.../
        else if (regex.startsWith("/") && regex.endsWith("/")) {
            unescapedRegex = unescapedRegex.substring(1, unescapedRegex.length - 1)
            escaped = false
        } else if ((regex.startsWith("\"") && regex.endsWith("\"")) ||
            (regex.startsWith("'") && regex.endsWith("'")) ||
            (regex.startsWith("`") && regex.endsWith("`"))
        ) {
            unescapedRegex = unescapedRegex.substring(1, unescapedRegex.length - 1)
        }

        if (escaped) {
            unescapedRegex = unescapedRegex.replace("\\^", "^")
            unescapedRegex = unescapedRegex.replace("\\$", "$")
            unescapedRegex = unescapedRegex.replace("\\.", ".")
            unescapedRegex = unescapedRegex.replace("\\*", "*")
            unescapedRegex = unescapedRegex.replace("\\+", "+")
            unescapedRegex = unescapedRegex.replace("\\?", "?")
            unescapedRegex = unescapedRegex.replace("\\[", "[")
            unescapedRegex = unescapedRegex.replace("\\]", "]")
            unescapedRegex = unescapedRegex.replace("\\(", "(")
            unescapedRegex = unescapedRegex.replace("\\)", ")")
            unescapedRegex = unescapedRegex.replace("\\{", "{")
            unescapedRegex = unescapedRegex.replace("\\}", "}")
            unescapedRegex = unescapedRegex.replace("\\|", "|")
            unescapedRegex = unescapedRegex.replace("\\\\", "\\")
        }
        return unescapedRegex
    }

    public fun looksLikeRegex(text: String): Boolean =
        text.contains("\\d") || text.contains("\\w") || text.contains("\\s") ||
                text.contains(")[") || text.startsWith("^") || text.endsWith("$") ||
                text.contains("]{") || text.contains(".*")
}
