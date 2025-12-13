package rs.sljivicbusiness.jetbrainshackathon.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import rs.sljivicbusiness.jetbrainshackathon.regex.*

class ExplainRegexAction : AnAction("Explain Regex") {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val selection = editor.selectionModel.selectedText ?: return

        val tokens = RegexTokenizer.tokenize(selection)
        val explanation = RegexExplainer.explain(tokens)

        ExplanationPopup.show(editor, explanation)
    }
}