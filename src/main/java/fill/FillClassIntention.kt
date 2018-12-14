package fill

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.analyzeAndGetResult
import org.jetbrains.kotlin.idea.quickfix.KotlinQuickFixAction
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.bindingContextUtil.getReferenceTargets
import org.jetbrains.kotlin.util.constructors

class FillClassIntention(
    element: KtCallExpression
): KotlinQuickFixAction<KtCallExpression>(element) {

    override fun getFamilyName(): String {
        return text
    }

    override fun getText(): String = "Fill class constructor"

    override fun invoke(project: Project, editor: Editor?, file: KtFile) {
        val analysisResult = element?.calleeExpression?.analyzeAndGetResult() ?: return
        val classDescriptor = element
            ?.calleeExpression
            ?.getReferenceTargets(analysisResult.bindingContext)
            ?.mapNotNull { (it as? ConstructorDescriptor)?.containingDeclaration }
            ?.distinct()
            ?.singleOrNull() ?: return
        val parameters = classDescriptor.constructors.first().valueParameters

        val factory = KtPsiFactory(project = project)
        val argument = factory.createExpression("""${classDescriptor.name.identifier}(
            ${createParameterSetterExpression(parameters)}
            )""".trimMargin())
        element?.replace(argument)
        return
    }

    override fun startInWriteAction() = true

    private fun createParameterSetterExpression(parameters: List<ValueParameterDescriptor>): String {
        var result = ""
        parameters.forEach { parameter ->
            var parameterString = "${parameter.name.isSpecial} = ${createDefaultValueFromParameter(parameter)},\n".let {
                if(parameters.last() == parameter) return@let it.replace(",\n","")
                it
            }
            result = "$result$parameterString"
        }
        return result
    }

    private fun createDefaultValueFromParameter(parameter: ValueParameterDescriptor): String {
        val type = parameter.type
        return when {
            KotlinBuiltIns.isBoolean(type) -> "false"
            KotlinBuiltIns.isChar(type) -> "''"
            KotlinBuiltIns.isDouble(type) -> "0.0"
            KotlinBuiltIns.isFloat(type) -> "0.0f"
            KotlinBuiltIns.isInt(type) || KotlinBuiltIns.isLong(type) || KotlinBuiltIns.isShort(type) -> "0"
            KotlinBuiltIns.isCollectionOrNullableCollection(type) -> "emptyArray()"
            KotlinBuiltIns.isNullableAny(type) -> "null"
            KotlinBuiltIns.isString(type) -> "\"\""
            KotlinBuiltIns.isListOrNullableList(type) -> "emptyList()"
            KotlinBuiltIns.isSetOrNullableSet(type) -> "emptySet()"
            type.isMarkedNullable -> "null"
            else -> ""
        }
    }
}