package gg.flyte.pluginportal.util

import gg.flyte.twilight.shaded.gson.Gson
import gg.flyte.twilight.shaded.gson.GsonBuilder

val gson: Gson = GsonBuilder()
    .setPrettyPrinting()
    .setLenient()
    .create()