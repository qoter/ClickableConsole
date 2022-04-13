package com.jetbrains.rider.plugins.clickableconsole

import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.openapi.project.Project

class ClickableLinkConsoleFilterProvider : ConsoleFilterProvider {
    override fun getDefaultFilters(p0: Project): Array<Filter> {
        return arrayOf(ClickableLinkFilter())
    }
}
