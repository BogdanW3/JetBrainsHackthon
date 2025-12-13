package rs.sljivicbusiness.jetbrainshackathon.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.CommonDataKeys
import kotlinx.coroutines.*
import rs.sljivicbusiness.jetbrainshackathon.openai.OpenAIService
import rs.sljivicbusiness.jetbrainshackathon.regex.*
import rs.sljivicbusiness.jetbrainshackathon.ui.ExplanationPopup

class ExplainRegexAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.text = "Explain Regex"
        presentation.isEnabledAndVisible = e.getData(CommonDataKeys.EDITOR) != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        var text = editor.selectionModel.selectedText

        if (text.isNullOrBlank()) {
            // If no selection, try to get the string under the caret
            val caretOffset = editor.caretModel.offset
            val document = editor.document
            val lineNumber = document.getLineNumber(caretOffset)
            val lineStartOffset = document.getLineStartOffset(lineNumber)
            val lineEndOffset = document.getLineEndOffset(lineNumber)
            val lineText = document.getText().substring(lineStartOffset, lineEndOffset)
            val regexPattern = Regex("\"(.*?)\"|'(.*?)'|`(.*?)`")
            val matchResult = regexPattern.findAll(lineText)

            text = matchResult.mapNotNull {
                val range = it.range
                val absoluteStart = lineStartOffset + range.first
                val absoluteEnd = lineStartOffset + range.last + 1
                if (caretOffset in absoluteStart..absoluteEnd) {
                    it.groups[1]?.value ?:
                    it.groups[2]?.value ?:
                    it.groups[3]?.value
                } else {
                    null
                }
            }.firstOrNull()
            if (text.isNullOrBlank()) {
                return
            }
        }

        val tokens = RegexTokenizer.tokenize(text, true)
        val explanation = RegexExplainer.explain(tokens)

        // Show initial explanation immediately
        ExplanationPopup.show(editor, explanation)

        // Call OpenAI API asynchronously to get AI-powered explanation
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            val openAIService = OpenAIService()
            try {
                val prompt = buildString {
                    appendLine("Explain this regular expression in detail:")
                    appendLine("Regex: $text")
                    appendLine()
                    appendLine("Basic breakdown:")
                    explanation.forEach { appendLine(it) }
                    appendLine()
                    appendLine("Please provide a comprehensive explanation including:")
                    appendLine("- What this regex matches")
                    appendLine("- Real-world use cases")
                    appendLine("- Example matches and non-matches")
                }

                val aiExplanation = openAIService.askOpenAI(prompt)

                // Update the popup with AI explanation on EDT
                withContext(Dispatchers.Main) {
                    val fullExplanation = buildList {
                        add("=== Basic Pattern Breakdown ===")
                        addAll(explanation)
                        add("")
                        add("=== AI-Powered Detailed Explanation ===")
                        add(aiExplanation)
                    }
                    ExplanationPopup.show(editor, fullExplanation)
                }
            } catch (e: Exception) {
                // Log error but don't interrupt user experience
                println("Failed to get OpenAI explanation: ${e.message}")
            } finally {
                openAIService.close()
            }
        }
    }
}