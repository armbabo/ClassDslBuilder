package jsontool.action.gson

import jsontool.action.base.RemoveAction
import jsontool.core.JsonType

open class GsonRemoveAction : RemoveAction() {

    override fun getJsonType(): JsonType = JsonType.Gson
}