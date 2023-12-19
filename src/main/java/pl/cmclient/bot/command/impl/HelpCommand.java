package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

public class HelpCommand extends Command {

    public HelpCommand() {
        super(Commands.slash("help", "List of all commands"), CommandType.GENERAL, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyEmbeds(new CustomEmbed()
                        .create(CustomEmbed.Type.SUCCESS)
                        .setTitle("List of available commands")
                        .setThumbnail(event.getMember().getUser().getAvatarUrl())
                        .setDescription(this.getBot().getCommandManager().getCommandsList())
                        .build())
                .setEphemeral(true)
                .queue();
    }
}
