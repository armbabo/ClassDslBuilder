package jsontool.action.base

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import jsontool.core.FileType
import jsontool.core.JsonType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile

abstract class BaseAction : AnAction() {

    override fun update(e: AnActionEvent) {
        super.update(e)

        if (e != null) {
            e.presentation.isVisible = isFileOk(e)
        }
    }

    private fun isFileOk(e: AnActionEvent): Boolean {
        val virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE)
        val psiFile = e.getData(LangDataKeys.PSI_FILE)

        val isVFOk = virtualFile?.name?.toLowerCase()?.endsWith("java") ?: false
        val isPsiOk = psiFile?.name?.toLowerCase()?.endsWith("java") ?: false

        val isKtVFOk = virtualFile?.name?.toLowerCase()?.endsWith("kt") ?: false
        val isKtPsiOk = psiFile?.name?.toLowerCase()?.endsWith("kt") ?: false

        val isJava = isVFOk && isPsiOk
        val isKt = isKtVFOk && isKtPsiOk
        return isJava || isKt
    }

    override fun actionPerformed(event: AnActionEvent) {
        try {
            doAction(event)
        } catch (e: Exception) {
            e.printStackTrace()
            val project = event.getData(PlatformDataKeys.PROJECT)
            Messages.showMessageDialog(project, e.message, "Warning", Messages.getWarningIcon())
        }
    }

    private fun doAction(event: AnActionEvent) {
        var msg: String? = null
        do {
            val psiFile = event.getData(LangDataKeys.PSI_FILE)
            if (psiFile == null) {
                msg = "No file"
                break
            }

            println("psiFile = $psiFile")

            var fileType: FileType?

            if (psiFile is PsiJavaFile) {
                fileType = FileType.JavaFile
            } else if (psiFile is KtFile) {
                fileType = FileType.KotlinFile
            } else {
                msg = "is not java or kotlin file"
                break
            }

            val project = event.getData(PlatformDataKeys.PROJECT)

            if (project != null) {
                WriteCommandAction.runWriteCommandAction(project) {
                    psiFile.children
                            .filter {
                                println("PsiFile1 -> $psiFile")
                                println("it1 -> $it")
                                it is PsiClass || it is KtClass
                            }
                            .forEach { doModify(project, psiFile, fileType, it) }
                }
            }
        } while (false)

        println("msg = $msg")

        if (msg != null && msg.isNotEmpty()) {
            throw RuntimeException(msg)
        }
    }

    abstract fun doModify(project: Project, psiFile: PsiFile, fileType: FileType, clazz: PsiElement)

    abstract fun getJsonType(): JsonType
}