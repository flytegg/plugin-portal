package gg.flyte.pluginportal.paper.command.info

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.components.GuiType
import dev.triumphteam.gui.guis.Gui
import gg.flyte.pluginportal.api.type.CompactPlugin
import gg.flyte.pluginportal.extensions.getSha256Hash
import gg.flyte.pluginportal.paper.PluginPortal
import gg.flyte.pluginportal.paper.plugin.PPPluginCache
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission
import java.io.File

@Command("pp", "pluginportal", "ppm", "pportal")
class MenuSubCommand {

    @Subcommand("menu")
    @CommandPermission("pluginportal.command.menu")
    fun onMenuCommand(
        sender: Player
    ) {
        println("Opening gui")

        Gui.gui(GuiType.HOPPER)
            .title(text("Plugin Portal Panel", NamedTextColor.BLACK, TextDecoration.BOLD))
            .disableAllInteractions().apply {
                println("Disabled interactions")
            }
            .create()
            .apply {
                setItem(
                    1,
                    ItemBuilder.from(Material.BOOK)
                        .name(
                            text("Config (COMING SOON)", NamedTextColor.GRAY, TextDecoration.BOLD).unDecorate()
                        )
                        .asGuiItem()
                )

                setItem(
                    3,
                    ItemBuilder.from(Material.COMPASS)
                        .name(text("Plugins", NamedTextColor.AQUA, TextDecoration.BOLD).unDecorate())
                        .asGuiItem { sender.openPluginsMenu() }
                )

                filler.fill(
                    ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                        .name(text("").decoration(TextDecoration.ITALIC, false))
                        .asGuiItem()
                )

                setDefaultClickAction {
                    println("Default click action")
                    it.isCancelled = true
                }
            }
            .open(sender)
    }

    private fun Player.openPluginsMenu() {
        val gui = Gui.paginated()
            .title(text("Plugins", NamedTextColor.BLACK, TextDecoration.BOLD))
            .rows(6)
            .disableAllInteractions()
            .create()

        gui.apply {
            val usedPlugins = hashSetOf<String>()

            server.pluginManager.plugins
                .filter { it.name != "PluginPortal" }
                .forEach { plugin ->
                    val compactPlugin: CompactPlugin? =
                        File(plugin.javaClass.protectionDomain.codeSource.location.toURI().path).let { file ->
                            PPPluginCache.getInstalledPlugins()
                                .firstOrNull { plugin -> plugin.sha256.equals(file.getSha256Hash(), true) }
                        }

                    if (compactPlugin != null) usedPlugins.add(compactPlugin.id)

                    addItem(
                        if (compactPlugin == null) {
                            ItemBuilder.from(Material.RED_CONCRETE)
                                .name(
                                    text(
                                        plugin.name,
                                        NamedTextColor.WHITE,
                                        TextDecoration.BOLD
                                    ).unDecorate()
                                ).lore(
                                    listComponent(text("Version: ", NamedTextColor.GRAY)
                                        .append(text(plugin.description.version, NamedTextColor.AQUA))),
                                    text(""),
                                    text("Left Click to view more info (COMING SOON)", NamedTextColor.GRAY).unDecorate(),
                                )
                                .asGuiItem()
                        } else {
                            ItemBuilder.from(Material.GREEN_CONCRETE)
                                .name(
                                    text(
                                        compactPlugin.name,
                                        NamedTextColor.AQUA,
                                        TextDecoration.BOLD
                                    ).unDecorate()
                                ).lore(
                                    listComponent(text("Version: ", NamedTextColor.GRAY)
                                        .append(text(compactPlugin.version ?: "N/A", NamedTextColor.AQUA))),
                                    listComponent(text("ID: ", NamedTextColor.GRAY)
                                        .append(text(compactPlugin.id, NamedTextColor.AQUA))),
                                    text(""),
                                    text("Left Click to view more info (COMING SOON)", NamedTextColor.GRAY).unDecorate(),
                                )
                                .asGuiItem()
                        }
                    )
                }

            PPPluginCache.getInstalledPlugins()
                .filter { !usedPlugins.contains(it.id) }
                .forEach {
                    addItem(
                        ItemBuilder.from(Material.GRAY_CONCRETE)
                            .name(
                                text(
                                    "${it.name} | UNLOADED",
                                    NamedTextColor.GRAY,
                                    TextDecoration.BOLD
                                ).unDecorate()
                            ).asGuiItem()
                    )
                }

            filler.fillBetweenPoints(
                5, 1, 6, 9,
                ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                    .name(text("").unDecorate())
                    .asGuiItem()
            )

            setItem(6, 3, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                .name(
                    text("Previous")
                        .unDecorate()
                        .decorate(TextDecoration.BOLD)
                        .color(NamedTextColor.RED)
                ).asGuiItem { previous() })

            setItem(6, 7, ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE)
                .name(
                    text("Next")
                        .unDecorate()
                        .decorate(TextDecoration.BOLD)
                        .color(NamedTextColor.GREEN)
                ).asGuiItem { next() }
            )

            setItem(
                6, 5, ItemBuilder.from(Material.COMPASS)
                    .name(
                        text("Info")
                            .unDecorate()
                            .decorate(TextDecoration.BOLD)
                            .color(NamedTextColor.WHITE)
                    )
                    .lore(
                        listComponent(text("Version: ", NamedTextColor.GRAY)
                            .append(text(PluginPortal.instance.description.version, NamedTextColor.AQUA))),
                        listComponent(text("Outdated: ", NamedTextColor.GRAY)
                            .append(text("TODO", NamedTextColor.AQUA))),
                    )
                    .asGuiItem()
            )
        }

        gui.open(this)
    }

    private fun Player.openPluginMenu(compactPlugin: CompactPlugin) {
        Gui.gui(GuiType.HOPPER)
            .title(text(compactPlugin.name, NamedTextColor.BLACK, TextDecoration.BOLD))
            .disableAllInteractions()
            .create()
            .apply {
                setItem(
                    1,
                    ItemBuilder.from(Material.BOOK)
                        .name(
                            text("Info", NamedTextColor.GRAY, TextDecoration.BOLD).unDecorate()

                        )
                        .asGuiItem { }
                )

                setItem(
                    3,
                    ItemBuilder.from(Material.COMPASS)
                        .name(text("Update", NamedTextColor.AQUA, TextDecoration.BOLD).unDecorate())
                        .asGuiItem()
                )

                filler.fill(
                    ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                        .name(text("").decoration(TextDecoration.ITALIC, false))
                        .asGuiItem()
                )
            }
            .open(this)
    }

    private fun TextComponent.unDecorate() = decoration(TextDecoration.ITALIC, false)

    private fun listComponent(component: TextComponent) = text("- ", NamedTextColor.DARK_GRAY)
        .unDecorate()
        .append(component.unDecorate())
}

