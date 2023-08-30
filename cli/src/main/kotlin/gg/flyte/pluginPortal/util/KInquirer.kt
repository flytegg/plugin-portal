package gg.flyte.pluginPortal.util

import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.ListViewOptions
import com.github.kinquirer.components.promptList

fun KInquirer.promptBetterList(
    message: String,
    choices: List<String> = emptyList(),
    hint: String = "",
    pageSize: Int = 7,
    viewOptions: ListViewOptions = ListViewOptions(
        questionMarkPrefix = "✅",
        cursor = " ❯ ",
        nonCursor = "   "
    )

): String {
    return KInquirer.promptList(
        message = message,
        choices = choices,
        hint = hint,
        pageSize = pageSize,
        viewOptions = viewOptions
    )
}