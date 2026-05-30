package pl.cmclient.bot.audio;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class YtDlpAudioSourceManager implements AudioSourceManager {

    private static final Logger log = LoggerFactory.getLogger(YtDlpAudioSourceManager.class);

    private final String ytDlpPath;
    private final String cookiesPath;
    private final HttpAudioSourceManager httpSourceManager;

    public YtDlpAudioSourceManager(String ytDlpPath, String cookiesPath) {
        this.ytDlpPath = ytDlpPath;
        this.cookiesPath = cookiesPath;
        this.httpSourceManager = new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY);
    }

    @Override
    public String getSourceName() {
        return "yt-dlp";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        String url = reference.identifier;
        if (!isYoutubeUrl(url)) {
            return null;
        }
        try {
            String directUrl = resolveDirectUrl(url);
            if (directUrl == null || directUrl.isBlank()) {
                log.error("yt-dlp returned no URL for: {}", url);
                return AudioReference.NO_TRACK;
            }
            AudioItem item = httpSourceManager.loadItem(manager, new AudioReference(directUrl, reference.title));
            if (item instanceof AudioTrack track) {
                return new YtDlpAudioTrack(track.getInfo(), this);
            }
            return item;
        } catch (Exception ex) {
            log.error("Failed to resolve URL via yt-dlp for: {}", url, ex);
            return AudioReference.NO_TRACK;
        }
    }

    public String resolveDirectUrlPublic(String videoUrl) throws IOException, InterruptedException {
        return resolveDirectUrl(videoUrl);
    }

    private String resolveDirectUrl(String videoUrl) throws IOException, InterruptedException {
        ProcessBuilder pb;
        if (cookiesPath != null && !cookiesPath.isBlank()) {
            pb = new ProcessBuilder(ytDlpPath, "--cookies", cookiesPath, "--js-runtimes", "node", "-f", "bestaudio", "--get-url", videoUrl);
        } else {
            pb = new ProcessBuilder(ytDlpPath, "-f", "bestaudio", "--get-url", videoUrl);
        }
        pb.redirectErrorStream(false);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes()).trim();
        process.waitFor();
        // Return only the first line (in case multiple formats are printed)
        return output.lines().findFirst().orElse(null);
    }

    private boolean isYoutubeUrl(String url) {
        return url.contains("youtube.com/") || url.contains("youtu.be/");
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return track instanceof YtDlpAudioTrack;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        // No extra fields needed
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return new YtDlpAudioTrack(trackInfo, this);
    }

    @Override
    public void shutdown() {
        httpSourceManager.shutdown();
    }

    public HttpAudioSourceManager getHttpSourceManager() {
        return httpSourceManager;
    }
}
