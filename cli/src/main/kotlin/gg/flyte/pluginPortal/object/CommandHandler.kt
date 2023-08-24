package gg.flyte.pluginPortal.`object`

import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptConfirm
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Default
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.cli.ConsoleCommandHandler
import revxrsal.commands.command.CommandActor
import kotlin.concurrent.thread

@Command("pp")
object CommandHandler {

    fun init() {
        val commandHandler = ConsoleCommandHandler.create()
        commandHandler.register(this)

        thread {
            while (true) {
                commandHandler.pollInput()
            }
        }
    }

    @Subcommand()
    fun help(
        actor: CommandActor,
        @Default("") args: String
    ) {
        actor.reply("Hello, world!")
        KInquirer.promptConfirm("Print Args?").let {
            if (it) actor.reply(args.replace("help", ""))
            else actor.reply("No Args will be printed")
        }
    }

}