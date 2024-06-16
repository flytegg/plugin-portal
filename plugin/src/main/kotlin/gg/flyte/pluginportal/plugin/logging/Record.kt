package gg.flyte.pluginportal.plugin.logging

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

/**
 * @param timestamp The time in millis at which this record took place
 * @param initiator The user or action that initiated this action
 * @param action The action that took place
 * @param target The plugin affected by the action
 *
 */
class Record(
    val timestamp: Long,
    val initiator: String,
    val action: PortalLogger.Action,
    val target: String,
) {
    private val date = Date.from(Instant.ofEpochMilli(timestamp))

    val timelessDescription = "$initiator $action $target"
    val description: String = "[${dateFormat.format(date)}] $timelessDescription"

    override fun toString() = description

    companion object {
        private val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm:ss")

        fun read(record: String): Record {
            val time = dateFormat.parse(record.substring(1, 18)).time
            val action = PortalLogger.Action.entries.find { record.contains(it.name) }!! // Should never be null
            val (initiator, target) = record.substring(20).split(" $action ")
            return Record(time, initiator, action, target)
        }
    }

}