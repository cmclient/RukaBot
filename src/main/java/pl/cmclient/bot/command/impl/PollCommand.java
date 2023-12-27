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

import java.util.Objects;

public class PollCommand extends Command {

    public PollCommand() {
        super(Commands.slash("poll", "Create a poll")
                .addOption(OptionType.STRING, "question", "The poll question (use '|' to separate title from description, '\\n' for a new line)", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
                .setGuildOnly(true), CommandType.MODERATION, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String question = event.getOption("question").getAsString();
        EmbedBuilder eb = new CustomEmbed().create(CustomEmbed.Type.SUCCESS);

        if (question.contains("|")) {
            String[] split = question.split("\\|");
            eb.setTitle(split[0]);
            eb.setDescription(split[1].replace("\\n", "\n"));
        } else {
            eb.setDescription(question.replace("\\n", "\n"));
        }

        event.deferReply().queue(interactionHook -> interactionHook.editOriginalEmbeds(eb.build()).queue(message -> {
            message.addReaction(Objects.requireNonNull(event.getGuild().getEmojisByName("yes", false).stream().findAny().orElse(null))).queue();
            message.addReaction(Objects.requireNonNull(event.getGuild().getEmojisByName("no", false).stream().findAny().orElse(null))).queue();
        }));
    }
}
