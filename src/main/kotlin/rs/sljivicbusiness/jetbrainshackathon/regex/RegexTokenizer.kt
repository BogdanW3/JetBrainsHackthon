package rs.sljivicbusiness.jetbrainshackathon.regex

import rs.sljivicbusiness.jetbrainshackathon.regex.RegexUtils

object RegexTokenizer {

    fun tokenize(regex: String, skipUnescape: Boolean = false): List<RegexToken> {

        var regex = regex
        if (!skipUnescape) {
            regex = RegexUtils.tryUnescapeString(regex)
        }
        val tokens = mutableListOf<RegexToken>()
        var i = 0

        // First look for alternation across the whole string, then tokenize substrings

        val alternationParts = mutableListOf<String>()
        var lastSplit = 0
        var depth = 0
        while (i < regex.length) {
            when (regex[i]) {
                '(' -> depth++
                ')' -> if (depth > 0) depth--
                '|' -> if (depth == 0) {
                    alternationParts.add(regex.substring(lastSplit, i))
                    lastSplit = i + 1
                }
            }
            i++
        }
        alternationParts.add(regex.substring(lastSplit, regex.length))
        if (alternationParts.size > 1) {
            val alternationTokens = alternationParts.map { part ->
                val partTokens = tokenize(part, true)
                if (partTokens.size == 1) {
                    partTokens[0]
                } else {
                    RegexToken.Group(partTokens)
                }
            }
            tokens.add(RegexToken.Alternative(alternationTokens))
            return tokens
        }

        i = 0
        while (i < regex.length) {
            i = parseToken(regex, i, tokens)
        }

        return tokens
    }

    private fun parseToken(
        regex: String,
        i: Int,
        tokens: MutableList<RegexToken>
    ): Int {
        var i1 = i
        when {
            // Anchors
            regex[i1] == '^' -> tokens.add(RegexToken.StartAnchor)
            regex[i1] == '$' -> tokens.add(RegexToken.EndAnchor)

            // Escape sequences
            regex[i1] == '\\' && i1 + 1 < regex.length -> {
                when (regex[i1 + 1]) {
                    'd' -> {
                        tokens.add(RegexToken.DigitClass)
                        i1++
                    }

                    'D' -> {
                        tokens.add(RegexToken.NotDigitClass)
                        i1++
                    }

                    'w' -> {
                        tokens.add(RegexToken.WordClass)
                        i1++
                    }

                    'W' -> {
                        tokens.add(RegexToken.NotWordClass)
                        i1++
                    }

                    's' -> {
                        tokens.add(RegexToken.WhitespaceClass)
                        i1++
                    }

                    'S' -> {
                        tokens.add(RegexToken.NotWhitespaceClass)
                        i1++
                    }
                    // Escaped special characters
                    '.', '*', '+', '?', '[', ']', '(', ')', '{', '}', '^', '$', '|', '\\' -> {
                        tokens.add(RegexToken.Literal(regex[i1 + 1].toString()))
                        i1++
                    }

                    else -> {
                        // Other escape sequences treated as literals
                        tokens.add(RegexToken.Literal(regex[i1 + 1].toString()))
                        i1++
                    }
                }
            }

            // Character classes
            regex[i1] == '[' -> {
                val end = regex.indexOf(']', i1)
                if (end > i1) {
                    val charClassContent = regex.substring(i1, end + 1)
                    // Check if it's a negated character class [^...]
                    if (charClassContent.startsWith("[^")) {
                        tokens.add(RegexToken.NegatedCharClass(charClassContent))
                    } else {
                        tokens.add(RegexToken.CharClass(charClassContent))
                    }
                    i1 = end
                }
            }

            // Groups
            regex[i1] == '(' -> {
                val groupTokens = parseGroup(regex, i1)
                if (groupTokens != null) {
                    tokens.add(groupTokens.first)
                    i1 = groupTokens.second
                } else {
                    tokens.add(RegexToken.Literal(regex[i1].toString()))
                }
            }

            // Quantifiers (must come after a token)
            regex[i1] == '*' && tokens.isNotEmpty() -> {
                val lazy = i1 + 1 < regex.length && regex[i1 + 1] == '?'
                tokens.add(RegexToken.Quantifier(0, null, lazy))
                if (lazy) i1++
            }

            regex[i1] == '+' && tokens.isNotEmpty() -> {
                val lazy = i1 + 1 < regex.length && regex[i1 + 1] == '?'
                tokens.add(RegexToken.Quantifier(1, null, lazy))
                if (lazy) i1++
            }

            regex[i1] == '?' && tokens.isNotEmpty() -> {
                val lazy = i1 + 1 < regex.length && regex[i1 + 1] == '?'
                tokens.add(RegexToken.Quantifier(0, 1, lazy))
                if (lazy) i1++
            }

            regex[i1] == '{' -> {
                val quantifier = parseQuantifier(regex, i1)
                if (quantifier != null && tokens.isNotEmpty()) {
                    tokens.add(quantifier.first)
                    i1 = quantifier.second
                } else {
                    tokens.add(RegexToken.Literal(regex[i1].toString()))
                }
            }

            // Wildcards
            regex[i1] == '.' -> tokens.add(RegexToken.CharClass("."))

            // Literals
            else -> tokens.add(RegexToken.Literal(regex[i1].toString()))
        }
        i1++
        return i1
    }

    private fun parseGroup(regex: String, startIndex: Int): Pair<RegexToken, Int>? {
        var depth = 0
        var i = startIndex

        while (i < regex.length) {
            if (regex[i] == '(') depth++
            if (regex[i] == ')') {
                depth--
                if (depth == 0) {
                    // Found matching closing parenthesis
                    val groupContent = regex.substring(startIndex + 1, i)
                    val groupTokens = tokenize(groupContent, true)
                    return Pair(RegexToken.Group(groupTokens), i)
                }
            }
            i++
        }
        return null // No matching closing parenthesis
    }

    private fun parseQuantifier(regex: String, startIndex: Int): Pair<RegexToken.Quantifier, Int>? {
        val closeIndex = regex.indexOf('}', startIndex)
        if (closeIndex == -1) return null

        val content = regex.substring(startIndex + 1, closeIndex)
        var endIndex = closeIndex
        val lazy = if (closeIndex + 1 < regex.length && regex[closeIndex + 1] == '?') {
            endIndex++
            true
        } else false

        return try {
            when {
                content.contains(',') -> {
                    val parts = content.split(',')
                    val min = parts[0].trim().toIntOrNull() ?: return null
                    val max = parts[1].trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
                    Pair(RegexToken.Quantifier(min, max, lazy), endIndex)
                }
                else -> {
                    val exact = content.trim().toIntOrNull() ?: return null
                    Pair(RegexToken.Quantifier(exact, exact, lazy), endIndex)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}