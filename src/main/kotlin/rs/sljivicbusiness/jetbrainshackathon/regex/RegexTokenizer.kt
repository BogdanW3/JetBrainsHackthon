package rs.sljivicbusiness.jetbrainshackathon.regex

object RegexTokenizer {

    fun tokenize(regex: String): List<RegexToken> {
        val tokens = mutableListOf<RegexToken>()
        var i = 0

        while (i < regex.length) {
            when {
                // Anchors
                regex[i] == '^' -> tokens.add(RegexToken.StartAnchor)
                regex[i] == '$' -> tokens.add(RegexToken.EndAnchor)

                // Escape sequences
                regex[i] == '\\' && i + 1 < regex.length -> {
                    when (regex[i + 1]) {
                        'd' -> {
                            tokens.add(RegexToken.DigitClass)
                            i++
                        }
                        'w' -> {
                            tokens.add(RegexToken.WordClass)
                            i++
                        }
                        's' -> {
                            tokens.add(RegexToken.WhitespaceClass)
                            i++
                        }
                        // Escaped special characters
                        '.', '*', '+', '?', '[', ']', '(', ')', '{', '}', '^', '$', '|', '\\' -> {
                            tokens.add(RegexToken.Literal(regex[i + 1].toString()))
                            i++
                        }
                        else -> {
                            // Other escape sequences treated as literals
                            tokens.add(RegexToken.Literal(regex[i + 1].toString()))
                            i++
                        }
                    }
                }

                // Character classes
                regex[i] == '[' -> {
                    val end = regex.indexOf(']', i)
                    if (end > i) {
                        tokens.add(RegexToken.CharClass(regex.substring(i, end + 1)))
                        i = end
                    }
                }

                // Groups
                regex[i] == '(' -> {
                    val groupTokens = parseGroup(regex, i)
                    if (groupTokens != null) {
                        tokens.add(groupTokens.first)
                        i = groupTokens.second
                    } else {
                        tokens.add(RegexToken.Literal(regex[i].toString()))
                    }
                }

                // Quantifiers (must come after a token)
                regex[i] == '*' && tokens.isNotEmpty() -> {
                    val lastToken = tokens.removeLast()
                    tokens.add(RegexToken.Group(listOf(lastToken)))
                    tokens.add(RegexToken.Quantifier(0, null))
                }
                regex[i] == '+' && tokens.isNotEmpty() -> {
                    val lastToken = tokens.removeLast()
                    tokens.add(RegexToken.Group(listOf(lastToken)))
                    tokens.add(RegexToken.Quantifier(1, null))
                }
                regex[i] == '?' && tokens.isNotEmpty() -> {
                    val lastToken = tokens.removeLast()
                    tokens.add(RegexToken.Group(listOf(lastToken)))
                    tokens.add(RegexToken.Quantifier(0, 1))
                }
                regex[i] == '{' -> {
                    val quantifier = parseQuantifier(regex, i)
                    if (quantifier != null && tokens.isNotEmpty()) {
                        val lastToken = tokens.removeLast()
                        tokens.add(RegexToken.Group(listOf(lastToken)))
                        tokens.add(quantifier.first)
                        i = quantifier.second
                    } else {
                        tokens.add(RegexToken.Literal(regex[i].toString()))
                    }
                }

                // Wildcards
                regex[i] == '.' -> tokens.add(RegexToken.CharClass("."))

                // Literals
                else -> tokens.add(RegexToken.Literal(regex[i].toString()))
            }
            i++
        }
        return tokens
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
                    val groupTokens = tokenize(groupContent)
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

        return try {
            when {
                content.contains(',') -> {
                    val parts = content.split(',')
                    val min = parts[0].trim().toIntOrNull() ?: return null
                    val max = parts[1].trim().toIntOrNull()
                    Pair(RegexToken.Quantifier(min, max), closeIndex)
                }
                else -> {
                    val exact = content.trim().toIntOrNull() ?: return null
                    Pair(RegexToken.Quantifier(exact, exact), closeIndex)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}