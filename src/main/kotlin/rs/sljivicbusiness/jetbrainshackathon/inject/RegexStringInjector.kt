package rs.sljivicbusiness.jetbrainshackathon.inject

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import org.intellij.lang.regexp.RegExpLanguage
import rs.sljivicbusiness.jetbrainshackathon.regex.RegexTokenizer

/**
 * Injects RegExpLanguage into string literals that look like regular expressions.
 * Works for any language implementing PsiLanguageInjectionHost.
 */
class RegexStringInjector : MultiHostInjector {

    override fun elementsToInjectIn(): List<Class<out PsiElement>> =
        listOf(PsiLanguageInjectionHost::class.java)

    // Newer API: registrar first, context second
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val host = context as? PsiLanguageInjectionHost ?: return
        val raw = host.text ?: return

        val inner = stripQuotes(raw) ?: return
        val unescaped = RegexTokenizer.tryUnescapeString(raw)
        if (!looksLikeRegex(unescaped)) return

        // inject into the inner range (strip quotes)
        val range = TextRange(raw.indexOf(inner), raw.indexOf(inner) + inner.length)
        registrar.startInjecting(RegExpLanguage.INSTANCE)
        registrar.addPlace(null, null, host, range)
        registrar.doneInjecting()
    }

    // Heuristic from existing hover provider: simple patterns that often indicate a regex
    private fun looksLikeRegex(text: String): Boolean =
        text.contains("\\d") || text.contains("\\w") || text.contains("\\s") ||
            text.contains(")[") || text.startsWith("^") || text.endsWith("$") ||
            text.contains("]{") || text.contains(".*")

    private fun stripQuotes(text: String): String? {
        if (text.length >= 2 && ((text.startsWith("\"") && text.endsWith("\"")) || (text.startsWith("'") && text.endsWith("'")))) {
            return text.substring(1, text.length - 1)
        }
        // JS regex literal /.../
        if (text.length >= 2 && text.startsWith("/") && text.endsWith("/")) return text.substring(1, text.length - 1)
        return null
    }

}
