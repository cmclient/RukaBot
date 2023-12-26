package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

public class EmbedCommand extends Command {

    public EmbedCommand() {
        super(Commands.slash("embed", "Send embed")
                .addOption(OptionType.STRING, "message", "Message (seperate title from description with |, for new line use \\n)", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
                .setGuildOnly(true), CommandType.ADMINISTRATION, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String message = event.getOption("message").getAsString();
        EmbedBuilder eb = new CustomEmbed().create(CustomEmbed.Type.SUCCESS);

        if (message.contains("|")) {
            String[] split = message.split("\\|");
            eb.setTitle(split[0]);
            eb.setDescription(split[1].replace("\\n", "\n"));
        } else {
            eb.setDescription(message.replace("\\n", "\n"));
        }

        event.replyEmbeds(eb.build()).queue();
    }
}
