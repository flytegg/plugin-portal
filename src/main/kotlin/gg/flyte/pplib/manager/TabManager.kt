package gg.flyte.pplib.manager

import gg.flyte.pplib.util.searchPlugins

class TabManager {

    // HashMap<Key, Values>
    val searchList = HashMap<String, HashSet<String>>()

    fun searchTerms(filter: String) {
        if (filter.length <= 2 || searchList.keys.any { filter.startsWith(it, true) }) return
        searchList[filter] = HashSet(searchPlugins(filter))
    }

    fun getTableComplete(input: String): MutableCollection<String> = searchList
        .filter { input.contains(it.key, true) }
        .map {
            it.value.filter { set -> set.contains(input, true) }
        }
        .flatten()
        .toMutableSet()
}