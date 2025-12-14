package rs.sljivicbusiness.regins.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class OpenAISettingsConfigurable : Configurable {

    private val apiKeyField = JBPasswordField()
    private val organizationIdField = JBTextField()

    private val settings: OpenAISettings
        get() = OpenAISettings.getInstance()

    override fun getDisplayName(): String = "OpenAI API Settings"

    override fun createComponent(): JComponent {
        reset()

        return FormBuilder.createFormBuilder()
            .addLabeledComponent("API Key:", apiKeyField, 1, false)
            .addLabeledComponent("Organization ID (optional):", organizationIdField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean =
        String(apiKeyField.password) != settings.apiKey ||
                organizationIdField.text != settings.organizationId

    override fun apply() {
        settings.apiKey = String(apiKeyField.password).trim()
        settings.organizationId = organizationIdField.text.trim()
    }

    override fun reset() {
        apiKeyField.text = settings.apiKey
        organizationIdField.text = settings.organizationId
    }
}
