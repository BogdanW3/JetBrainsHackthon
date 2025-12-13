package rs.sljivicbusiness.jetbrainshackathon.hover

import rs.sljivicbusiness.jetbrainshackathon.regex.*
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement

class RegexHoverProvider : AbstractDocumentationProvider() {

    override fun generateDoc(
        element: PsiElement,
        originalElement: PsiElement?
    ): String? {
        val text = element.text ?: return null
        if (!looksLikeRegex(text)) return null

        var cleanedText: String
        var escaped = true
        // detect regex string, and strip the /
        if (text.startsWith("/") && text.endsWith("/")) {
            val regexBody = text.substring(1, text.length - 1)
            cleanedText = regexBody
            escaped = false
        }
        else if (text.startsWith("r\"") && text.endsWith("\"")) {
            val regexBody = text.substring(2, text.length - 1)
            cleanedText = regexBody
            escaped = false
        } else {
            // Remove surrounding quotes if present
            cleanedText = text.trim('"', '\'')
        }

        val tokens = RegexTokenizer.tokenize(cleanedText, escaped)
        val explanation = RegexExplainer.explain(tokens)

        return explanation.joinToString("<br>")
    }

    private fun looksLikeRegex(text: String): Boolean =
        text.contains("\\d") || text.contains("\\w") || text.contains("\\s") ||
        text.contains(")[") || text.startsWith("^") || text.endsWith("$") ||
        text.contains("]{") || text.contains(".*")
}