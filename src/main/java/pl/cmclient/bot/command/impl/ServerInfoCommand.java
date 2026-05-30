package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

import java.time.format.DateTimeFormatter;

public class ServerInfoCommand extends Command {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public ServerInfoCommand() {
        super(Commands.slash("serverinfo", "Informations about this server"),
                CommandType.GENERAL, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        String icon = guild.getIconUrl();
        String owner = guild.retrieveOwner().complete().getAsMention();
        String createdAt = guild.getTimeCreated().format(FORMATTER);
        String boostTier = guild.getBoostTier().name().replace("_", " ");
        int boostCount = guild.getBoostCount();
        int memberCount = guild.getMemberCount();
        int textChannels = guild.getTextChannels().size();
        int voiceChannels = guild.getVoiceChannels().size();
        int roles = guild.getRoles().size();
        int emotes = guild.getEmojis().size();

        EmbedBuilder embed = new CustomEmbed()
                .create(CustomEmbed.Type.SUCCESS)
                .setDescription("**" + guild.getName() + "** informations")
                .addField("Owner", owner, true)
                .addField("Server ID", guild.getId(), true)
                .addField("Created At", createdAt, true)
                .addField("Members", String.valueOf(memberCount), true)
                .addField("Text Channels", String.valueOf(textChannels), true)
                .addField("Voice Channels", String.valueOf(voiceChannels), true)
                .addField("Roles", String.valueOf(roles), true)
                .addField("Emojis", String.valueOf(emotes), true)
                .addField("Boost Tier", boostTier, true)
                .addField("Boosts", String.valueOf(boostCount), true);

        if (icon != null) {
            embed.setImage(icon);
        }

        event.replyEmbeds(embed.build()).queue();
    }
}


