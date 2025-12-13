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

        // Remove surrounding quotes if present
        val cleanedText = text.trim('"', '\'')

        val tokens = RegexTokenizer.tokenize(cleanedText)
        val explanation = RegexExplainer.explain(tokens)

        return explanation.joinToString("<br>")
    }

    private fun looksLikeRegex(text: String): Boolean =
        text.contains("\\d") || text.contains("\\w") || text.contains("\\s") ||
        text.contains(")[") || text.startsWith("^") || text.endsWith("$") ||
        text.contains("]{") || text.contains(".*")
}