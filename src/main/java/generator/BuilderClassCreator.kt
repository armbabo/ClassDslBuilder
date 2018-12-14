package generator

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.util.ThrowableRunnable
import generator.beans.ClassFileInfo
import generator.utils.PsiUtils
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.util.*
import java.util.regex.Pattern


class BuilderClassCreator(private val mProject: Project, private val mCurrentPsiFile: PsiFile, config: Config) {
    private var mConfig: Config? = null
    private val mElementFactory: PsiElementFactory

    private var mBuilderPsiClass: PsiClass? = null

    private val mClassFileInfo: ClassFileInfo

    init {
        this.mConfig = config

        this.mElementFactory = JavaPsiFacade.getInstance(mProject).elementFactory
        mClassFileInfo = ClassFileInfo.parse(mCurrentPsiFile)
        if (mConfig == null) {
            mConfig = Config()
        }
    }

    fun generate() {
        Writter(mProject, mCurrentPsiFile).run()
    }

    private fun changeFieldModifyType() {
        for (field in mClassFileInfo.classFields) {
            if (field.modifierList!!.hasModifierProperty("static")) {
                continue
            }
            field.delete()
            val newField = mElementFactory.createFieldFromText(
                String.format(
                    "private final %s %s;",
                    field.type.presentableText, field.name
                ), mClassFileInfo.psiClass
            )
            newField.modifierList!!.checkSetModifierProperty("private", true)
            newField.modifierList!!.checkSetModifierProperty("final", true)
            mClassFileInfo.psiClass?.add(newField)
        }
    }

    private fun createConstructMethod() {
        val methodStr = StringBuilder()
        methodStr.append(String.format("private %s(Builder builder) {", mClassFileInfo.psiClass?.name))
        for (field in mClassFileInfo.classFields) {
            methodStr.append(String.format("this.%s = builder.%s;", field.name, field.name))
        }
        methodStr.append("}")
        val psiMethod = mElementFactory.createMethodFromText(methodStr.toString(), mClassFileInfo.psiClass)
        mClassFileInfo.psiClass?.add(psiMethod)
    }

    private fun createStaticBuildMethod() {
        val method = mElementFactory.createMethodFromText(
            "public static Builder builder() { return new Builder();}",
            mClassFileInfo.psiClass
        )
        mClassFileInfo.psiClass?.add(method)

    }

    private fun createGetterMethods() {
        for (psiField in mClassFileInfo.classFields) {
            val method = mElementFactory.createMethodFromText(
                String.format(
                    "public %s %s() { return %s;}", psiField.type.presentableText,
                    getGetterMethodName(psiField), psiField.name
                ), mClassFileInfo.psiClass
            )
            mClassFileInfo.psiClass?.add(method)
        }
    }

    private fun getGetterMethodName(psiField: PsiField): String {
        var name = "get"
        if (psiField.type.presentableText == "boolean" || psiField.type.presentableText == "Boolean") {
            name = "is"
        }

        val fieldName = psiField.name
        if (Pattern.matches("m[A-Z].*", psiField.name)) {
            name += fieldName.substring(1)
        } else {
            name += PsiUtils.firstToUpper(fieldName)
        }
        return name
    }

    private fun createInnerBuilderClass() {
        mBuilderPsiClass = mElementFactory.createClass("Builder")
        mBuilderPsiClass!!.modifierList!!.setModifierProperty("static", true)
        appendFields()
        if (mConfig!!.overlay) {
            createReplaceConcurent()
            createOverlayMethod()
        }
        appendSetMethods()
        appendBuildMethod()
        mClassFileInfo.psiClass?.add(mBuilderPsiClass!!)
    }

    private fun appendFields() {
        for (psiField in mClassFileInfo.classFields) {
            val builderField = mElementFactory.createFieldFromText(
                String.format("private %s %s;", psiField.type.presentableText, psiField.name), mBuilderPsiClass
            )
            mBuilderPsiClass!!.add(builderField)
        }
    }

    private fun appendSetMethods() {
        for (psiField in mClassFileInfo.classFields) {
            val methodStr = StringBuilder()
            val memberFieldName = PsiUtils.getMemberFieldName(psiField.name)
            methodStr.append(
                String.format(
                    "public Builder %s(%s %s) {", memberFieldName,
                    psiField.type.presentableText, memberFieldName
                )
            )
            methodStr.append(String.format("this.%s = %s;", psiField.name, memberFieldName))
            methodStr.append("return this; }")
            mBuilderPsiClass!!.add(mElementFactory.createMethodFromText(methodStr.toString(), mBuilderPsiClass))
        }
    }

