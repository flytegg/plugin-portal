package gg.flyte.pplib.util

fun String.alphaNumericOnly(): String{
    val regex = Regex("[^A-Za-z0-9 :]")
    return regex.replace(this, "")
}
