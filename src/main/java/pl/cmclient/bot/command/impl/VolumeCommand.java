package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;

public class VolumeCommand extends Command {

    public VolumeCommand() {
        super("volume", "Change volume", CommandType.MUSIC, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        if (args.length == 0 || !this.isNumber(args[0])) {
            channel.sendMessage(new RukaEmbed().create(false)
                    .setTitle(this.getUsage("<volume>")));
            return;
        }

        event.getServer().ifPresent(server -> server.getAudioConnection().ifPresentOrElse(connection -> {
            int volume = Integer.parseInt(args[0]);
            if (volume <= 0 || volume > 100) {
                channel.sendMessage(new RukaEmbed().create(false).setTitle("Minimum volume is 1, maximum is 100"));
                return;
            }
            this.bot.getMusicManager().setVolume(volume, server, channel);
        }, () -> event.getChannel().sendMessage(new RukaEmbed().create(false)
                .setTitle("I'm not connected to any channel!"))));
    }

    private boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException ex) {
            return false;
        }
    }
}
