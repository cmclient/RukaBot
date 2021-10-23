package pl.cmclient.bot.object;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.audio.LavaplayerAudioSource;
import pl.cmclient.bot.audio.TrackScheduler;

public class AudioPlayer {

    public final com.sedmelluq.discord.lavaplayer.player.AudioPlayer player;
    public final TrackScheduler scheduler;
    
    public AudioPlayer(AudioPlayerManager manager) {
        this.player = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.player);
        this.player.addListener(this.scheduler);
    }

    public LavaplayerAudioSource getSendHandler() {
        return new LavaplayerAudioSource(BotApplication.getInstance().getApi(), this.player);
    }
}
