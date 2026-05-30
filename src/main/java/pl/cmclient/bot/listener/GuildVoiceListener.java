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
        AudioChannel leftChannel = event.getChannelLeft();
        if (leftChannel == null || event.getMember().getUser().isBot()) {
            return;
        }

        Guild guild = event.getGuild();
        AudioManager audioManager = guild.getAudioManager();

        if (!audioManager.isConnected()) {
            return;
        }

        AudioChannel botChannel = audioManager.getConnectedChannel();
        if (botChannel == null || !botChannel.getId().equals(leftChannel.getId())) {
            return;
        }

        ServerData serverData = this.bot.getServerDataManager().get(guild.getIdLong());
        if (serverData.isTwoFourSeven()) {
            return;
        }

        boolean anyHumansLeft = botChannel.getMembers().stream()
                .anyMatch(member -> !member.getUser().isBot());

        if (!anyHumansLeft) {
//            bot.getLogger().info("No non-bot users left in voice channel {} in guild {}. Disconnecting.", botChannel.getName(), guild.getName());
            AudioPlayer audioPlayer = this.bot.getMusicManager().get(guild);
            if (audioPlayer.getScheduler().getPlayingTrack() != null) {
                audioPlayer.getScheduler().stopTrack();
            }
            audioManager.closeAudioConnection();
        }
    }
}

