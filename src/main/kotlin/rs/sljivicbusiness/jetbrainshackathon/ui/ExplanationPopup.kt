package rs.sljivicbusiness.jetbrainshackathon.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.ui.popup.AbstractPopup
import java.awt.Point
import javax.swing.JEditorPane
import javax.swing.JScrollPane

object ExplanationPopup {

    private var popup: JBPopup? = null

    fun show(editor: Editor, lines: List<String>) {
        val html = buildHtml(editor, lines)

        popup?.let {
            updatePopup(it, editor, html)
            return
        }

        createAndShowPopup(editor, html)
    }

    private fun createAndShowPopup(editor: Editor, html: String) {
        val editorPane = JEditorPane("text/html", html).apply {
            isEditable = false
            font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
        }

        popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(JScrollPane(editorPane), editorPane)
            .setTitle("Regex Explanation")
            .setResizable(true)
            .createPopup()
            .also { popup ->
                popup.addListener(object : JBPopupListener {
                    override fun onClosed(event: LightweightWindowEvent) {
                        this@ExplanationPopup.popup = null
                    }
                })
                popup.showInBestPositionFor(editor)
            }
    }

    private fun updatePopup(
        popup: JBPopup,
        editor: Editor,
        html: String
    ) {
        val abstractPopup = popup as? AbstractPopup ?: return
        val content = abstractPopup.content as? AbstractPopup.MyContentPanel ?: return

        val editorPane = extractEditorPane(content) ?: return
        editorPane.text = html

        adjustPopupSizeAndPosition(abstractPopup, content, editor)
    }

    private fun extractEditorPane(
        content: AbstractPopup.MyContentPanel
    ): JEditorPane? {
        val scrollPane = content.components
            .firstOrNull { it is JScrollPane } as? JScrollPane

        return scrollPane?.viewport?.view as? JEditorPane
    }

    private fun adjustPopupSizeAndPosition(
        popup: AbstractPopup,
        content: AbstractPopup.MyContentPanel,
        editor: Editor
    ) {
        var newSize = content.preferredSize
        val screenBounds = editor.component.graphicsConfiguration?.bounds ?: return

        val popupLocation = popup.locationOnScreen
        val adjustedPoint = Point(popupLocation)

        if (newSize.width > screenBounds.width) {
            newSize = newSize.apply { width = screenBounds.width - 20 }
            adjustedPoint.x = screenBounds.x + 10
        } else if (popupLocation.x + newSize.width > screenBounds.maxX) {
            adjustedPoint.x = screenBounds.maxX.toInt() - newSize.width - 20
        }

        if (newSize.height > screenBounds.height) {
            newSize = newSize.apply { height = screenBounds.height - 20 }
            adjustedPoint.y = screenBounds.y + 10
        } else if (popupLocation.y + newSize.height > screenBounds.maxY) {
            adjustedPoint.y = screenBounds.maxY.toInt() - newSize.height - 20
        }

        popup.setLocation(adjustedPoint)
        popup.size = newSize
    }

    private fun buildHtml(editor: Editor, lines: List<String>): String {
        val content = lines.joinToString("<br>")
        val font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
        val fg = editor.colorsScheme.defaultForeground
        val bg = editor.colorsScheme.defaultBackground

        return """
            <html>
              <body style="
                font-family: ${font.family};
                font-size: ${font.size - 2}px;
                color: rgb(${fg.red}, ${fg.green}, ${fg.blue});
                background-color: rgb(${bg.red}, ${bg.green}, ${bg.blue});
              ">
                $content
              </body>
            </html>
        """.trimIndent()
    }
}
