package gg.flyte.common.type.logger

import com.github.ajalt.mordant.rendering.TextColors

enum class StatusType(val color: TextColors) {
    OK(TextColors.green),
    LOADING(TextColors.magenta),
    WARNING(TextColors.yellow),
    ERROR(TextColors.red),
}