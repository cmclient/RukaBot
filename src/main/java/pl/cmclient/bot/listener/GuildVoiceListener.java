package pl.cmclient.bot.listener;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.object.AudioPlayer;
import pl.cmclient.bot.object.ServerData;

@RequiredArgsConstructor
public class GuildVoiceListener extends ListenerAdapter {

    private final BotApplication bot;

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        // Only care about users leaving a channel
        AudioChannel leftChannel = event.getChannelLeft();
        if (leftChannel == null) {
            return;
        }

        // Ignore bot events
        if (event.getMember().getUser().isBot()) {
            return;
        }

        Guild guild = event.getGuild();
        AudioManager audioManager = guild.getAudioManager();

        // Bot must be connected
        if (!audioManager.isConnected()) {
            return;
        }

        // The user must have left the same channel the bot is in
        AudioChannel botChannel = audioManager.getConnectedChannel();
        if (botChannel == null || !botChannel.getId().equals(leftChannel.getId())) {
            return;
        }

        // Check if 24/7 mode is enabled for this server
        ServerData serverData = this.bot.getServerDataManager().get(guild.getIdLong());
        if (serverData.isTwoFourSeven()) {
            return;
        }

        // Check if any non-bot users remain in the channel
        boolean anyHumansLeft = botChannel.getMembers().stream()
                .anyMatch(member -> !member.getUser().isBot());

        if (!anyHumansLeft) {
            AudioPlayer audioPlayer = this.bot.getMusicManager().get(guild);
            if (audioPlayer.getScheduler().getPlayingTrack() != null) {
                audioPlayer.getScheduler().stopTrack();
            }
            audioManager.closeAudioConnection();
        }
    }
}

