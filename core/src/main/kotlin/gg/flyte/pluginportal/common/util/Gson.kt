package gg.flyte.pluginportal.common.util

import com.google.gson.GsonBuilder

val GSON = GsonBuilder()
    .setPrettyPrinting()
    .create()

val GSON_COMPACT = GsonBuilder()
    .create()