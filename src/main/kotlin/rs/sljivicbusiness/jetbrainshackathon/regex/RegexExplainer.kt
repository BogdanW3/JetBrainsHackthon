package rs.sljivicbusiness.jetbrainshackathon.regex

object RegexExplainer {

    fun explain(tokens: List<RegexToken>): List<String> =
        tokens.map { token ->
            when (token) {
                is RegexToken.StartAnchor -> "^  Start of the string"
                is RegexToken.EndAnchor -> "$  End of the string"
                is RegexToken.DigitClass -> "\\d  A digit (0–9)"
                is RegexToken.CharClass -> "${token.value}  Character class"
                is RegexToken.Literal -> "${token.value}  Literal character"
                else -> "Unsupported token"
            }
        }
}