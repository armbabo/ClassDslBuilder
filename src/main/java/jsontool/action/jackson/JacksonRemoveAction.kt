package jsontool.action.jackson

import jsontool.action.base.RemoveAction
import jsontool.core.JsonType

open class JacksonRemoveAction : RemoveAction() {

    override fun getJsonType(): JsonType = JsonType.Jackson
}