package com.github.carolosf.andon.andon

import com.intellij.openapi.ui.Messages
import org.junit.Assert
import org.junit.Test

class ReminderProjectActivityTest {
    @Test
    fun ensureIconIsCorrect() {
        Assert.assertEquals(Messages.getErrorIcon(), ReminderProjectActivity.getIcon("error"))
        Assert.assertEquals(Messages.getWarningIcon(), ReminderProjectActivity.getIcon("warn"))
        Assert.assertEquals(Messages.getInformationIcon(), ReminderProjectActivity.getIcon("info"))
        Assert.assertEquals(Messages.getQuestionIcon(), ReminderProjectActivity.getIcon("question"))
        Assert.assertEquals(Messages.getWarningIcon(), ReminderProjectActivity.getIcon("blah"))
    }
}