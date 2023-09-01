package gg.flyte.common.type.api.user

data class Profile(
    val primaryUser: Pair<String, String>? = null,
    val uuid: MutableSet<String> = mutableSetOf(),
    val usernames: MutableSet<String> = mutableSetOf(),
    val usedPlatforms: MutableSet<PPPlatform> = mutableSetOf(),
)