    private fun appendBuildMethod() {
        val psiMethod = mElementFactory.createMethodFromText(
            String.format(
                "public %s build() { return new %s(this);}", mClassFileInfo.className,
                mClassFileInfo.className
            ), mBuilderPsiClass
        )
        mBuilderPsiClass!!.add(psiMethod)
    }

    private fun createOverlayMethod() {
        val classFieldName = mClassFileInfo.nameOfClassField
        val methodStr = StringBuilder(
            String.format(
                "public Builder overlay(%s %s) {", mClassFileInfo.className,
                classFieldName
            )
        )
        methodStr.append(String.format("if (%s == null) { return this;}", classFieldName))
        for (psiField in mClassFileInfo.classFields) {
            val getStr = String.format("%s.%s()", classFieldName, getGetterMethodName(psiField))
            if (psiField.type.presentableText == "String") {
                methodStr.append(String.format("if (!TextUtils.isEmpty(%s)) {%s = %s;}", getStr, psiField.name, getStr))
            } else if (Arrays.asList("int", "long").contains(psiField.type.presentableText)) {
                methodStr.append(String.format("if (%s != 0) {%s = %s;}", getStr, psiField.name, getStr))
            } else if (Arrays.asList("float", "double").contains(psiField.type.presentableText)) {
                methodStr.append(String.format("if (%s != 0.0) {%s = %s;}", getStr, psiField.name, getStr))
            } else if ("boolean" == psiField.type.presentableText) {
                methodStr.append(String.format("%s = %s;", psiField.name, getStr))
            } else {
                methodStr.append(String.format("if (%s != null) {%s = %s;}", getStr, psiField.name, getStr))
            }
        }
        methodStr.append("return this;}")
        mBuilderPsiClass!!.add(mElementFactory.createMethodFromText(methodStr.toString(), mBuilderPsiClass))
    }

    private fun createReplaceConcurent() {
        mBuilderPsiClass!!.add(mElementFactory.createMethodFromText("public Builder() {}", mBuilderPsiClass))

        val classFieldName = mClassFileInfo.nameOfClassField
        val replaceMethodStr = StringBuilder(
            String.format(
                "public Builder(%s %s) {", mClassFileInfo.className,
                classFieldName
            )
        )
        for (psiField in mClassFileInfo.classFields) {
            replaceMethodStr.append(String.format("this.%s = %s.%s;", psiField.name, classFieldName, psiField.name))
        }
        replaceMethodStr.append("}")
        mBuilderPsiClass!!.add(mElementFactory.createMethodFromText(replaceMethodStr.toString(), mBuilderPsiClass))
    }

    private inner class Writter(val project: Project, val files: PsiFile) {

        fun run() {
            WriteCommandAction.writeCommandAction(project,files).run(ThrowableRunnable<Throwable> {
                if (mConfig!!.forceChangeToFinal) {
//                    changeFieldModifyType()
                }
                createFunBuilder(project)
//                createConstructMethod()
//                createStaticBuildMethod()
//                if (mConfig!!.createGetter) {
//                    createGetterMethods()
//                }
//                createInnerBuilderClass()
            })
        }
    }

    private fun createFunBuilder(project: Project) {
        val write = KtPsiFactory(project)

        val classNameFile = mClassFileInfo.ktClass?.name
        val methodStr = "fun ${classNameFile?.toLowerCase()}" +
                "(block : ${classNameFile}DslBuilder.() -> Unit): $classNameFile " +
                "= " +
                "${classNameFile}DslBuilder().apply(block).build()"
        val classBuilderString = "class ${classNameFile}DslBuilder() {\n" +
                createParameterClassDsl()+
                "    fun armbabo(block: ArmbaboAutoDslBuilder.() -> Unit): ArmAutoDslBuilder = this.apply { this.armbabo = ArmbaboAutoDslBuilder().apply(block).build() }\n" +
                "\n" +
                startBuild()
                "    fun build(): Arm = Arm(name, lop, armbabo)\n" +
                "}"
        val ktFun = write.createFunction(methodStr)
        val ktClass = write.createClass(classBuilderString)
        mClassFileInfo.ktClass?.body?.addAfter(ktFun,mClassFileInfo.ktClass?.lastChild)
        mClassFileInfo.ktClass?.body?.addAfter(ktClass,mClassFileInfo.ktClass?.lastChild)

    }

    private fun startBuild(): String? {

    }

    private fun createParameterClassDsl(): String {
        var parameter =""
        for (pararam in mClassFileInfo.parameterList) {
            parameter +="var ${pararam.name}: ${pararam.typeReference?.text} = ${pararam.defaultValue?.text}\n"
        }
        return parameter
    }
}
