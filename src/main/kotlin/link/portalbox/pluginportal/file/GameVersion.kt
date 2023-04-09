package link.portalbox.pluginportal.file

data class GameVersion(val major: Int, val minor: Int, val patch: Int) {
    operator fun compareTo(other: GameVersion): Int {
        if (major != other.major) return major - other.major
        if (minor != other.minor) return minor - other.minor
        return patch - other.patch
    }
}
