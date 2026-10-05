package gg.flyte.pluginportal.plugin.network

import com.google.gson.JsonObject
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.plugin.PortalApplication
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import revxrsal.commands.autocomplete.AsyncSuggestionProvider
import revxrsal.commands.node.ExecutionContext
import java.util.concurrent.CompletableFuture

object ServerTargets {
    private val targetArgument = Regex("""(?:^|\s)(?:--servers?|-s|-S)\s+("[^"]*"?|[^\s"]*)$""")

    fun completingTarget(input: String): String? = targetArgument.find(input)?.groupValues?.get(1)

    fun complete(actor: PortalCommandActor, input: String): List<String> {
        val suggestions = PluginPortalBase.lamp.autoCompleter().complete(actor, input)
        if (completingTarget(input)?.startsWith('"') != false) return suggestions
        return suggestions.map { if (' ' in it && !it.startsWith('"')) "\"$it\"" else it }.distinct()
    }

    fun requested(actor: PortalCommandActor, server: String?, servers: String?, task: (String) -> Unit): Boolean {
        if (server == null && servers == null) return false
        if (!actor.hasPermission("pluginportal.network")) actor.audience.sendFailure("You do not have permission to target other servers.")
        else if (server != null && servers != null) actor.audience.sendFailure("Use --server or --servers, not both.")
        else work(actor) { task(server ?: servers!!) }
        return true
    }

    fun resolve(selection: String): List<JsonObject> {
        val names = selection.split(',').map(String::trim)
        require(names.isNotEmpty() && names.size <= 100 && names.all { it.isNotEmpty() }) { "Select 1 to 100 server names or UUIDs, separated by commas." }
        val nodes = PortalApplication.network.state().getAsJsonArray("nodes").map { it.asJsonObject }.filter { !it.get("revoked").asBoolean }
        val selected = names.map { name ->
            val matches = nodes.filter { it.get("id").asString.equals(name, true) || it.get("name").asString.equals(name, true) }
            require(matches.isNotEmpty()) { "Server '$name' was not found. Use /pp servers." }
            require(matches.size == 1) { "More than one server is named '$name'. Use its UUID from /pp servers." }
            matches.single()
        }
        require(selected.map { it.get("id").asString }.distinct().size == selected.size) { "A server was selected more than once." }
        return selected
    }

    fun install(actor: PortalCommandActor, selection: String, name: String, platform: MarketplacePlatform?, byId: Boolean, exact: Boolean, version: String?, channel: String?) {
        val targets = resolve(selection)
        MarketplacePluginCache.handlePluginSearchFeedback(actor.audience, name, platform, byId,
            ifSingle = { plugin -> work(actor) {
                val source = platform?.let { plugin.platform(it) } ?: plugin.platforms.bestDownloadable
                    ?: error("No downloadable marketplace was found. Specify a marketplace.")
                submit(actor, targets, action("install", source.platform.name, source.platformId, version, channel))
            } },
            ifMore = { actor.audience.sendFailure("More than one plugin matches '$name'. Use its exact name or marketplace project ID with --byId.") }, exact = exact)
    }

    fun change(actor: PortalCommandActor, selection: String, kind: String, name: String, byId: Boolean, version: String? = null, channel: String? = null) {
        val targets = resolve(selection)
        val identities = targets.map { node ->
            val plugins = node.getAsJsonObject("inventory")?.getAsJsonArray("plugins")?.map { it.asJsonObject }
                ?: error("No inventory is available for ${node.get("name").asString}. Wait for it to connect.")
            val matches = plugins.filter { plugin -> plugin.get("id") != null &&
                (if (byId) plugin.get("id").asString == name else plugin.get("name").asString.equals(name, true)) }
            require(matches.size == 1) { "Select one tracked plugin on ${node.get("name").asString}. Use /pp list --server <name> to check its name or ID." }
            matches.single().let { it.get("platform").asString to it.get("id").asString }
        }.distinct()
        require(identities.size == 1) { "The plugin uses different marketplace listings on these servers. Select servers with the same listing together." }
        submit(actor, targets, action(kind, identities.single().first, identities.single().second, version, channel))
    }

    fun list(actor: PortalCommandActor, selection: String, page: Int?, full: Boolean, untracked: Boolean) {
        require(page == null || page >= 1) { "Page must be at least 1." }
        require(page == null || !full) { "Use --page or --full, not both." }
        val nodes = resolve(selection)
        val rows = nodes.flatMap { node ->
            val plugins = node.getAsJsonObject("inventory")?.getAsJsonArray("plugins")?.map { it.asJsonObject }
                ?: error("No inventory is available for ${node.get("name").asString}.")
            plugins.filter { !untracked || it.get("id") == null }.map { plugin -> node.get("name").asString to {
                textPrimary(plugin.get("name").asString).append(textSecondary("  ${plugin.get("version").asString}"))
                    .append(textDark(plugin.get("id")?.asString?.let { "  ${plugin.get("platform").asString} $it" } ?: "  Unmanaged"))
            } }
        }
        if (rows.isEmpty()) actor.audience.sendInfo("No matching plugins on the selected servers.")
        else sendPagedRows(actor.audience, rows, page, full, "/pp list --servers ${nodes.joinToString(",") { it.get("id").asString }}" + if (untracked) " --untracked" else "")
    }

    private fun action(kind: String, platform: String, id: String, version: String?, channel: String?) = NetworkClient.jsonObject("kind" to kind, "platform" to platform, "id" to id).apply {
        version?.let { addProperty("version", it) }; channel?.let { addProperty("channel", it) }
    }
    private fun submit(actor: PortalCommandActor, nodes: List<JsonObject>, action: JsonObject) {
        val operation = PortalApplication.network.submit(nodes.map { it.get("id").asString }, action)
        actor.audience.sendMessage(Component.empty().append(textPrimary("Operation submitted").bold())
            .appendNewline().append(textSecondary(nodes.joinToString(", ") { it.get("name").asString }))
            .appendNewline().append(textPrimary("[Results]").clickEvent(ClickEvent.runCommand("/pp history ${operation.get("id").asString}")))
            .appendNewline().append(textDark("Offline servers are skipped. Restart to apply JAR changes.")).boxed())
    }
    private fun work(actor: PortalCommandActor, task: () -> Unit) {
        PortalApplication.runtime.executor.execute { try { task() } catch (failure: Exception) { actor.audience.sendFailure(failure.message ?: "Server request failed") } }
    }
}

class ServerTargetSuggestionProvider : AsyncSuggestionProvider<PortalCommandActor> {
    override fun getSuggestionsAsync(context: ExecutionContext<PortalCommandActor>): CompletableFuture<Collection<String>> {
        if (!context.actor().hasPermission("pluginportal.network")) return CompletableFuture.completedFuture(emptyList())
        val argument = ServerTargets.completingTarget(context.input().source())?.trim('"') ?: ""
        val prefix = if (',' in argument) argument.substringBeforeLast(',') + "," else ""
        val names = PortalApplication.network.serverNameSuggestions()
            .filter { ',' !in it && '"' !in it && it !in prefix.split(',') }
            .flatMap { name -> (prefix + name).let { if (' ' in it) listOf(it, "\"$it\"") else listOf(it) } }
        return CompletableFuture.completedFuture(names)
    }
}
