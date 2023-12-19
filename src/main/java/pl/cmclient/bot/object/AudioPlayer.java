package pl.cmclient.bot.object;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lombok.Getter;
import pl.cmclient.bot.audio.AudioPlayerSendHandler;
import pl.cmclient.bot.audio.TrackScheduler;

@Getter
public class AudioPlayer {

    private final com.sedmelluq.discord.lavaplayer.player.AudioPlayer player;
    private final TrackScheduler scheduler;
    
    public AudioPlayer(AudioPlayerManager manager) {
        this.player = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.player);
        this.player.addListener(this.scheduler);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(this.player);
    }
}
