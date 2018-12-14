package jsontool.action.fastjson

import jsontool.action.base.RemoveAction
import jsontool.core.JsonType

open class FastJsonRemoveAction : RemoveAction() {

    override fun getJsonType(): JsonType = JsonType.FastJson
}