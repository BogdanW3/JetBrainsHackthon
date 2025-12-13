package rs.sljivicbusiness.jetbrainshackathon.regex

sealed class RegexToken {
    data class Literal(val value: String) : RegexToken()
    object StartAnchor : RegexToken()
    object EndAnchor : RegexToken()
    object DigitClass : RegexToken()       // \d
    object NotDigitClass : RegexToken()    // \D
    object NotWordClass : RegexToken()    // \W
    object WordClass : RegexToken()        // \w
    object WhitespaceClass : RegexToken()  // \s
    object NotWhitespaceClass : RegexToken()  // \S
    data class Alternative(val args: List<RegexToken>) : RegexToken()    // |
    data class CharClass(val value: String) : RegexToken()
    data class NegatedCharClass(val value: String) : RegexToken()  // [^...] - matches any character NOT in the set
    data class Quantifier(val min: Int, val max: Int?, val lazy: Boolean = false) : RegexToken()
    data class Group(val tokens: List<RegexToken>) : RegexToken()
}