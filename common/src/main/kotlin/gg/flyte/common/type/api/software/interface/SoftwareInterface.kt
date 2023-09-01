package gg.flyte.common.type.api.software.`interface`

interface SoftwareInterface {

    abstract fun getVersions(): List<String>
    abstract fun getDownloadUrl(version: String): String

}