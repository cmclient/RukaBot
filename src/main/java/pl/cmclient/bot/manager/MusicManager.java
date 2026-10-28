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
import dev.lavalink.youtube.YoutubeSource;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.audio.YtDlpAudioSourceManager;
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
    private @Getter final String youtubeSourceName;
    private @Getter final String ytDlpVersion;

    public MusicManager() {
        this(null);
    }

    public MusicManager(pl.cmclient.bot.config.Config config) {
        this.playerManager = new DefaultAudioPlayerManager();
        String ytDlpPath = config == null ? null : resolveYtDlpPath(config.getYtDlpPath());
        if (ytDlpPath != null) {
            BotApplication.getInstance().getLogger().info("Using yt-dlp source manager ({})", ytDlpPath);
            this.playerManager.registerSourceManager(new YtDlpAudioSourceManager(ytDlpPath, config.getYtDlpCookiesPath(), config.getYtDlpJsRuntimes()));
            this.youtubeSourceName = "yt-dlp";
            this.ytDlpVersion = readYtDlpVersion(ytDlpPath);
        } else {
            this.playerManager.registerSourceManager(createYoutubeSource(config));
            this.youtubeSourceName = "youtube-source";
            this.ytDlpVersion = null;
        }
        this.playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        this.playerManager.registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
        this.playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        this.playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        this.audioPlayers = new ConcurrentHashMap<>();
    }

    public void queue(InteractionHook hook, String url, Guild server) {
        AudioPlayer audioManager = this.get(server);
        this.playerManager.loadItemOrdered(audioManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioManager.getScheduler().queue(track);
                hook.sendMessageEmbeds(new CustomEmbed().create(CustomEmbed.Type.SUCCESS)
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

                hook.sendMessageEmbeds(new CustomEmbed().create(CustomEmbed.Type.SUCCESS)
                        .setAuthor(track.getInfo().title, track.getInfo().uri, "https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg")
                        .setTitle("Added playlist to queue (" + playlist.getTracks().size() + " songs)")
                        .setDescription(audioManager.getScheduler().getQueue().isEmpty() ? null : "Position in queue: " + audioManager.getScheduler().getQueue().size())
                        .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg").build()).queue();
            }

            @Override
            public void noMatches() {
                hook.sendMessageEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                        .setTitle("Cannot find any song by this URL.").build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException ex) {
                hook.sendMessageEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
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

    /**
     * Fallback for when yt-dlp is unavailable. As of youtube-source 1.18.2 none of the innertube
     * clients can actually stream: TVHTML5_SIMPLY hits "Sign in to confirm you're not a bot",
     * MWEB/ANDROID_* fail signature extraction or demand login, and WEB only gets SABR-only
     * responses. A poToken may revive WEB/WEBEMBEDDED, hence the config hooks below.
     */
    private static YoutubeAudioSourceManager createYoutubeSource(pl.cmclient.bot.config.Config config) {
        BotApplication.getInstance().getLogger().warn("yt-dlp unavailable, falling back to youtube-source; YouTube playback will likely fail.");
        YoutubeAudioSourceManager youtubeSource = new YoutubeAudioSourceManager();
        if (config == null) {
            return youtubeSource;
        }
        String poToken = config.getYoutubePoToken();
        String visitorData = config.getYoutubeVisitorData();
        if (poToken != null && !poToken.isBlank() && visitorData != null && !visitorData.isBlank()) {
            BotApplication.getInstance().getLogger().info("Applying YouTube poToken to WEB/WEBEMBEDDED clients");
            YoutubeSource.setPoTokenAndVisitorData(poToken, visitorData);
        }
        String oauthToken = config.getYoutubeOAuthToken();
        if (oauthToken != null && !oauthToken.isBlank()) {
            youtubeSource.useOauth2(oauthToken, false);
        }
        return youtubeSource;
    }

    /**
     * Resolves the yt-dlp executable, preferring the configured path and falling back to a PATH
     * lookup so a normal yt-dlp install is picked up without any configuration.
     *
     * @return a usable executable path, or null if yt-dlp could not be found.
     */
    private static String resolveYtDlpPath(String configuredPath) {
        if (configuredPath != null && !configuredPath.isBlank()) {
            if (isExecutable(new java.io.File(configuredPath))) {
                return configuredPath;
            }
            BotApplication.getInstance().getLogger().warn("Configured yt-dlp path is not an executable file: {}", configuredPath);
        }
        String path = System.getenv("PATH");
        if (path == null) {
            return null;
        }
        for (String directory : path.split(java.io.File.pathSeparator)) {
            if (directory.isBlank()) {
                continue;
            }
            for (String name : new String[] {"yt-dlp.exe", "yt-dlp.cmd", "yt-dlp"}) {
                java.io.File candidate = new java.io.File(directory.trim(), name);
                if (isExecutable(candidate)) {
                    return candidate.getAbsolutePath();
                }
            }
        }
        return null;
    }

    private static boolean isExecutable(java.io.File file) {
        return file.isFile() && file.canExecute();
    }

    /**
     * Asks yt-dlp for its own version once at startup, so /botinfo can show how fresh the
     * extractor is without spawning a process every time the command is used.
     *
     * @return the reported version, or null if yt-dlp did not answer.
     */
    private static String readYtDlpVersion(String ytDlpPath) {
        try {
            Process process = new ProcessBuilder(ytDlpPath, "--version").redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes()).trim();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            return process.exitValue() == 0 && !output.isEmpty() ? output : null;
        } catch (java.io.IOException ex) {
            BotApplication.getInstance().getLogger().warn("Failed to read yt-dlp version", ex);
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * @return how many servers are listening to something right now.
     */
    public long getActivePlayerCount() {
        return this.audioPlayers.values().stream().filter(player -> player.getPlayer().getPlayingTrack() != null).count();
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
