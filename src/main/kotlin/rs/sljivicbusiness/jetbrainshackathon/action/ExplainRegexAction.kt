package rs.sljivicbusiness.jetbrainshackathon.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import kotlinx.coroutines.runBlocking
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
            val lineText = document.text.substring(lineStartOffset, lineEndOffset)
            val regexPattern = Regex("(r?\".*?\")|('.*?')|(`.*?`)|(/.*?/)")
            val matchResult = regexPattern.findAll(lineText)

            text = matchResult.firstNotNullOfOrNull {
                val range = it.range
                val absoluteStart = lineStartOffset + range.first
                val absoluteEnd = lineStartOffset + range.last + 1
                if (caretOffset in absoluteStart..absoluteEnd) {
                    it.groups[1]?.value ?: it.groups[2]?.value ?: it.groups[3]?.value ?: it.groups[4]?.value
                } else {
                    null
                }
            }
            if (text.isNullOrBlank()) {
                return
            }
        }

        val tokens = RegexTokenizer.tokenize(text)
        val explanation = RegexExplainer.explain(tokens, 0, false)

        val lines = buildList {
            add(text)
            add("========= AI-Powered Explanation =========")
            add("Fetching detailed explanation from OpenAI...")
            add("")
            add("======= Regex Pattern Illustration =======")
            addAll(explanation)
        }
        // Show initial explanation immediately
        ExplanationPopup.show(editor, lines)

        // Call OpenAI API on a pooled thread to avoid using Dispatchers.Main
        val openAIService = OpenAIService()
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val prompt = buildString {
                    appendLine("Explain this regular expression in detail:")
                    appendLine("Regex: $text")
                    appendLine()
                    appendLine("Basic breakdown:")
                    explanation.forEach { appendLine(it) }
                    appendLine()
                    appendLine("Please provide a short explanation of its intent, including:")
                    appendLine("- What this regex matches")
                    appendLine("- Real-world use cases")
                    appendLine("- Potential problems if any")
                    appendLine("IMPORTANT: format the answer in plain text, don't make it too exotic or long. Don't use markdown, keep it short and concise, focus on clarity.")
                    appendLine("Don't echo back the question, just provide the explanation directly.")
                    appendLine("Keep the response under 150 words, don't needlessly strive for full sentences, keep it maximally short, eg.  ```matches dates in the ISO format\n\nuseful for validating dates in json input\n\ndoesn't check number validity (e.g. 32nd december)```")
                }

                val aiExplanation = runBlocking { openAIService.askOpenAI(prompt) }

                // Update the popup on the EDT
                ApplicationManager.getApplication().invokeLater {
                    val fullExplanation = buildList {
                        add(text)
                        add("========= AI-Powered Explanation =========")
                        add(aiExplanation)
                        add("")
                        add("======= Regex Pattern Illustration =======")
                        addAll(explanation)
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