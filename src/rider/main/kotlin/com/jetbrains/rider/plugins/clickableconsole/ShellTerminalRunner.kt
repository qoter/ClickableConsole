package com.jetbrains.rider.plugins.clickableconsole

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager
import com.jetbrains.rd.util.parseFromFlags
import org.jetbrains.plugins.terminal.ShellTerminalWidget
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory
import org.jetbrains.plugins.terminal.TerminalView
import java.io.IOException
import java.util.*

class ShellTerminalRunner {
    private val LOG: Logger = Logger.getInstance(ClickableLinkFilter::class.java)

    fun run(project: Project,
            command: String,
            title: String,
            activateToolWindow: Boolean) {
        val terminalView = TerminalView.getInstance(project)
        val window = ToolWindowManager.getInstance(project)
                .getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID)
                ?: return

        val pair = getSuitableTerminal(window.contentManager, title)
        try {
            if (pair == null) {
                terminalView
                        .createLocalShellWidget(null, title, activateToolWindow, activateToolWindow)
                        .executeCommand(command)
                return
            }
            val (content, terminal) = pair

            if (activateToolWindow) {
                window.activate(null)
            }

            window.contentManager.setSelectedContent(content)
            terminal.executeCommand(command)
        } catch (e: IOException) {
            LOG.warn("Cannot run command:$command", e)
        }
    }

    private fun getSuitableTerminal(contentManager: ContentManager, title: String): Pair<Content, ShellTerminalWidget>? {
        for (content in contentManager.contents) {
            if (content.displayName != title)
                continue
            val terminal = TerminalView.getWidgetByContent(content) as? ShellTerminalWidget ?: continue

            if (terminal.typedShellCommand.isNotEmpty() || terminal.hasRunningCommands())
                continue

            return Pair(content, terminal)
        }

        return null
    }
}