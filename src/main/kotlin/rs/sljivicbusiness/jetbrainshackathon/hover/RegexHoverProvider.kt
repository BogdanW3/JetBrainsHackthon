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

        val tokens = RegexTokenizer.tokenize(text)
        val explanation = RegexExplainer.explain(tokens)

        return explanation.joinToString("<br>")
    }

    private fun looksLikeRegex(text: String): Boolean =
        text.contains("\\d") || text.contains("[") || text.startsWith("^")
}