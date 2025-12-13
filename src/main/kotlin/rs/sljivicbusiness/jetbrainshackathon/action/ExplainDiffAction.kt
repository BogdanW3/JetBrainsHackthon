package rs.sljivicbusiness.jetbrainshackathon.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.vcs.log.VcsLogDataKeys
import com.intellij.vcs.log.ui.table.size
import kotlinx.coroutines.runBlocking
import rs.sljivicbusiness.jetbrainshackathon.openai.OpenAIService
import rs.sljivicbusiness.jetbrainshackathon.ui.ExplanationPopup
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


class ExplainDiffAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.text = "Explain Diff"
        // enable when a project is open
        presentation.isEnabledAndVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR)

        var commits = e.getData(VcsLogDataKeys.VCS_LOG_COMMIT_SELECTION)

        if (commits == null || commits.size != 2) {
            return
        }


        // get the two commits from the Vcs selection
        val commitA = (commits.commits[0].hash).asString()
        val commitB = (commits.commits[1].hash).asString()

        val baseDir = project.basePath?.let { File(it) } ?: File(".")

        // Show initial popup while fetching
        val initialLines = listOf(
            "Diff: $commitA -> $commitB",
            "========= AI-Powered Explanation =========",
            "Fetching detailed explanation from OpenAI...",
            "",
            "(Diff will be truncated if too large)"
        )

        if (editor != null) {
            ExplanationPopup.show(editor, initialLines)
        } else {
            Messages.showInfoMessage(project, "Requesting explanation for diff: $commitA -> $commitB", "Explain Diff")
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            val diffText = runGitDiff(baseDir, commitA.trim(), commitB.trim())
            val truncated = truncateDiff(diffText, 60_000) // keep prompt reasonably sized

            val prompt = buildString {
                appendLine("You are a helpful assistant. Explain the following git diff in plain text, focusing on intent, high level summary, and potential risks (only if the risks are very likely). Keep it very concise (<= 200 words). Do not echo the entire diff back and use the least words possible to illustrate the intention of the diff.")
                appendLine("IMPORTANT: be as short as possible, do not analyze changes line-by-line nor elaborate on binary file changes. Focus on summarizing the intent of the changes.")
                appendLine()
                appendLine("Diff between $commitA and $commitB:")
                appendLine()
                appendLine(truncated)
            }

            val openAIService = OpenAIService()
            try {
                val aiExplanation = runBlocking { openAIService.askOpenAI(prompt) }
                val fullLines = buildList {
                    add("Diff: $commitA -> $commitB")
                    add("========= AI-Powered Explanation =========")
                    add(aiExplanation)
                    add("")
                    add("(Diff truncated to ${truncated.length} chars)")
                }

                ApplicationManager.getApplication().invokeLater {
                    if (editor != null) {
                        ExplanationPopup.show(editor, fullLines)
                    } else {
                        // No editor to attach popup; show message dialog
                        Messages.showInfoMessage(project, fullLines.joinToString("\n"), "Explain Diff Result")
                    }
                }
            } catch (ex: Exception) {
                println("Failed to get OpenAI explanation: ${ex.message}")
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(project, "Failed to get explanation: ${ex.message}", "Explain Diff Error")
                }
            } finally {
                openAIService.close()
            }
        }
    }

    private fun runGitDiff(workingDir: File, a: String, b: String): String {
        return try {
            val pb = ProcessBuilder("git", "diff", a, b)
            pb.directory(workingDir)
            pb.redirectErrorStream(true)
            val process = pb.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                output.append(line).append('\n')
                line = reader.readLine()
            }
            process.waitFor()
            output.toString()
        } catch (e: Exception) {
            "Error running git diff: ${e.message}"
        }
    }

    private fun truncateDiff(diff: String, maxChars: Int): String {
        if (diff.length <= maxChars) return diff
        return diff.substring(0, maxChars) + "\n\n...[truncated]..."
    }
}
