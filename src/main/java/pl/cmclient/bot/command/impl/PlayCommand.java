package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;
import pl.cmclient.bot.helper.StringHelper;

public class PlayCommand extends Command {

    public PlayCommand() {
        super("play", "Play music from YouTube", CommandType.MUSIC, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        if (args.length == 0) {
            channel.sendMessage(new RukaEmbed()
                    .create(false)
                    .setDescription(this.getUsage("<url>")));
            return;
        }


        event.getServer().ifPresent(server -> {
            if (!user.getConnectedVoiceChannel(server).isPresent()) {
                channel.sendMessage(new RukaEmbed().create(false)
                        .setTitle("You are not connected to any channel!"));
                return;
            }

            ServerVoiceChannel voiceChannel = user.getConnectedVoiceChannel(server).get();

            if (!voiceChannel.isConnected(this.bot.getApi().getYourself())) {
                voiceChannel.connect().thenAccept(audioConnection -> this.play(user, server, channel, args));
            } else {
                play(user, server, channel, args);
            }
        });
    }

    private void play(User user, Server server, ServerTextChannel channel, String[] args) {
        String url = StringHelper.join(args, " ", 0, args.length);

        if (!url.contains("://")) {
            channel.sendMessage(new RukaEmbed().create(true)
                    .setTitle("Searching **" + url + "** on YouTube..."));

            if (this.bot.getConfig().getYoutubeApiKey().equals("default")) {
                channel.sendMessage(new RukaEmbed().create(false)
                        .setTitle("Searching on YouTube is not available!"));
            } else {
                this.bot.getYoutubeApiManager().search(url).ifPresent(videoUrl -> this.bot.getServerMusicManager().queue(videoUrl, user, server, channel));
            }
        } else {
            this.bot.getServerMusicManager().queue(url, user, server, channel);
        }
    }
}
