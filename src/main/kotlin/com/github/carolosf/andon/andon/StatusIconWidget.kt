package com.github.carolosf.andon.andon

import com.github.carolosf.andon.andon.AndonSharedState.Companion.ICON_BROKEN
import com.github.carolosf.andon.andon.AndonSharedState.Companion.ICON_OK
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.IconUtil
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import javax.swing.Icon

class StatusIconWidget : StatusBarWidget,  StatusBarWidget.IconPresentation {
    override fun ID(): String = Factory.ID
    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this
    // Below don't seem to work
    override fun getTooltipText(): String = "Build Status"
//    override fun getShortcutText(): String = "Hi"

    override fun getIcon(): Icon {
        val icon = if (AndonSharedState.allHealthy()) {
            ICON_OK
        } else {
            ICON_BROKEN
        }
        val scale = JBUI.scale(AndonSharedState.ICON_HEIGHT).toFloat() / icon.iconHeight

        return IconUtil.scale(icon, null,  scale)
    }

    internal class Factory : StatusBarWidgetFactory {
        companion object {
            const val ID = "com.github.carolosf.andon"
        }
        override fun getId(): String = ID
        @NlsSafe
        override fun getDisplayName(): String = "Andon"
        override fun isAvailable(project: Project): Boolean = true

        override fun createWidget(project: Project, scope: CoroutineScope): StatusBarWidget = StatusIconWidget()

//    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
    }
}
