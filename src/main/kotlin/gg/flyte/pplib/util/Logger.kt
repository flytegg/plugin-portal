package gg.flyte.pplib.util

import com.andreapivetta.kolor.*
import gg.flyte.pplib.type.logger.LogType
import gg.flyte.pplib.type.logger.StatusType

fun log(message: String) = println(message)

fun log(url: String, status: StatusType, type: LogType) {
    when (status) {
        StatusType.LOADING -> println("${type.name.lightYellow()} > $url > ${status.name.lightMagenta()}")
        StatusType.ERROR -> println("${type.name.lightYellow()} > $url > ${status.name.red()}")
        StatusType.WARNING -> println("${type.name.lightYellow()} > $url > ${status.name.yellow()}")
        StatusType.OK -> println("${type.name.lightYellow()} > $url > ${status.name.green()}")
    }
}