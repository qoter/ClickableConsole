package com.jetbrains.rider.plugins.clickableconsole

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.LocalFileSystem

class ClickableLinkFilter : Filter {
    private val LOG: Logger = Logger.getInstance(ClickableLinkFilter::class.java)
    private val EMPTY_FILTER = Filter.Result(emptyList())
    private val ClickableLinkRegex: Regex = Regex("""^(\<.*?\>) \$\((.*)\) (.*)""")

    override fun applyFilter(line: String, endPoing: Int): Filter.Result {
        LOG.info("LINE: $line")
        val startPoint = endPoing - line.length;
        val matchResult = ClickableLinkRegex.find(line) ?: return EMPTY_FILTER

        val text = matchResult.groups[1] ?: return EMPTY_FILTER
        val type = matchResult.groups[2]?.value ?: return EMPTY_FILTER
        val command = matchResult.groups[3]?.value ?: return EMPTY_FILTER

        val linkStart = startPoint + text.range.first
        val linkEnd = startPoint + text.range.last + 1

        val hyperLinkInfo = getHyperlinkInfo(type, command) ?: return EMPTY_FILTER

        return Filter.Result(linkStart, linkEnd, hyperLinkInfo)
    }

    private fun getHyperlinkInfo(type: String, command: String): HyperlinkInfo? {
        return when (type) {
            "rider" -> getRiderCommand(command)
            "term" -> getTermCommand(command)
            else -> null
        }
    }

    private fun getTermCommand(command: String): HyperlinkInfo {
        return HyperlinkInfo {
            ShellTerminalRunner().run(it, command, "ClickableConsole.Terminal", false)
        }
    }


    private fun getRiderCommand(command: String): HyperlinkInfo? {
        val args = splitCommand(command)
        if (args.isEmpty()) {
            return null
        }

        val commandName = args[0]

        return when (commandName) {
            "diff" -> getRiderDiffCommand(args)
            "navigate" -> getRiderNavigateCommand(args)
            else -> null
        }
    }

    private fun getRiderNavigateCommand(args: List<String>): HyperlinkInfo? {
        if (args.size < 2) {
            return null
        }

        return HyperlinkInfo {
            val file = LocalFileSystem.getInstance().refreshAndFindFileByPath(args[1]) ?: return@HyperlinkInfo
            ProjectView.getInstance(it).select(null, file, true)
        }
    }

    private fun getRiderDiffCommand(args: List<String>): HyperlinkInfo {
        return HyperlinkInfo {
            val localFileSystem = LocalFileSystem.getInstance()
            val contents = args
                    .drop(1)
                    .take(2)
                    .mapNotNull { p -> localFileSystem.refreshAndFindFileByPath(p) }
                    .map { f -> DiffContentFactory.getInstance().create(it, f) }

            if (contents.size != 2) {
                return@HyperlinkInfo
            }

            DiffManager.getInstance().showDiff(it, SimpleDiffRequest(null, contents[0], contents[1], args[1], args[2]))
        }
    }

    private fun splitCommand(command: String): List<String> {
        val args = mutableListOf<String>()
        var currentArg = "";
        var quotedArg = false;

        for (c in command) {
            if (quotedArg && c == '"') {
                quotedArg = false
            } else if (c == '"') {
                quotedArg = true;
            } else if (c.isWhitespace() && !quotedArg) {
                args.add(currentArg)
                currentArg = ""
            } else {
                currentArg += c
            }
        }
        if (currentArg.isNotEmpty()) {
            args.add(currentArg)
        }

        return args
    }
}