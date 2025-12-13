package rs.sljivicbusiness.jetbrainshackathon.regex

object RegexExplainer {

    fun explain(tokens: List<RegexToken>): List<String> =
        tokens.map { token ->
            when (token) {
                is RegexToken.StartAnchor -> "^  Start of the string"
                is RegexToken.EndAnchor -> "$  End of the string"
                is RegexToken.DigitClass -> "\\d  A digit (0–9)"
                is RegexToken.WordClass -> "\\w  A word character (a-z, A-Z, 0-9, _)"
                is RegexToken.WhitespaceClass -> "\\s  A whitespace character (space, tab, newline)"
                is RegexToken.CharClass -> when (token.value) {
                    "." -> ".  Any character (wildcard)"
                    else -> "${token.value}  Character class"
                }
                is RegexToken.Quantifier -> when {
                    token.min == 0 && token.max == null -> "*  Zero or more times"
                    token.min == 1 && token.max == null -> "+  One or more times"
                    token.min == 0 && token.max == 1 -> "?  Zero or one time (optional)"
                    token.min == token.max -> "{${token.min}}  Exactly ${token.min} time(s)"
                    token.max == null -> "{${token.min},}  At least ${token.min} time(s)"
                    else -> "{${token.min},${token.max}}  Between ${token.min} and ${token.max} times"
                }
                is RegexToken.Group -> "(...)  Group with ${token.tokens.size} token(s)"
                is RegexToken.Literal -> "${token.value}  Literal character"
            }
        }
}