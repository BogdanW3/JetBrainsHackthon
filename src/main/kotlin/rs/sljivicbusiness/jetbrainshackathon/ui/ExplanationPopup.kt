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
            val textArea = JTextArea(lines.joinToString("\n"))
            textArea.isEditable = false
            textArea.font = editor.colorsScheme.getFont(EditorFontType.PLAIN)

            popup = (JBPopupFactory.getInstance()
                .createComponentPopupBuilder(JScrollPane(textArea), textArea)
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
                val textArea = scrollPane?.viewport?.view as? JTextArea
                textArea?.text = lines.joinToString("\n")

                var newSize = content.preferredSize
                // check that it doesn't go off-screen using the popup's position
                val screenBounds = editor.component.graphicsConfiguration?.bounds
                if (screenBounds != null) {
//                    val popupLocation = popup!!.locationOnScreen
//                    if (popupLocation.x + newSize.width > screenBounds.x + screenBounds.width) {
//                        newSize = newSize.apply {
//                            width = screenBounds.x + screenBounds.width - popupLocation.x - 20
//                        }
//                    }
//                    if (popupLocation.y + newSize.height > screenBounds.y + screenBounds.height) {
//                        newSize = newSize.apply {
//                            height = screenBounds.y + screenBounds.height - popupLocation.y - 20
//                        }
//                    }
                    // also try to move it into a better position if needed
                    val popupLocation = popup!!.locationOnScreen
                    val adjustedPoint = Point(popupLocation.x, popupLocation.y)
                    if (popupLocation.x + newSize.width > screenBounds.x + screenBounds.width) {
                        adjustedPoint.x = screenBounds.x + screenBounds.width - newSize.width - 20
                    }
                    if (popupLocation.y + newSize.height > screenBounds.y + screenBounds.height) {
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