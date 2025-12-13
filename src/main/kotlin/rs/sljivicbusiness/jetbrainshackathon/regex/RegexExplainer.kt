package rs.sljivicbusiness.jetbrainshackathon.regex

object RegexExplainer {

    fun explain(tokens: List<RegexToken>, depth: Int = 0): List<String> =
        tokens.map { token ->
            // html indent
            val indent = "&nbsp;".repeat((depth + if (token is RegexToken.Quantifier) 1 else 0) * 4)
            when (token) {
                is RegexToken.StartAnchor -> "$indent^  Start of the string"
                is RegexToken.EndAnchor -> "$indent$  End of the string"
                is RegexToken.DigitClass -> "$indent\\d  A digit (0–9)"
                is RegexToken.WordClass -> "$indent\\w  A word character (a-z, A-Z, 0-9, _)"
                is RegexToken.WhitespaceClass -> "$indent\\s  A whitespace character (space, tab, newline)"
                is RegexToken.CharClass -> when (token.value) {
                    "." -> "$indent.  Any character (wildcard)"
                    else -> "$indent${token.value}  Character class"
                }
                is RegexToken.Quantifier -> when {
                    token.min == 0 && token.max == null -> "$indent*  Zero or more times"
                    token.min == 1 && token.max == null -> "$indent+  One or more times"
                    token.min == 0 && token.max == 1 -> "$indent?  Zero or one time (optional)"
                    token.min == token.max -> "$indent{${token.min}}  Exactly ${token.min} time(s)"
                    token.max == null -> "$indent{${token.min},}  At least ${token.min} time(s)"
                    else -> "$indent{${token.min},${token.max}}  Between ${token.min} and ${token.max} times"
                }
                is RegexToken.Group -> {
                    val innerExplanation = explain(token.tokens, depth + 1).joinToString("<br>")
                    "$indent( ------------ depth $depth<br>$innerExplanation<br>$indent) ------------- depth $depth"
                }
                is RegexToken.Literal -> "$indent${token.value}  Literal character"
            }
        }
}
