package jsontool.action.gson

import jsontool.action.base.AddAction
import jsontool.core.JsonType

class GsonAddAction : AddAction() {

    override fun getJsonType(): JsonType = JsonType.Gson
}