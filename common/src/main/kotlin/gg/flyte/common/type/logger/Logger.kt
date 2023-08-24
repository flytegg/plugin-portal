package gg.flyte.common.type.logger

import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.github.ajalt.mordant.terminal.Terminal


object Logger {

    var enabled = true
    val terminal = Terminal()

    fun logSuccess(message: String, type: LogType) {
        log(message, StatusType.OK, type)
    }

    fun logLoading(message: String, type: LogType) {
        log(message, StatusType.LOADING, type)
    }

    fun loadWarning(message: String, type: LogType) {
        log(message, StatusType.WARNING, type)
    }

    fun logError(message: String, type: LogType) {
        log(message, StatusType.ERROR, type)
    }

    fun log(message: String, status: StatusType, logType: LogType) {
        if (!enabled) return

        println("${yellow(logType.name)} > ${status.color(message)}")
    }

}


fun Int.getStatusType(): StatusType {
    when (this) {
        in 100..199 -> return StatusType.LOADING
        in 200..299 -> return StatusType.OK
        in 300..399 -> return StatusType.WARNING
        in 400..499 -> return StatusType.ERROR
        in 500..599 -> return StatusType.ERROR
    }

    return StatusType.ERROR
}