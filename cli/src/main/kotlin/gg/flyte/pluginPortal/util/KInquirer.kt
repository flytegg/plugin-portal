package gg.flyte.pluginPortal.util

import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.ListViewOptions
import com.github.kinquirer.components.promptInput
import com.github.kinquirer.components.promptList

fun KInquirer.promptBetterList(
    message: String,
    choices: List<String> = emptyList(),
    hint: String = "",
    pageSize: Int = 7,
    viewOptions: ListViewOptions = ListViewOptions(
        //questionMarkPrefix = "❓",
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

fun KInquirer.promptBetterInput(
    message: String,
    default: String = "",
    hint: String = "",
    validation: (s: String) -> Boolean = { true },
    filter: (s: String) -> Boolean = { true },
    transform: (s: String) -> String = { it }
): String {
    return KInquirer.promptInput(
        message,
        default,
        hint,
        validation,
        filter,
        transform
    )
}