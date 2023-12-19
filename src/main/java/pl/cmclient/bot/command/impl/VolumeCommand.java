package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

public class VolumeCommand extends Command {

    public VolumeCommand() {
        super(Commands.slash("volume", "Change volume")
                        .addOption(OptionType.NUMBER, "volume", "Volume", true)
                        .setGuildOnly(true),
                CommandType.MUSIC, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.getGuild().getAudioManager().isConnected()) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle("I'm not connected to any channel.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        int volume = event.getOption("volume").getAsInt();
        if (volume <= 0 || volume > 500) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle("Minimum volume is 1, maximum is 500")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        this.getBot().getMusicManager().setVolume(volume, event.getGuild(), event.getInteraction().getChannel().asTextChannel());
    }
}
