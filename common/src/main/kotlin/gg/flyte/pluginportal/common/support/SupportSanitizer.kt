package gg.flyte.pluginportal.common.support

object SupportSanitizer {
    private data class Rule(val name: String, val pattern: Regex, val replacement: String)

    private val rules = listOf(
        Rule("authorization", Regex("""(?i)\b(authorization\s*[:=]\s*)(bearer\s+)?[^\s,;]+"""), "$1<redacted>"),
        Rule("apiKey", Regex("""(?i)\b(x-api-key|api[-_ ]?key|authentication\.apikey|pluginportal key|mclicense key)\s*[:=]\s*["']?[^"'\s,;]+"""), "$1=<redacted>"),
        Rule("pluginPortalKey", Regex("""\bpps?_[A-Za-z0-9._-]{12,}\b"""), "<redacted-plugin-portal-key>"),
        Rule("token", Regex("""(?i)\b(token|secret|password|passwd|pwd)\s*[:=]\s*["']?[^"'\s,;]+"""), "$1=<redacted>"),
        Rule("urlToken", Regex("""(?i)([?&](?:token|key|signature|auth|password|code)=)[^&#\s]+"""), "$1<redacted>"),
        Rule("email", Regex("""(?i)\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b"""), "<redacted-email>"),
        Rule("ipv4", Regex("""\b(?:(?:25[0-5]|2[0-4]\d|1?\d?\d)\.){3}(?:25[0-5]|2[0-4]\d|1?\d?\d)\b"""), "<redacted-ip>"),
        Rule("ipv6", Regex("""(?i)\b(?:[a-f0-9]{1,4}:){2,7}[a-f0-9]{1,4}\b"""), "<redacted-ip>")
    )

    fun sanitize(text: String, summary: MutableMap<String, Int>): String {
        var output = text
        rules.forEach { rule ->
            val count = rule.pattern.findAll(output).count()
            if (count > 0) {
                summary[rule.name] = (summary[rule.name] ?: 0) + count
                output = rule.pattern.replace(output, rule.replacement)
            }
        }
        return output
    }
}
