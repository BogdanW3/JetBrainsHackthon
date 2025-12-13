package rs.sljivicbusiness.jetbrainshackathon.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import kotlinx.coroutines.runBlocking
import rs.sljivicbusiness.jetbrainshackathon.openai.OpenAIService
import rs.sljivicbusiness.jetbrainshackathon.ui.ExplanationPopup

class ExplainCodeAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.text = "Explain Code"

        // Only show the action if there's an editor and selected text
        val editor = e.getData(CommonDataKeys.EDITOR)
        val hasSelection = editor?.selectionModel?.hasSelection() == true

        presentation.isEnabledAndVisible = editor != null && hasSelection
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val selectedText = editor.selectionModel.selectedText

        if (selectedText.isNullOrBlank()) {
            return
        }

        // Show initial popup with loading message
        val lines = buildList {
            add("Code:")
            add(selectedText)
            add("")
            add("========= AI-Powered Explanation =========")
            add("Fetching explanation from OpenAI...")
        }
        ExplanationPopup.show(editor, lines)

        // Call OpenAI API on a pooled thread
        val openAIService = OpenAIService()
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val prompt = buildString {
                    appendLine("Explain the following code in detail:")
                    appendLine()
                    appendLine("```")
                    appendLine(selectedText)
                    appendLine("```")
                    appendLine()
                    appendLine("Please provide a clear explanation including:")
                    appendLine("- What this code does (main purpose)")
                    appendLine("- Key logic and important details")
                    appendLine("- Any potential issues or improvements")
                    appendLine()
                    appendLine("IMPORTANT: Format the answer in plain text (no markdown). Keep it concise and clear, under 200 words.")
                    appendLine("Don't echo back the code, just provide the explanation directly.")
                }

                val aiExplanation = runBlocking { openAIService.askOpenAI(prompt) }

                // Update the popup on the EDT
                ApplicationManager.getApplication().invokeLater {
                    val fullExplanation = buildList {
                        add("Code:")
                        add(selectedText)
                        add("")
                        add("========= AI-Powered Explanation =========")
                        add(aiExplanation)
                    }
                    ExplanationPopup.show(editor, fullExplanation)
                }
            } catch (e: Exception) {
                // Update popup with error message
                ApplicationManager.getApplication().invokeLater {
                    val errorExplanation = buildList {
                        add("Code:")
                        add(selectedText)
                        add("")
                        add("========= AI-Powered Explanation =========")
                        add("Error: Failed to get explanation from OpenAI.")
                        add("Details: ${e.message}")
                        add("")
                        add("Please check your API key configuration.")
                    }
                    ExplanationPopup.show(editor, errorExplanation)
                }
                println("Failed to get OpenAI explanation: ${e.message}")
                e.printStackTrace()
            } finally {
                openAIService.close()
            }
        }
    }
}

