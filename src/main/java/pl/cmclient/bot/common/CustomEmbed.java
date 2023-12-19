package pl.cmclient.bot.common;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import pl.cmclient.bot.BotApplication;

import java.awt.*;
import java.time.Instant;

public class CustomEmbed {

    @Getter
    public enum Type {
        SUCCESS(new Color(29, 185, 84)),
        WARNING(new Color(255, 153, 102)),
        ERROR(new Color(213, 16, 7));

        Type(Color color) {
            this.color = color;
        }

        private final Color color;
    }

    public EmbedBuilder create() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setFooter(BotApplication.getInstance().getConfig().getBotName(), BotApplication.getInstance().getJda().getSelfUser().getAvatarUrl());
        builder.setTimestamp(Instant.now());
        return builder;
    }

    public EmbedBuilder create(Type type) {
        EmbedBuilder builder = this.create();
        builder.setColor(type.getColor());
        return builder;
    }
}
