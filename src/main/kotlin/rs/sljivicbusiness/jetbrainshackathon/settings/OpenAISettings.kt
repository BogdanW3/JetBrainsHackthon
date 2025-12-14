package rs.sljivicbusiness.jetbrainshackathon.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "OpenAISettings",
    storages = [Storage("OpenAIPlugin.xml")]
)
@Service(Service.Level.APP)
class OpenAISettings : PersistentStateComponent<OpenAISettings> {

    var apiKey: String? = null
    var organizationId: String? = null

    override fun getState(): OpenAISettings = this

    override fun loadState(state: OpenAISettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): OpenAISettings = service<OpenAISettings>()
    }
}
