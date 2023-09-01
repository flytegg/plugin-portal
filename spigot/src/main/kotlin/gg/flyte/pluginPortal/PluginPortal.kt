package gg.flyte.pluginPortal

import gg.flyte.pluginPortal.command.PPCommand
import gg.flyte.pluginPortal.type.Config
import io.papermc.lib.PaperLib
import me.superpenguin.superglue.foundations.customevents.CustomEventListener
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler
import revxrsal.commands.orphan.OrphanCommand
import revxrsal.commands.orphan.Orphans

class PluginPortal : JavaPlugin() {
    override fun onEnable() {
        Config.init(this)

        BukkitCommandHandler.create(this).apply {
            register(PPCommand())
            fastRegister(

            )
        }.registerBrigadier()


        Metrics(this, 18005)
        PaperLib.suggestPaper(this)
    }

    override fun onDisable() {
        logger.info("PluginPortal has been disabled!")
    }

    fun BukkitCommandHandler.fastRegister(vararg commands: OrphanCommand) {
        commands.forEach { register(Orphans.path().handler(it)) }
    }
}