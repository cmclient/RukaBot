package pl.cmclient.bot.common;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import pl.cmclient.bot.BotApplication;

import java.awt.*;

public class CustomEmbed {

    private final static Color MAIN_COLOR = new Color(29, 185, 84);
    private final static Color ERROR_COLOR = new Color(213, 16, 7);

    public EmbedBuilder create() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setFooter(BotApplication.getInstance().getConfig().getBotName() + " | v1.0-SNAPSHOT", BotApplication.getInstance().getApi().getYourself().getAvatar());
        builder.setTimestampToNow();
        return builder;
    }

    public EmbedBuilder create(boolean success) {
        EmbedBuilder builder = this.create();
        builder.setColor(success ? MAIN_COLOR : ERROR_COLOR);
        return builder;
    }
}
