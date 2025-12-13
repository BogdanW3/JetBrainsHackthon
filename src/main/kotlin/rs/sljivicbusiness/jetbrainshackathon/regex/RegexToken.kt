package rs.sljivicbusiness.jetbrainshackathon.regex

sealed class RegexToken {
    data class Literal(val value: String) : RegexToken()
    object StartAnchor : RegexToken()
    object EndAnchor : RegexToken()
    object DigitClass : RegexToken()       // \d
    object WordClass : RegexToken()        // \w
    object WhitespaceClass : RegexToken()  // \s
    data class Alternative(val args: List<RegexToken>) : RegexToken()    // |
    data class CharClass(val value: String) : RegexToken()
    data class Quantifier(val min: Int, val max: Int?) : RegexToken()
    data class Group(val tokens: List<RegexToken>) : RegexToken()
}