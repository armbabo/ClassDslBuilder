package jsontool.action.jackson

import jsontool.action.base.AddAction
import jsontool.core.JsonType

class JacksonAddAction : AddAction() {

    override fun getJsonType(): JsonType = JsonType.Jackson
}