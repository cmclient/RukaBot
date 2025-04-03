package pl.cmclient.bot.manager;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.object.AudioPlayer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MusicManager {

    private final AudioPlayerManager playerManager;
    private final Map<Long, AudioPlayer> audioPlayers;

    public MusicManager() {
        this.playerManager = new DefaultAudioPlayerManager();
        this.playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        this.playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        this.playerManager.registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
        this.playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        this.playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        this.audioPlayers = new ConcurrentHashMap<>();
    }

    public void queue(String url, Guild server, TextChannel channel) {
        AudioPlayer audioManager = this.get(server);
        this.playerManager.loadItemOrdered(audioManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioManager.getScheduler().queue(track);
                channel.sendMessageEmbeds(new CustomEmbed().create(CustomEmbed.Type.SUCCESS)
                        .setAuthor(track.getInfo().title, track.getInfo().uri, "https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg")
                        .setTitle("Added track to queue")
                        .setDescription(audioManager.getScheduler().getQueue().isEmpty() ? null : "Position in queue: " + audioManager.getScheduler().getQueue().size())
                        .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg").build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack track = playlist.getSelectedTrack();
                List<AudioTrack> tracks = new ArrayList<>(playlist.getTracks());

                if (track != null) {
                    audioManager.getScheduler().queue(track);
                    tracks.remove(track);
                } else {
                    track = tracks.get(0);
                }

                tracks.forEach(audioManager.getScheduler()::queue);

                channel.sendMessageEmbeds(new CustomEmbed().create(CustomEmbed.Type.SUCCESS)
                        .setAuthor(track.getInfo().title, track.getInfo().uri, "https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg")
                        .setTitle("Added playlist to queue (" + playlist.getTracks().size() + " songs)")
                        .setDescription(audioManager.getScheduler().getQueue().isEmpty() ? null : "Position in queue: " + audioManager.getScheduler().getQueue().size())
                        .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg").build()).queue();
            }

            @Override
            public void noMatches() {
                channel.sendMessageEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                        .setTitle("Cannot find any song by this URL.").build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException ex) {
                channel.sendMessageEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                        .setTitle("Error while trying to play that song.").build()).queue();
                BotApplication.getInstance().getLogger().error("Error while trying to play song", ex);
            }
        });
    }

    public void stop(SlashCommandInteractionEvent event) {
        AudioPlayer audioManager = this.get(event.getGuild());
        AudioTrack track = audioManager.getScheduler().getPlayingTrack();
        if (track == null) {
            event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR).setTitle("Currently nothing playing.").build()).setEphemeral(true).queue();
            return;
        }
        audioManager.getScheduler().stopTrack();
        event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.SUCCESS).setTitle("Stopped playing **" + track.getInfo().title + "**").build()).queue();
    }

    public void skip(SlashCommandInteractionEvent event) {
        AudioPlayer audioManager = this.get(event.getGuild());
        AudioTrack track = audioManager.getScheduler().getPlayingTrack();
        if (track == null) {
            event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR).setTitle("Currently nothing playing.").build()).setEphemeral(true).queue();
            return;
        }
        audioManager.getScheduler().nextTrack();
        AudioTrack nextTrack = audioManager.getScheduler().getPlayingTrack();
        event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.SUCCESS)
                .setAuthor(nextTrack.getInfo().title, nextTrack.getInfo().uri, "https://img.youtube.com/vi/" + nextTrack.getInfo().identifier + "/maxresdefault.jpg")
                .setTitle("Skipped track **" + track.getInfo().title + "**")
                .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg").build()).queue();
    }

    public void setVolume(int volume, SlashCommandInteractionEvent event) {
        AudioPlayer audioManager = this.get(event.getGuild());
        audioManager.getPlayer().setVolume(volume);
        event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.SUCCESS).setTitle("Volume has been set to: **" + volume + "**%").build()).queue();
    }

    public AudioTrack getPlayingTrack(Guild server) {
        AudioPlayer audioManager = this.get(server);
        return audioManager.getPlayer().getPlayingTrack();
    }

    public long setPosition(Guild server, long position, TimeUnit unit) {
        long frameNumber = (unit.toMillis(position) / 20);
        long positionInMillis = frameNumber * 20;
        this.getPlayingTrack(server).setPosition(positionInMillis);
        return positionInMillis;
    }

    public AudioPlayer get(Guild server) {
        AudioPlayer audioManager = this.audioPlayers.get(server.getIdLong());
        if (audioManager == null) {
            audioManager = new AudioPlayer(this.playerManager);
            this.audioPlayers.put(server.getIdLong(), audioManager);
        }
        return audioManager;
    }

    public Duration getPosition(Guild server) {
        return Duration.ofMillis(this.getPlayingTrack(server).getPosition());
    }
}
