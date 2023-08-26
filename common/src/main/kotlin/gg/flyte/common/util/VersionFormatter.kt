package gg.flyte.common.util

data class SemVer(val major: Int, val minor: Int, val patch: Int)

fun parseSemVer(version: String): SemVer {
    version.split('.').map { it.toIntOrNull() }.let {
        return SemVer(
            it.getOrNull(0) ?: 0,
            it.getOrNull(1) ?: 0,
            it.getOrNull(2) ?: 0
        )
    }
}

fun sortSemVerVersions(versions: List<String>): List<String> {
    val semVerList = versions.map { parseSemVer(it) }
    val sortedSemVerList = semVerList.sortedWith(
        compareBy(SemVer::major, SemVer::minor, SemVer::patch)
    )
    return sortedSemVerList.map { "${it.major}.${it.minor}.${it.patch}".removeSuffix(".0") }
}

fun getVersionRange(versions: List<String>): String {
    sortSemVerVersions(versions).let {
        "${it.first()}-${it.last()}".let { range ->
            return if (it.first() == it.last()) it.first()
            else range
        }
    }
}