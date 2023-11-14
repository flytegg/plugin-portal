package gg.flyte.common.util

fun String.alphaNumericOnly(): String {
    val regex = Regex("[^A-Za-z0-9 :]")
    return regex.replace(this, "")
}

fun String.addDashesToStringUUID(): String {
    return this.let {
        it.substring(0, 8) + "-" +
                it.substring(8, 12) + "-" +
                it.substring(12, 16) + "-" +
                it.substring(16, 20) + "-" +
                it.substring(20)
    }
}

fun Any.toJson() = GSON.toJson(this)


