package pl.cmclient.bot.helper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.function.Consumer;

public final class BotHelper {

    public static void safeDM(User user, EmbedBuilder embed) {
        user.openPrivateChannel().queue(
                channel -> channel.sendMessageEmbeds(embed.build()).queue(unused -> {}, ex -> {}),
                ex -> {}
        );
    }

    public static void safeDM(User user, EmbedBuilder embed, Consumer<? super Message> consumer) {
        user.openPrivateChannel().queue(
                channel -> channel.sendMessageEmbeds(embed.build()).queue(consumer, ex -> {}),
                ex -> {}
        );
    }

    public static void safeDM(User user, EmbedBuilder embed, Consumer<? super Message> consumer, Consumer<? super Throwable> consumer2) {
        user.openPrivateChannel().queue(
                channel -> channel.sendMessageEmbeds(embed.build()).queue(consumer, consumer2),
                consumer2
        );
    }
}
