package rs.sljivicbusiness.jetbrainshackathon.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.CommonDataKeys
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

        ExplanationPopup.show(editor, explanation)
    }
}