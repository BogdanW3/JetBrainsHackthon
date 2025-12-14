package rs.sljivicbusiness.regins.hover

import rs.sljivicbusiness.regins.regex.*
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import rs.sljivicbusiness.regins.regex.RegexUtils

class RegexHoverProvider : AbstractDocumentationProvider() {

    override fun generateDoc(
        element: PsiElement,
        originalElement: PsiElement?
    ): String? {
        val text = element.text ?: return null
        if (!RegexUtils.looksLikeRegex(RegexUtils.tryUnescapeString(text))) return null

        val tokens = RegexTokenizer.tokenize(text)
        val explanation = RegexExplainer.explain(tokens)

        return explanation.joinToString("<br>")
    }

}