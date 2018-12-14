
package generator.utils


import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import java.util.regex.Pattern

/**
 *
 */
object PsiUtils {

    fun getPsiClass(psiFile: PsiFile): PsiClass? {
        for (element in psiFile.children) {
            if (element is PsiClass) {
                //find first psiClass
                return element
            }
        }
        return null
    }

    fun formatFieldNameToaB(str: String): String {
        val result = StringBuilder()
        var changeToUp = false
        for (i in 0 until str.length) {
            if ('_' == str[i]) {
                changeToUp = true
            } else if (changeToUp) {
                result.append(toUp(str[i]))
                changeToUp = false
            } else {
                result.append(str[i])
            }
        }
        return result.toString()
    }

    fun getMemberFieldName(fieldName: String): String {
        return if (Pattern.matches("m[A-Z].*", fieldName)) {
            firstToLower(fieldName.substring(1))
        } else fieldName
    }

    fun firstToUpper(name: String): String {
        val firstIndex = name[0]
        if (firstIndex >= 'a' && firstIndex <= 'z') {
            val newIndex = (name[0].toInt() + ('A' - 'a')).toChar()
            return name.replaceFirst((firstIndex + "").toRegex(), newIndex + "")
        }
        return name
    }

    fun firstToLower(name: String): String {
        val firstIndex = name[0]
        if (firstIndex >= 'A' && firstIndex <= 'Z') {

            val newIndex = (name[0].toInt() - ('A' - 'a')).toChar()
            return name.replaceFirst((firstIndex + "").toRegex(), newIndex + "")
        }
        return name
    }

    fun toUp(a: Char): Char {
        return if (a >= 'A' && a <= 'Z') {
            a
        } else (a.toInt() + ('A' - 'a')).toChar()
    }

    fun toLow(a: Char): Char {
        return if (a >= 'a' && a <= 'z') {
            a
        } else (a.toInt() - ('A' - 'a')).toChar()
    }
}
