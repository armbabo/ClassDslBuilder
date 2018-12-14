
package generator.beans

import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import generator.utils.PsiUtils
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.getValueParameters
import java.util.*

/**
 */
class ClassFileInfo {

    lateinit var psiFile: PsiFile

    var psiClass: PsiClass? = null
    var ktClass: KtClass? = null
    var ktClassBody: KtClassBody? = null

    var psiMethods: MutableList<PsiMethod> = ArrayList()

    var psiImports: MutableList<PsiImportStatement> = ArrayList()

    var classFields: MutableList<PsiField> = ArrayList()

    val parameterList: ArrayList<KtParameter> = ArrayList()
    val propertyList: ArrayList<KtProperty> = ArrayList()

    val className: String?
        get() = psiClass!!.name

    val nameOfClassField: String
        get() = PsiUtils.firstToLower(className!!)

    companion object {

        fun parse(psiFile: PsiFile): ClassFileInfo {
            val classFileInfo = ClassFileInfo()
            psiFile.children.forEach {
                when(it) {
                    is KtClass -> classFileInfo.ktClass = it
                    is KtClassBody -> classFileInfo.ktClassBody = it

                    is PsiClass -> classFileInfo.psiClass = it
                    is PsiImportStatement -> classFileInfo.psiImports.add(it)
                }
            }

            classFileInfo.ktClass?.let {
                classFileInfo.propertyList.addAll(it.getProperties())
            classFileInfo.parameterList.addAll(it.getValueParameters())
            }
            classFileInfo.ktClassBody?.let {
                classFileInfo.propertyList.addAll(it.properties)
            }
            //get type
            classFileInfo.propertyList[0].typeReference?.text
            //get name
            classFileInfo.parameterList[0].name
            val value = classFileInfo.parameterList[1].typeReference?.typeElement?.firstChild!!.text
            val psiFile2 : Array<out PsiFile> = PsiShortNamesCache.getInstance(psiFile.project).getFilesByName(value)
            FilenameIndex.getFilesByName(psiFile.project, value+".kt", GlobalSearchScope.allScope(psiFile.project) )
            return classFileInfo
        }
    }

}
