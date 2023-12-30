package gg.flyte.pluginportal.bukkit.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder

val gson: Gson = GsonBuilder()
    .setPrettyPrinting()
    .setLenient()
    .create()