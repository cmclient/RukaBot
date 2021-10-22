package pl.cmclient.bot.common;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import pl.cmclient.bot.BotApplication;

import java.awt.*;

public class RukaEmbed {

    public EmbedBuilder create() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setFooter(BotApplication.getInstance().getConfig().getBotName() + " | v1.0-SNAPSHOT", BotApplication.getInstance().getApi().getYourself().getAvatar());
        builder.setTimestampToNow();
        return builder;
    }

    public EmbedBuilder create(boolean success) {
        EmbedBuilder builder = this.create();
        builder.setColor(success ? Color.green : Color.red);
        return builder;
    }
}
