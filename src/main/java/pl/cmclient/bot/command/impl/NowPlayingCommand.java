package pl.cmclient.bot.command.impl;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.helper.StringHelper;

public class NowPlayingCommand extends Command {

    public NowPlayingCommand() {
        super(Commands.slash("nowplaying", "Display currently played song")
                        .setGuildOnly(true),
                CommandType.MUSIC, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        AudioTrack track = this.getBot().getMusicManager().getPlayingTrack(event.getGuild());

        if (track == null) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle("Currently i'm not playing any song.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.replyEmbeds(new CustomEmbed()
                        .create(CustomEmbed.Type.SUCCESS)
                        .setAuthor(track.getInfo().title, track.getInfo().uri, "https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg")
                        .setTitle("<:watch:901557828127449099> " + StringHelper.formatDuration(this.getBot().getMusicManager().getPosition(event.getGuild())))
                        .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg")
                        .build())
                .queue();
    }
}
