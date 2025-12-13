package rs.sljivicbusiness.jetbrainshackathon.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class OpenAISettingsConfigurable : Configurable {

    private val apiKeyField = JBPasswordField()
    private val organizationIdField = JBTextField()

    override fun getDisplayName(): String = "OpenAI API Settings"

    override fun createComponent(): JComponent {
        val settings = OpenAISettings.getInstance()

        apiKeyField.text = settings.apiKey
        organizationIdField.text = settings.organizationId

        return FormBuilder.createFormBuilder()
            .addLabeledComponent("API Key:", apiKeyField, 1, false)
            .addLabeledComponent("Organization ID (optional):", organizationIdField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val settings = OpenAISettings.getInstance()
        return String(apiKeyField.password) != settings.apiKey ||
               organizationIdField.text != settings.organizationId
    }

    override fun apply() {
        val settings = OpenAISettings.getInstance()
        settings.apiKey = String(apiKeyField.password)
        settings.organizationId = organizationIdField.text
    }

    override fun reset() {
        val settings = OpenAISettings.getInstance()
        apiKeyField.text = settings.apiKey
        organizationIdField.text = settings.organizationId
    }
}

