package rs.sljivicbusiness.jetbrainshackathon.regex

object RegexTokenizer {

    fun tokenize(regex: String): List<RegexToken> {
        // M1 scope: linear scan, no nesting beyond groups
        val tokens = mutableListOf<RegexToken>()
        var i = 0

        while (i < regex.length) {
            when {
                regex[i] == '^' -> tokens.add(RegexToken.StartAnchor)
                regex[i] == '$' -> tokens.add(RegexToken.EndAnchor)
                regex.startsWith("\\d", i) -> {
                    tokens.add(RegexToken.DigitClass)
                    i++
                }
                regex[i] == '[' -> {
                    val end = regex.indexOf(']', i)
                    if (end > i) {
                        tokens.add(
                            RegexToken.CharClass(regex.substring(i, end + 1))
                        )
                        i = end
                    }
                }
                else -> tokens.add(
                    RegexToken.Literal(regex[i].toString())
                )
            }
            i++
        }
        return tokens
    }
}