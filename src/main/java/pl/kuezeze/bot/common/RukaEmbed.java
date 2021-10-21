package pl.kuezeze.bot.common;

import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;

public class RukaEmbed {

    public EmbedBuilder create() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setFooter("RukaBot | v1.0-SNAPSHOT");
        builder.setTimestampToNow();
        return builder;
    }

    public EmbedBuilder create(boolean success) {
        EmbedBuilder builder = this.create();
        builder.setColor(success ? Color.green : Color.red);
        return builder;
    }
}
