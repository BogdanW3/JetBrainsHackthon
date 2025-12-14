package rs.sljivicbusiness.jetbrainshackathon.action

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import kotlinx.coroutines.runBlocking
import rs.sljivicbusiness.jetbrainshackathon.openai.OpenAIService
import rs.sljivicbusiness.jetbrainshackathon.regex.*
import rs.sljivicbusiness.jetbrainshackathon.ui.ExplanationPopup

class ExplainRegexAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread =
        ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        e.presentation.apply {
            text = "Explain Regex"
            isEnabledAndVisible = e.getData(CommonDataKeys.EDITOR) != null
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val regexText = extractRegex(editor) ?: return

        val tokens = RegexTokenizer.tokenize(regexText)
        val explanation = RegexExplainer.explain(tokens, html = true)

        showInitialPopup(editor, regexText, explanation)
        fetchAiExplanation(editor, regexText, explanation)
    }

    private fun extractRegex(editor: Editor): String? {
        editor.selectionModel.selectedText
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        val caretOffset = editor.caretModel.offset
        val document = editor.document
        val lineNumber = document.getLineNumber(caretOffset)

        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val lineText = document.text.substring(lineStart, lineEnd)

        val regexLiteral =
            Regex("(r?\".*?\")|('.*?')|(`.*?`)|(/.*?/)")


        return regexLiteral.findAll(lineText)
            .firstNotNullOfOrNull { match ->
                val absoluteStart = lineStart + match.range.first
                val absoluteEnd = lineStart + match.range.last + 1

                if (caretOffset in absoluteStart..absoluteEnd) {
                    match.groups.drop(1).firstOrNull()?.value
                } else {
                    null
                }
            }
    }

    private fun showInitialPopup(
        editor: Editor,
        regex: String,
        explanation: List<String>
    ) {
        val lines = buildList {
            add(regex)
            add("========= AI-Powered Explanation =========")
            add("Fetching detailed explanation from OpenAI...")
            add("")
            add("======= Regex Pattern Illustration =======")
            addAll(explanation)
        }

        ExplanationPopup.show(editor, lines)
    }

    private fun fetchAiExplanation(
        editor: Editor,
        regex: String,
        explanation: List<String>
    ) {
        val app = ApplicationManager.getApplication()
        val service = OpenAIService()

        app.executeOnPooledThread {
            try {
                val prompt = buildPrompt(regex, explanation)
                val aiExplanation = runBlocking {
                    service.askOpenAI(prompt)
                }

                app.invokeLater {
                    val updatedLines = buildList {
                        add(regex)
                        add("========= AI-Powered Explanation =========")
                        add(aiExplanation)
                        add("")
                        add("======= Regex Pattern Illustration =======")
                        addAll(explanation)
                    }
                    ExplanationPopup.show(editor, updatedLines)
                }
            } catch (e: Exception) {
                println("Failed to get OpenAI explanation: ${e.message}")
            } finally {
                service.close()
            }
        }
    }

    private fun buildPrompt(
        regex: String,
        explanation: List<String>
    ): String =
        buildString {
            appendLine("Explain this regular expression in detail:")
            appendLine("Regex: $regex")
            appendLine()
            appendLine("Basic breakdown:")
            explanation.forEach { appendLine(it) }
            appendLine()
            appendLine("Please provide a short explanation of its intent, including:")
            appendLine("- What this regex matches")
            appendLine("- Real-world use cases")
            appendLine("- Potential problems if any")
            appendLine(
                "IMPORTANT: format the answer in plain text, short and concise. " +
                        "No markdown. Under 150 words. Do not repeat the question."
            )
        }
}
