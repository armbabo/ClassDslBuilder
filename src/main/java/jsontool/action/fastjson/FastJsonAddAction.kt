package jsontool.action.fastjson

import jsontool.action.base.AddAction
import jsontool.core.JsonType

class FastJsonAddAction : AddAction() {

    override fun getJsonType(): JsonType = JsonType.FastJson
}