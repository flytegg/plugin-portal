package gg.flyte.pplib.manager

import gg.flyte.pplib.util.searchPlugins

class TabManager {

    // HashMap<Key, Values>
    private val searchList = HashMap<String, HashSet<String>>()

    fun searchTerms(filter: String) {
        if (filter.length <= 2) return
        for (key in searchList.keys) {
            if (filter.startsWith(key, true)) {
                return
            }
        }

        searchList[filter] = HashSet<String>().apply {
            addAll(searchPlugins(filter))
        }
    }

    fun getTableComplete(input: String): MutableCollection<String> {
        val pluginList = mutableListOf<String>()

        for (key in searchList.keys) {
            if (input.contains(key, true)) {
                for (value in searchList[key]!!) {
                    if (value.contains(input, true)) {
                        pluginList.add(value)
                    }
                }
            }
        }

        return pluginList
    }

    fun getSearchList(): HashMap<String, HashSet<String>> {
        return searchList
    }
}