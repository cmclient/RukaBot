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
import org.javacord.api.entity.user.User;
import pl.cmclient.bot.common.RukaEmbed;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMusicManager {

    private final AudioPlayerManager playerManager;
    private final Map<Long, ServerAudioManager> serverAudioManagers;

    public ServerMusicManager() {
        AudioSourceManagers.registerRemoteSources(this.playerManager = new DefaultAudioPlayerManager());
        this.serverAudioManagers = new ConcurrentHashMap<>();
    }

    public void queue(String url, User user, Server server, ServerTextChannel channel) {
        ServerAudioManager audioManager = this.getAudioManager(server);
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

    public void stop(User user, Server server, ServerTextChannel channel) {
        ServerAudioManager audioManager = this.getAudioManager(server);
        AudioTrack track = audioManager.scheduler.getPlayingTrack();
        if (track == null) {
            channel.sendMessage(new RukaEmbed().create(false).setTitle("Currently nothing playing."));
            return;
        }
        audioManager.scheduler.stopTrack();
        channel.sendMessage(new RukaEmbed().create(true).setTitle("Stopped playing **" + track.getInfo().title + "**"));
    }

    public void skip(User user, Server server, ServerTextChannel channel) {
        ServerAudioManager audioManager = this.getAudioManager(server);
        AudioTrack track = audioManager.scheduler.getPlayingTrack();
        if (track == null) {
            channel.sendMessage(new RukaEmbed().create(false).setTitle("Currently nothing playing."));
            return;
        }
        audioManager.scheduler.nextTrack();
        channel.sendMessage(new RukaEmbed().create(true).setTitle("Skipped **" + track.getInfo().title + "**"));
    }

    public void setVolume(int volume, User user, Server server, ServerTextChannel channel) {
        ServerAudioManager audioManager = this.getAudioManager(server);
        audioManager.player.setVolume(volume);
        channel.sendMessage(new RukaEmbed().create(true).setTitle("Volume has been set to: **" + volume + "**%"));
    }

    private synchronized ServerAudioManager getAudioManager(Server server) {
        ServerAudioManager audioManager = this.serverAudioManagers.get(server.getId());
        if (audioManager == null) {
            audioManager = new ServerAudioManager(this.playerManager);
            this.serverAudioManagers.put(server.getId(), audioManager);
        }
        server.getAudioConnection().ifPresent(audioConnection -> {
            audioConnection.setAudioSource(this.serverAudioManagers.get(server.getId()).getSendHandler());
            audioConnection.setSelfDeafened(true);
        });
        return audioManager;
    }


    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }
}
