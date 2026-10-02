package gg.flyte.pluginportal.plugin

internal object MarketplaceKey {
    // These strings are rewritten in compiled classes by marketplace delivery.
    fun embedded(): String? = resolve("%%__LICENSE__%%", "%%__BBB_LICENSE__%%")

    fun resolve(polymartLicense: String, builtByBitLicense: String): String? =
        rewritten(builtByBitLicense) ?: rewritten(polymartLicense)?.let { "pm_$it" }

    private fun rewritten(value: String): String? = value.trim()
        .takeIf { it.isNotEmpty() && !it.startsWith("%%__") }
}
