package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.helper.StringHelper;

public class PlayCommand extends Command {

    public PlayCommand() {
        super("play", "Play music from YouTube", CommandType.MUSIC, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        if (args.length == 0) {
            channel.sendMessage(new CustomEmbed()
                    .create(false)
                    .setDescription(this.getUsage("<url>")));
            return;
        }

        event.getServer().ifPresent(server -> event.getMessageAuthor().getConnectedVoiceChannel().ifPresentOrElse(voiceChannel -> {
            if (voiceChannel.canYouConnect() && voiceChannel.canYouSee() && voiceChannel.hasPermission(event.getApi().getYourself(), PermissionType.SPEAK)) {

                if (!voiceChannel.isConnected(event.getApi().getYourself()) && server.getAudioConnection().isEmpty()) {
                    voiceChannel.connect().thenAccept(audioConnection -> {
                        audioConnection.setAudioSource(this.bot.getMusicManager().get(server).getSendHandler());
                        audioConnection.setSelfDeafened(true);
                        this.play(server, channel, args);
                    });

                } else if (server.getAudioConnection().isPresent()) {
                    server.getAudioConnection().ifPresent(audioConnection -> {
                        if (audioConnection.getChannel().getId() == voiceChannel.getId()) {
                            this.play(server, channel, args);
                        } else {
                            channel.sendMessage(new CustomEmbed().create(false)
                                    .setTitle("You are not connected with the same channel as the bot."));
                        }
                    });
                }
            } else {
                channel.sendMessage(new CustomEmbed().create(false)
                        .setTitle("I cannot connect, cannot see, or do not have the permission to speak on the channel."));
            }
        }, () -> channel.sendMessage(new CustomEmbed().create(false)
                .setTitle("You are not connected in any voice channel."))));
    }

    private void play(Server server, ServerTextChannel channel, String... args) {
        String query = StringHelper.join(args, " ", 0, args.length);

        if (query.contains("://")) {
            this.bot.getMusicManager().queue(query, server, channel);
        } else {
            channel.sendMessage(new CustomEmbed().create(true)
                    .setTitle("Searching **" + query + "** on YouTube..."));

            if (this.bot.getConfig().getYoutubeApiKey().equals("default")) {
                channel.sendMessage(new CustomEmbed().create(false)
                        .setTitle("Searching on YouTube is not available!"));
            } else {
                this.bot.getYoutubeApiManager().search(query)
                        .ifPresentOrElse(url -> this.bot.getMusicManager().queue(url, server, channel),
                                () -> channel.sendMessage(new CustomEmbed().create(false)
                                        .setTitle("Cannot find any song by this URL.")));
            }
        }
    }
}
