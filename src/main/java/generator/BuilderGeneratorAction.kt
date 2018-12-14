package generator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import generator.ui.SettingDialog

/**
 */
class BuilderGeneratorAction : AnAction() {
    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val currentFile = anActionEvent.getData(LangDataKeys.PSI_FILE)
        val project = anActionEvent.getData(LangDataKeys.PROJECT)

        val configSetting = Config()
        configSetting.forceChangeToFinal = true
        configSetting.overlay = true
        configSetting.createGetter = true
        val settingDialog = SettingDialog()
        settingDialog.setOnClickListner(object: SettingDialog.OnClickListner {
            override fun onClick(config: Config) {
                currentFile?.children
                BuilderClassCreator(project!!, currentFile!!, config).generate()
            }
        })
        settingDialog.isVisible = true

    }


}
