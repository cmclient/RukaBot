package pl.cmclient.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class YtDlpAudioSourceManager implements AudioSourceManager {

    private static final Logger log = LoggerFactory.getLogger(YtDlpAudioSourceManager.class);

    private final String ytDlpPath;
    private final String cookiesPath;
    private final String jsRuntimes;

    public YtDlpAudioSourceManager(String ytDlpPath, String cookiesPath, String jsRuntimes) {
        this.ytDlpPath = ytDlpPath;
        this.cookiesPath = cookiesPath;
        this.jsRuntimes = jsRuntimes;
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
            String[] meta = resolveMetadata(url);
            String directUrl = meta[0];
            String title     = meta[1];
            String author    = meta[2];
            long   duration  = parseDurationSeconds(meta[3]);
            String webUrl    = meta[4];
            String videoId   = meta[5];

            if (directUrl == null || directUrl.isBlank()) {
                log.error("yt-dlp returned no URL for: {}", url);
                return AudioReference.NO_TRACK;
            }

            AudioTrackInfo info = new AudioTrackInfo(
                    title  != null && !title.isBlank()  ? title  : "Unknown title",
                    author != null && !author.isBlank() ? author : "Unknown artist",
                    duration,
                    videoId != null && !videoId.isBlank() ? videoId : url,
                    false,
                    webUrl  != null && !webUrl.isBlank()  ? webUrl  : url
            );
            return new YtDlpAudioTrack(info, this);
        } catch (Exception ex) {
            log.error("Failed to resolve URL via yt-dlp for: {}", url, ex);
            return AudioReference.NO_TRACK;
        }
    }

    private long parseDurationSeconds(String raw) {
        if (raw == null || raw.isBlank()) return 0L;
        try {
            return (long) (Double.parseDouble(raw.trim()) * 1000L);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public String resolveDirectUrlPublic(String videoUrl) throws IOException, InterruptedException {
        return resolveMetadata(videoUrl)[0];
    }

    /**
     * Returns [directUrl, title, uploader, durationSeconds, webpageUrl, videoId]
     */
    private String[] resolveMetadata(String videoUrl) throws IOException, InterruptedException {
        java.util.List<String> cmd = new java.util.ArrayList<>();
        cmd.add(ytDlpPath);
        if (cookiesPath != null && !cookiesPath.isBlank()) {
            cmd.add("--cookies");
            cmd.add(cookiesPath);
        }
        if (jsRuntimes != null && !jsRuntimes.isBlank()) {
            cmd.add("--js-runtimes");
            cmd.add(jsRuntimes);
        }
        cmd.add("-f");
        cmd.add("bestaudio");
        cmd.add("--print");
        cmd.add("url");
        cmd.add("--print");
        cmd.add("title");
        cmd.add("--print");
        cmd.add("uploader");
        cmd.add("--print");
        cmd.add("duration");
        cmd.add("--print");
        cmd.add("webpage_url");
        cmd.add("--print");
        cmd.add("id");
        cmd.add("--no-playlist");
        cmd.add(videoUrl);
        log.info("Executing command: {}", cmd);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(false);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes()).trim();
        process.waitFor();
        String[] lines = output.split("\n", -1);
        String[] result = new String[6];
        Arrays.fill(result, "");
        for (int i = 0; i < Math.min(lines.length, result.length); i++) {
            result[i] = lines[i].trim();
        }
        return result;
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
    }
}
