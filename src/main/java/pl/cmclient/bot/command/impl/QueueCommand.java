package pl.cmclient.bot.command.impl;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class QueueCommand extends Command {

    public QueueCommand() {
        super(Commands.slash("queue", "Displays song queue")
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

        BlockingQueue<AudioTrack> queue = this.getBot().getMusicManager().get(guild).getScheduler().getQueue();

        if (queue.isEmpty()) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle("Song queue is empty.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        AtomicInteger counter = new AtomicInteger(1);
        String result = queue.stream()
                .map(audioTrack -> counter.getAndIncrement() + ". " + audioTrack.getInfo().title + '\n')
                .collect(Collectors.joining());

        event.replyEmbeds(new CustomEmbed()
                        .create(CustomEmbed.Type.SUCCESS)
                        .setTitle("Song queue:")
                        .setDescription(result)
                        .build())
                //.setEphemeral(true)
                .queue();
    }
}
