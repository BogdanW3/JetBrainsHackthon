package rs.sljivicbusiness.reggin.regex

object RegexExplainer {

    fun explain(
        tokens: List<RegexToken>,
        depth: Int = 0,
        html: Boolean = true
    ): List<String> =
        tokens.map { token ->
            val ctx = FormatContext(depth, html)
            explainToken(token, ctx)
        }

    private fun explainToken(token: RegexToken, ctx: FormatContext): String =
        when (token) {
            is RegexToken.StartAnchor ->
                ctx.line("^  Start of the string")

            is RegexToken.EndAnchor ->
                ctx.line("$  End of the string")

            is RegexToken.DigitClass ->
                ctx.line("\\d  Digit (0–9)")

            is RegexToken.NotDigitClass ->
                ctx.line("\\D  Non-digit character")

            is RegexToken.WordClass ->
                ctx.line("\\w  Word character (a-z, A-Z, 0-9, _)")

            is RegexToken.NotWordClass ->
                ctx.line("\\W  Non-word character")

            is RegexToken.WhitespaceClass ->
                ctx.line("\\s  Whitespace character (space, tab, newline)")

            is RegexToken.NotWhitespaceClass ->
                ctx.line("\\S  Non-whitespace character")

            is RegexToken.CharClass ->
                ctx.line("${token.value}  Character class (matches any character in the set)")

            is RegexToken.NegatedCharClass ->
                ctx.line("${token.value}  Negated character class (matches any character not in the set)")

            is RegexToken.Literal ->
                ctx.line("${token.value}  Literal character")

            is RegexToken.Quantifier ->
                explainQuantifier(token, ctx)

            is RegexToken.Group ->
                explainGroup(token, ctx)

            is RegexToken.Alternative ->
                explainAlternative(token, ctx)
        }

    private fun explainQuantifier(token: RegexToken.Quantifier, ctx: FormatContext): String =
        ctx.indentedLine(
            when {
                token.lazy && token.min == 0 && token.max == null ->
                    "*?  Zero or more times (as few times as possible)"

                token.lazy && token.min == 1 && token.max == null ->
                    "+?  One or more times (as few times as possible)"

                token.lazy && token.min == 0 && token.max == 1 ->
                    "??  Zero or one time (as few times as possible)"

                token.lazy ->
                    "{${quantifierSuffix(token)}}?  Lazy quantifier"

                token.min == 0 && token.max == null ->
                    "*  Zero or more times"

                token.min == 1 && token.max == null ->
                    "+  One or more times"

                token.min == 0 && token.max == 1 ->
                    "?  Zero or one time (optional)"

                token.min == token.max ->
                    "{${token.min}}  Exactly ${token.min} time(s)"

                token.max == null ->
                    "{${token.min},}  At least ${token.min} time(s)"

                else ->
                    "{${token.min},${token.max}}  Between ${token.min} and ${token.max} times"
            }
        )

    private fun quantifierSuffix(token: RegexToken.Quantifier): String =
        when {
            token.max == null -> "${token.min},"
            token.min == token.max -> "${token.min}"
            else -> "${token.min},${token.max}"
        }

    private fun explainGroup(token: RegexToken.Group, ctx: FormatContext): String {
        val inner = explain(token.tokens, ctx.depth + 1, ctx.html)
            .joinToString(ctx.newline)

        return buildString {
            append(ctx.line("( ------------ depth ${ctx.depth}"))
            append(ctx.newline)
            append(inner)
            append(ctx.newline)
            append(ctx.line(") ------------ depth ${ctx.depth}"))
        }
    }

    private fun explainAlternative(token: RegexToken.Alternative, ctx: FormatContext): String {
        val inner = token.args
            .flatMap { explain(listOf(it), ctx.depth + 1, ctx.html) }
            .joinToString(ctx.newline)

        return buildString {
            append(ctx.line("|  OR depth ${ctx.depth}"))
            append(ctx.newline)
            append(inner)
            append(ctx.newline)
            append(ctx.line("| End of OR depth ${ctx.depth}"))
        }
    }

    private data class FormatContext(
        val depth: Int,
        val html: Boolean
    ) {
        private val space: String =
            if (html) "&nbsp;".repeat(4) else "    "

        val newline: String =
            if (html) "<br>" else "\n"

        fun line(text: String): String =
            "${space.repeat(depth)}$text"

        fun indentedLine(text: String): String =
            "${space.repeat(depth + 1)}$text"
    }
}
