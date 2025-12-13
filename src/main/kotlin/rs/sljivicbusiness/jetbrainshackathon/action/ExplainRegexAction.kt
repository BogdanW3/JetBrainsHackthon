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
        val selection = editor.selectionModel.selectedText ?: return

        val tokens = RegexTokenizer.tokenize(selection)
        val explanation = RegexExplainer.explain(tokens)

        ExplanationPopup.show(editor, explanation)
    }
}