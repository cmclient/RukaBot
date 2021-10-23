package pl.cmclient.bot.manager;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;
import pl.cmclient.bot.common.RukaEmbed;
import pl.cmclient.bot.object.AudioPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ServerMusicManager {

    private final AudioPlayerManager playerManager;
    private final Map<Long, AudioPlayer> audioPlayers;

    public ServerMusicManager() {
        AudioSourceManagers.registerRemoteSources(this.playerManager = new DefaultAudioPlayerManager());
        this.audioPlayers = new ConcurrentHashMap<>();
    }

    public void queue(String url, Server server, ServerTextChannel channel) {
        AudioPlayer audioManager = this.get(server);
        this.playerManager.loadItemOrdered(audioManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioManager.scheduler.queue(track);
                channel.sendMessage(new RukaEmbed().create(true)
                        .setTitle("Added track to queue")
                        .setDescription(track.getInfo().title)
                        .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg"));
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack track = playlist.getSelectedTrack();
                if (track == null) {
                    track = playlist.getTracks().get(0);
                }
                audioManager.scheduler.queue(track);
                channel.sendMessage(new RukaEmbed().create(true)
                        .setTitle("Added track to queue")
                        .setDescription(track.getInfo().title)
                        .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg"));
            }

            @Override
            public void noMatches() {
                channel.sendMessage(new RukaEmbed().create(false)
                        .setTitle("Cannot find any song by this URL."));
            }

            @Override
            public void loadFailed(FriendlyException ex) {
                channel.sendMessage(new RukaEmbed().create(false)
                        .setTitle("Error while trying to play that song."));
            }
        });
    }

    public void stop(Server server, ServerTextChannel channel) {
        AudioPlayer audioManager = this.get(server);
        AudioTrack track = audioManager.scheduler.getPlayingTrack();
        if (track == null) {
            channel.sendMessage(new RukaEmbed().create(false).setTitle("Currently nothing playing."));
            return;
        }
        audioManager.scheduler.stopTrack();
        channel.sendMessage(new RukaEmbed().create(true).setTitle("Stopped playing **" + track.getInfo().title + "**"));
    }

    public void skip(Server server, ServerTextChannel channel) {
        AudioPlayer audioManager = this.get(server);
        AudioTrack track = audioManager.scheduler.getPlayingTrack();
        if (track == null) {
            channel.sendMessage(new RukaEmbed().create(false).setTitle("Currently nothing playing."));
            return;
        }
        audioManager.scheduler.nextTrack();
        channel.sendMessage(new RukaEmbed().create(true).setTitle("Skipped **" + track.getInfo().title + "**"));
    }

    public void setVolume(int volume, Server server, ServerTextChannel channel) {
        AudioPlayer audioManager = this.get(server);
        audioManager.player.setVolume(volume);
        channel.sendMessage(new RukaEmbed().create(true).setTitle("Volume has been set to: **" + volume + "**%"));
    }

    public AudioTrack getPlayingTrack(Server server) {
        AudioPlayer audioManager = this.get(server);
        return audioManager.player.getPlayingTrack();
    }

    public AudioPlayer get(Server server) {
        AudioPlayer audioManager = this.audioPlayers.get(server.getId());
        if (audioManager == null) {
            audioManager = new AudioPlayer(this.playerManager);
            this.audioPlayers.put(server.getId(), audioManager);
        }
        return audioManager;
    }

    public long setPosition(Server server, long position, TimeUnit unit) {
        long frameNumber = (unit.toMillis(position) / 20);
        long positionInMillis = frameNumber * 20;
        this.getPlayingTrack(server).setPosition(positionInMillis);
        return positionInMillis;
    }
}
