package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

import java.util.ArrayList;

public class PlayCommand extends Command {

    public PlayCommand() {
        super(Commands.slash("play", "Play a song")
                        .addOption(OptionType.STRING, "name", "Artist - Name", true)
                        .setGuildOnly(true),
                CommandType.MUSIC, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildVoiceState voiceState = event.getMember().getVoiceState();

        if (voiceState == null || !voiceState.inAudioChannel()) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle("You are not connected in any voice channel.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        VoiceChannel voiceChannel = voiceState.getChannel().asVoiceChannel();
        AudioManager audioManager = event.getGuild().getAudioManager();

        String name = event.getOption("name").getAsString();

        if (!audioManager.isConnected()) {
            audioManager.setAutoReconnect(true);
            audioManager.setSelfDeafened(true);
            audioManager.setSendingHandler(this.getBot().getMusicManager().get(event.getGuild()).getSendHandler());
            audioManager.openAudioConnection(voiceChannel);
        } else if (audioManager.getConnectedChannel().getIdLong() != voiceChannel.getIdLong()) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle("You are not connected with the same channel as me.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        this.play(event, name);
    }

    private void play(SlashCommandInteractionEvent event, String query) {
        if (query.startsWith("https://open.spotify.com/track/")) {
            try {
                event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.SUCCESS)
                        .setTitle("Searching **" + query + "**...").build()).queue();
                ArrayList<String> tracks = this.getBot().getLinkConverter().convert(query);
                if (tracks.isEmpty()) {
                    event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                            .setTitle("Cannot find any song by this URL.").build()).queue();
                    return;
                }
                if (tracks.size() > 1) {
                    event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                            .setTitle("Playing playlists from Spotify is unsupported.").build()).queue();
                    return;
                }
                this.getBot().getYoutubeApiManager().search(tracks.getFirst())
                        .ifPresentOrElse(url -> this.getBot().getMusicManager().queue(url, event.getGuild(), event.getInteraction().getChannel().asTextChannel()),
                                () -> event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                                        .setTitle("Failed to find any song.").build()).queue());
            } catch (Exception e) {
                event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                        .setTitle("Failed to search track from Spotify!").build()).queue();
            }
            return;
        }

        if (query.contains("://")) {
            this.getBot().getMusicManager().queue(query.replace("&list=LM", ""), event.getGuild(), event.getChannel().asTextChannel());
            return;
        }

        if (this.getBot().getConfig().getYoutubeApiKey().equals("default")) {
            event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                    .setTitle("Searching is not available!").build()).queue();
            return;
        }

        event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.SUCCESS)
                .setTitle("Searching **" + query + "**...").build()).queue();

        this.getBot().getYoutubeApiManager().search(query)
                .ifPresentOrElse(url -> this.getBot().getMusicManager().queue(url, event.getGuild(), event.getInteraction().getChannel().asTextChannel()),
                        () -> event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                                .setTitle("Failed to find any song.").build()).queue());
    }
}
