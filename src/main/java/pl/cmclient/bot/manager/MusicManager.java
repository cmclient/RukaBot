package pl.cmclient.bot.manager;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;
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
        AudioSourceManagers.registerRemoteSources(this.playerManager = new DefaultAudioPlayerManager());
        this.playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        this.playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        this.audioPlayers = new ConcurrentHashMap<>();
    }

    public void queue(String url, Server server, ServerTextChannel channel) {
        AudioPlayer audioManager = this.get(server);
        this.playerManager.loadItemOrdered(audioManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioManager.scheduler.queue(track);
                channel.sendMessage(new CustomEmbed().create(true)
                        .setAuthor(track.getInfo().title, track.getInfo().uri, "https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg")
                        .setTitle("Added track to queue")
                        .setDescription(audioManager.scheduler.getQueue().size() == 0 ? "" : "Position in queue: " + audioManager.scheduler.getQueue().size())
                        .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg"));
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack track = playlist.getSelectedTrack();
                List<AudioTrack> tracks = new ArrayList<>(playlist.getTracks());

                if (track != null) {
                    audioManager.scheduler.queue(track);
                    tracks.remove(track);
                } else {
                    track = tracks.get(0);
                }

                tracks.forEach(audioManager.scheduler::queue);

                channel.sendMessage(new CustomEmbed().create(true)
                        .setAuthor(track.getInfo().title, track.getInfo().uri, "https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg")
                        .setTitle("Added playlist to queue (" + playlist.getTracks().size() + " songs)")
                        .setDescription(audioManager.scheduler.getQueue().size() == 0 ? "" : "Position in queue: " + audioManager.scheduler.getQueue().size())
                        .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg"));
            }

            @Override
            public void noMatches() {
                channel.sendMessage(new CustomEmbed().create(false)
                        .setTitle("Cannot find any song by this URL."));
            }

            @Override
            public void loadFailed(FriendlyException ex) {
                channel.sendMessage(new CustomEmbed().create(false)
                        .setTitle("Error while trying to play that song."));
                BotApplication.getInstance().getLogger().error("Error while trying to play song", ex);
            }
        });
    }

    public void stop(Server server, ServerTextChannel channel) {
        AudioPlayer audioManager = this.get(server);
        AudioTrack track = audioManager.scheduler.getPlayingTrack();
        if (track == null) {
            channel.sendMessage(new CustomEmbed().create(false).setTitle("Currently nothing playing."));
            return;
        }
        audioManager.scheduler.stopTrack();
        channel.sendMessage(new CustomEmbed().create(true).setTitle("Stopped playing **" + track.getInfo().title + "**"));
    }

    public void skip(Server server, ServerTextChannel channel) {
        AudioPlayer audioManager = this.get(server);
        AudioTrack track = audioManager.scheduler.getPlayingTrack();
        if (track == null) {
            channel.sendMessage(new CustomEmbed().create(false).setTitle("Currently nothing playing."));
            return;
        }
        audioManager.scheduler.nextTrack();
        AudioTrack nextTrack = audioManager.scheduler.getPlayingTrack();
        channel.sendMessage(new CustomEmbed().create(true)
                .setAuthor(nextTrack.getInfo().title, nextTrack.getInfo().uri, "https://img.youtube.com/vi/" + nextTrack.getInfo().identifier + "/maxresdefault.jpg")
                .setTitle("Skipped track **" + track.getInfo().title + "**")
                .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg"));
    }

    public void setVolume(int volume, Server server, ServerTextChannel channel) {
        AudioPlayer audioManager = this.get(server);
        audioManager.player.setVolume(volume);
        channel.sendMessage(new CustomEmbed().create(true).setTitle("Volume has been set to: **" + volume + "**%"));
    }

    public AudioTrack getPlayingTrack(Server server) {
        AudioPlayer audioManager = this.get(server);
        return audioManager.player.getPlayingTrack();
    }

    public long setPosition(Server server, long position, TimeUnit unit) {
        long frameNumber = (unit.toMillis(position) / 20);
        long positionInMillis = frameNumber * 20;
        this.getPlayingTrack(server).setPosition(positionInMillis);
        return positionInMillis;
    }

    public AudioPlayer get(Server server) {
        AudioPlayer audioManager = this.audioPlayers.get(server.getId());
        if (audioManager == null) {
            audioManager = new AudioPlayer(this.playerManager);
            this.audioPlayers.put(server.getId(), audioManager);
        }
        return audioManager;
    }

    public Duration getPosition(Server server) {
        return Duration.ofMillis(this.getPlayingTrack(server).getPosition());
    }
}
