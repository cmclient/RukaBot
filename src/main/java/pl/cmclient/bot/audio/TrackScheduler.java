package pl.cmclient.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private AudioPlayer player;
    private BlockingQueue<AudioTrack> queue;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<AudioTrack>();
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return this.queue;
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    public int clearQueue() {
        int temp = this.queue.size();
        this.queue.clear();
        return temp;
    }

    public AudioTrack deleteFromQueue(int i) {
        AudioTrack track = new ArrayList<>(this.queue).get(i - 1);
        this.queue.remove(track);
        return track;
    }

    public long seek(long position) {
        long previousValue = this.player.getPlayingTrack().getPosition();
        long duration = this.getPlayingTrack().getDuration();
        position *= 1000L;
        if (this.player.getPlayingTrack().isSeekable() && duration > position) {
            this.player.getPlayingTrack().setPosition(position);
        }
        return previousValue / 1000L;
    }

    public AudioTrack getPlayingTrack() {
        return this.player.getPlayingTrack();
    }

    public void stopTrack() {
        this.player.stopTrack();
    }

    public void nextTrack() {
        this.player.startTrack(this.queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            this.nextTrack();
        }
    }
}
