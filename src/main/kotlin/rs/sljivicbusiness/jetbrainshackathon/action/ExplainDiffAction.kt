package rs.sljivicbusiness.jetbrainshackathon.action

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.vcs.log.VcsLogDataKeys
import kotlinx.coroutines.runBlocking
import rs.sljivicbusiness.jetbrainshackathon.openai.OpenAIService
import rs.sljivicbusiness.jetbrainshackathon.ui.ExplanationPopup
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class ExplainDiffAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread =
        ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        e.presentation.apply {
            text = "Explain Diff"
            isEnabledAndVisible = e.project != null
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR)

        val commitSelection = e.getData(VcsLogDataKeys.VCS_LOG_COMMIT_SELECTION) ?: return
        val commits = commitSelection.commits
        if (commits.size < 2) {
            Messages.showInfoMessage(
                project,
                "Please select two commits to explain the diff between them.",
                "Explain Diff"
            )
            return
        }
        val commitA = commits[0].hash.asString()
        val commitB = commits[1].hash.asString()
        val baseDir = project.basePath?.let(::File) ?: File(".")

        showInitialUi(project, editor, commitA, commitB)

        fetchDiffExplanation(
            project = project,
            editor = editor,
            baseDir = baseDir,
            commitA = commitA,
            commitB = commitB
        )
    }

    // ------------------------------------------------
    // UI
    // ------------------------------------------------

    private fun showInitialUi(
        project: com.intellij.openapi.project.Project,
        editor: com.intellij.openapi.editor.Editor?,
        commitA: String,
        commitB: String
    ) {
        val lines = listOf(
            "Diff: $commitA -> $commitB",
            "========= AI-Powered Explanation =========",
            "Fetching detailed explanation from OpenAI...",
            "",
            "(Diff will be truncated if too large)"
        )

        if (editor != null) {
            ExplanationPopup.show(editor, lines, false)
        } else {
            Messages.showInfoMessage(
                project,
                "Requesting explanation for diff: $commitA -> $commitB",
                "Explain Diff"
            )
        }
    }

    // ------------------------------------------------
    // Background work
    // ------------------------------------------------

    private fun fetchDiffExplanation(
        project: com.intellij.openapi.project.Project,
        editor: com.intellij.openapi.editor.Editor?,
        baseDir: File,
        commitA: String,
        commitB: String
    ) {
        val app = ApplicationManager.getApplication()

        app.executeOnPooledThread {
            val diffText = runGitDiff(baseDir, commitA.trim(), commitB.trim())
            val truncatedDiff = truncateDiff(diffText, MAX_DIFF_CHARS)
            val prompt = buildPrompt(commitA, commitB, truncatedDiff)

            val openAIService = OpenAIService()
            try {
                val aiExplanation = runBlocking {
                    openAIService.askOpenAI(prompt)
                }

                val resultLines = buildList {
                    add("Diff: $commitA -> $commitB")
                    add("========= AI-Powered Explanation =========")
                    add(aiExplanation)
                    add("")
                    add("(Diff truncated to ${truncatedDiff.length} chars)")
                }

                app.invokeLater {
                    if (editor != null) {
                        ExplanationPopup.show(editor, resultLines, true)
                    } else {
                        Messages.showInfoMessage(
                            project,
                            resultLines.joinToString("\n"),
                            "Explain Diff Result"
                        )
                    }
                }
            } catch (e: Exception) {
                println("Failed to get OpenAI explanation: ${e.message}")
                app.invokeLater {
                    Messages.showErrorDialog(
                        project,
                        "Failed to get explanation: ${e.message}",
                        "Explain Diff Error"
                    )
                }
            } finally {
                openAIService.close()
            }
        }
    }

    // ------------------------------------------------
    // Git
    // ------------------------------------------------

    private fun runGitDiff(
        workingDir: File,
        commitA: String,
        commitB: String
    ): String =
        try {
            val process = ProcessBuilder("git", "diff", commitA, commitB)
                .directory(workingDir)
                .redirectErrorStream(true)
                .start()

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                buildString {
                    reader.forEachLine { appendLine(it) }
                }
            }.also {
                process.waitFor()
            }
        } catch (e: Exception) {
            "Error running git diff: ${e.message}"
        }

    // ------------------------------------------------
    // Helpers
    // ------------------------------------------------

    private fun truncateDiff(diff: String, maxChars: Int): String =
        if (diff.length <= maxChars) diff
        else diff.substring(0, maxChars) + "\n\n...[truncated]..."

    private fun buildPrompt(
        commitA: String,
        commitB: String,
        diff: String
    ): String =
        buildString {
            appendLine(
                "You are a helpful assistant. Explain the following git diff in plain text, " +
                        "focusing on intent, high-level summary, and potential risks only if they are very likely. " +
                        "Be extremely concise (<= 200 words)."
            )
            appendLine(
                "IMPORTANT: do not analyze line-by-line, do not repeat the diff, " +
                        "and avoid mentioning binary file changes."
            )
            appendLine()
            appendLine("Diff between $commitA and $commitB:")
            appendLine()
            appendLine(diff)
        }

    companion object {
        private const val MAX_DIFF_CHARS = 60_000
    }
}
