package rs.sljivicbusiness.jetbrainshackathon.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.ui.popup.AbstractPopup
import java.awt.Point
import javax.swing.*


object ExplanationPopup {

    var popup: JBPopup? = null


    fun show(editor: Editor, lines: List<String>) {


        if (popup == null) {
            val editorPane = JEditorPane("text/html", lines.joinToString("<br>"))
            editorPane.isEditable = false
            editorPane.font = editor.colorsScheme.getFont(EditorFontType.PLAIN)

            popup = (JBPopupFactory.getInstance()
                .createComponentPopupBuilder(JScrollPane(editorPane), editorPane)
                .setTitle("Regex Explanation")
                .setResizable(true)
                .createPopup())


            popup?.addListener(object : JBPopupListener {
                override fun onClosed(event: LightweightWindowEvent) {
                    popup = null
                }
            })
        } else
        {
            // replace content
            val content = (popup as AbstractPopup).content
            if (content is AbstractPopup.MyContentPanel) {
                val scrollPane = content.components.firstOrNull { it is JScrollPane } as? JScrollPane
                val editorPane = scrollPane?.viewport?.view as? JEditorPane
                editorPane?.text = lines.joinToString("<br>")

                var newSize = content.preferredSize
                // check that it doesn't go off-screen using the popup's position
                val screenBounds = editor.component.graphicsConfiguration?.bounds
                if (screenBounds != null) {
                    val popupLocation = popup!!.locationOnScreen
                    val adjustedPoint = Point(popupLocation.x, popupLocation.y)
                    if (newSize.width > screenBounds.width) {
                        newSize = newSize.apply {
                            width = screenBounds.width - 20
                        }
                        adjustedPoint.x = screenBounds.x + 10
                    } else if (popupLocation.x + newSize.width > screenBounds.x + screenBounds.width) {
                        adjustedPoint.x = screenBounds.x + screenBounds.width - newSize.width - 20
                    }

                    if (newSize.height > screenBounds.height) {
                        newSize = newSize.apply {
                            height = screenBounds.height - 20
                        }
                        adjustedPoint.y = screenBounds.y + 10
                    } else if (popupLocation.y + newSize.height > screenBounds.y + screenBounds.height) {
                        adjustedPoint.y = screenBounds.y + screenBounds.height - newSize.height - 20
                    }
                    popup!!.setLocation(adjustedPoint)


                }
                popup!!.size = newSize
            }
        }

        popup?.showInBestPositionFor(editor)
    }
}