package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.audio.TrackScheduler;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;

public class LoopCommand extends Command {

    public LoopCommand() {
        super("loop", "Enables/disables loop-mode", CommandType.MUSIC, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        event.getServer().ifPresent(server -> server.getAudioConnection().ifPresentOrElse(connection -> {
            TrackScheduler scheduler = this.bot.getMusicManager().get(server).scheduler;
            scheduler.setLoop(!scheduler.isLoop());
            channel.sendMessage(new RukaEmbed().create(true)
                    .setTitle("Loop mode has been: " + (scheduler.isLoop() ? "enabled" : "disabled")));
        }, () -> channel.sendMessage(new RukaEmbed().create(false)
                .setTitle("I'm not connected to any channel!"))));
    }
}
