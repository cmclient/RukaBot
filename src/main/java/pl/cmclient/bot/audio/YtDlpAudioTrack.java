package pl.cmclient.bot.audio;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YtDlpAudioTrack extends DelegatedAudioTrack {

    private static final Logger log = LoggerFactory.getLogger(YtDlpAudioTrack.class);

    private final YtDlpAudioSourceManager sourceManager;

    public YtDlpAudioTrack(AudioTrackInfo trackInfo, YtDlpAudioSourceManager sourceManager) {
        super(trackInfo);
        this.sourceManager = sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        AudioTrackInfo trackInfo = getInfo();
        String directUrl = sourceManager.resolveDirectUrlPublic(trackInfo.uri);
        if (directUrl == null || directUrl.isBlank()) {
            throw new RuntimeException("yt-dlp returned no URL for: " + trackInfo.uri);
        }
        log.debug("yt-dlp resolved URL for playback: {}", directUrl);
        HttpAudioSourceManager httpManager = new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY);
        try {
            InternalAudioTrack httpTrack = (InternalAudioTrack) httpManager.loadItem(null, new com.sedmelluq.discord.lavaplayer.track.AudioReference(directUrl, trackInfo.title));
            if (httpTrack == null) {
                throw new RuntimeException("HttpAudioSourceManager could not load: " + directUrl);
            }
            processDelegate(httpTrack, executor);
        } finally {
            httpManager.shutdown();
        }
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new YtDlpAudioTrack(getInfo(), sourceManager);
    }

    @Override
    public YtDlpAudioSourceManager getSourceManager() {
        return sourceManager;
    }
}
