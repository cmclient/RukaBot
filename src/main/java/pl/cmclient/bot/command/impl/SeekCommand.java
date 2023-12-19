package pl.cmclient.bot.command.impl;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;

public class SeekCommand extends Command {

    public SeekCommand() {
        super(Commands.slash("seek", "Seek")
                        .addOption(OptionType.STRING, "time", "Format: <HH:MM:SS> | Example: 00:01:14", true)
                        .setGuildOnly(true),
                CommandType.MUSIC, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (!guild.getAudioManager().isConnected()) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle("I'm not connected to any channel.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        AudioTrack track = this.getBot().getMusicManager().getPlayingTrack(guild);

        if (track == null) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle("Currently i'm not playing any song.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (!track.isSeekable()) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle("This track is not seekable.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String time = event.getOption("time").getAsString();
        Duration duration = this.formatDuration(time);

        if (duration == null) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle("Invalid time format.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        this.getBot().getMusicManager().setPosition(guild, duration.toMillis(), TimeUnit.MILLISECONDS);

        event.replyEmbeds(new CustomEmbed()
                        .create(CustomEmbed.Type.SUCCESS)
                        .setAuthor(track.getInfo().title, track.getInfo().uri, "https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg")
                        .setTitle("<:watch:901557828127449099> " + time)
                        .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg")
                        .build())
                //.setEphemeral(true)
                .queue();
    }

    private Duration formatDuration(String duration) {
        try {
            return Duration.between(LocalTime.MIN, LocalTime.parse(duration));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
