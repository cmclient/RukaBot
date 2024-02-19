package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

public class PingCommand extends Command {

    public PingCommand() {
        super(Commands.slash("ping", "Response time from Discord Gateway"), CommandType.GENERAL, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.getJDA().getRestPing().queue(restPing -> event.replyEmbeds(
                        new CustomEmbed()
                                .create(CustomEmbed.Type.SUCCESS)
                                .setTitle(":watch: Response time")
                                .addField("Gateway", event.getJDA().getGatewayPing() + "ms", false)
                                .addField("Rest", restPing + "ms", false)
                                .build())
                .setEphemeral(true)
                .queue());
    }
}
