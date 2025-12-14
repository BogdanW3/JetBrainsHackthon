package rs.sljivicbusiness.reggin.regex

object RegexTokenizer {

    fun tokenize(input: String, skipUnescape: Boolean = false): List<RegexToken> {
        val regex = if (skipUnescape) input else RegexUtils.tryUnescapeString(input)

        splitTopLevelAlternation(regex)?.let { parts ->
            val tokens = parts.map { part ->
                val inner = tokenize(part, skipUnescape = true)
                if (inner.size == 1) inner.first() else RegexToken.Group(inner)
            }
            return listOf(RegexToken.Alternative(tokens))
        }

        val tokens = mutableListOf<RegexToken>()
        var index = 0

        while (index < regex.length) {
            index = parseToken(regex, index, tokens)
        }

        return tokens
    }

    private fun splitTopLevelAlternation(regex: String): List<String>? {
        val parts = mutableListOf<String>()
        var depth = 0
        var lastSplit = 0

        regex.forEachIndexed { i, c ->
            when (c) {
                '(' -> depth++
                ')' -> if (depth > 0) depth--
                '|' -> if (depth == 0) {
                    parts.add(regex.substring(lastSplit, i))
                    lastSplit = i + 1
                }
            }
        }

        if (parts.isEmpty()) return null

        parts.add(regex.substring(lastSplit))
        return parts
    }

    private fun parseToken(
        regex: String,
        startIndex: Int,
        tokens: MutableList<RegexToken>
    ): Int {
        var i = startIndex
        val c = regex[i]

        when {
            c == '^' -> tokens.add(RegexToken.StartAnchor)
            c == '$' -> tokens.add(RegexToken.EndAnchor)

            c == '\\' && i + 1 < regex.length -> {
                i = parseEscape(regex, i, tokens)
            }

            c == '[' -> {
                i = parseCharClass(regex, i, tokens)
            }

            c == '(' -> {
                parseGroup(regex, i)?.let {
                    tokens.add(it.first)
                    i = it.second
                } ?: tokens.add(RegexToken.Literal(c.toString()))
            }

            c == '*' || c == '+' || c == '?' -> {
                i = parseSimpleQuantifier(regex, i, tokens)
            }

            c == '{' -> {
                parseRangeQuantifier(regex, i)?.let {
                    tokens.add(it.first)
                    i = it.second
                } ?: tokens.add(RegexToken.Literal(c.toString()))
            }

            c == '.' -> tokens.add(RegexToken.CharClass("."))

            else -> tokens.add(RegexToken.Literal(c.toString()))
        }

        return i + 1
    }

    private fun parseEscape(
        regex: String,
        index: Int,
        tokens: MutableList<RegexToken>
    ): Int {
        val next = regex[index + 1]
        when (next) {
            'd' -> tokens.add(RegexToken.DigitClass)
            'D' -> tokens.add(RegexToken.NotDigitClass)
            'w' -> tokens.add(RegexToken.WordClass)
            'W' -> tokens.add(RegexToken.NotWordClass)
            's' -> tokens.add(RegexToken.WhitespaceClass)
            'S' -> tokens.add(RegexToken.NotWhitespaceClass)
            else -> tokens.add(RegexToken.Literal(next.toString()))
        }
        return index + 1
    }

    private fun parseCharClass(
        regex: String,
        startIndex: Int,
        tokens: MutableList<RegexToken>
    ): Int {
        val end = regex.indexOf(']', startIndex)
        if (end <= startIndex) {
            tokens.add(RegexToken.Literal("["))
            return startIndex
        }

        val content = regex.substring(startIndex, end + 1)
        tokens.add(
            if (content.startsWith("[^"))
                RegexToken.NegatedCharClass(content)
            else
                RegexToken.CharClass(content)
        )

        return end
    }

    private fun parseGroup(regex: String, startIndex: Int): Pair<RegexToken, Int>? {
        var depth = 0
        var i = startIndex

        while (i < regex.length) {
            if (regex[i] == '(') depth++
            if (regex[i] == ')') {
                depth--
                if (depth == 0) {
                    val content = regex.substring(startIndex + 1, i)
                    val tokens = tokenize(content, skipUnescape = true)
                    return RegexToken.Group(tokens) to i
                }
            }
            i++
        }
        return null
    }

    private fun parseSimpleQuantifier(
        regex: String,
        index: Int,
        tokens: MutableList<RegexToken>
    ): Int {
        if (tokens.isEmpty()) {
            tokens.add(RegexToken.Literal(regex[index].toString()))
            return index
        }

        val lazy = index + 1 < regex.length && regex[index + 1] == '?'
        val token = when (regex[index]) {
            '*' -> RegexToken.Quantifier(0, null, lazy)
            '+' -> RegexToken.Quantifier(1, null, lazy)
            '?' -> RegexToken.Quantifier(0, 1, lazy)
            else -> return index
        }

        tokens.add(token)
        return if (lazy) index + 1 else index
    }

    private fun parseRangeQuantifier(
        regex: String,
        startIndex: Int
    ): Pair<RegexToken.Quantifier, Int>? {
        val close = regex.indexOf('}', startIndex)
        if (close == -1) return null

        val content = regex.substring(startIndex + 1, close)
        var endIndex = close
        val lazy = close + 1 < regex.length && regex[close + 1] == '?'
        if (lazy) endIndex++

        return try {
            val quantifier = if (content.contains(',')) {
                val (minStr, maxStr) = content.split(',', limit = 2)
                val min = minStr.trim().toInt()
                val max = maxStr.trim().takeIf { it.isNotEmpty() }?.toInt()
                RegexToken.Quantifier(min, max, lazy)
            } else {
                val exact = content.trim().toInt()
                RegexToken.Quantifier(exact, exact, lazy)
            }
            quantifier to endIndex
        } catch (_: Exception) {
            null
        }
    }
}
