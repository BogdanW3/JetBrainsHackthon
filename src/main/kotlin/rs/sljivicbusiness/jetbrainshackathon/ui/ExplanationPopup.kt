package rs.sljivicbusiness.jetbrainshackathon.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.ui.popup.JBPopupFactory
import javax.swing.*

object ExplanationPopup {

    fun show(editor: Editor, lines: List<String>) {
        val textArea = JTextArea(lines.joinToString("\n"))
        textArea.isEditable = false
        textArea.font = editor.colorsScheme.getFont(EditorFontType.PLAIN)

        val popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(JScrollPane(textArea), textArea)
            .setTitle("Regex Explanation")
            .setResizable(true)
            .createPopup()

        popup.showInBestPositionFor(editor)
    }
}