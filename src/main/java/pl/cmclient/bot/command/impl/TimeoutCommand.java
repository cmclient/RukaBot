package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.helper.BotHelper;
import pl.cmclient.bot.helper.DateHelper;

import java.time.Duration;
import java.time.Instant;

public class TimeoutCommand extends Command {

    public TimeoutCommand() {
        super(Commands.slash("timeout", "Timeout or clear timeout for a user")
                        .addOption(OptionType.USER, "user", "User to timeout or clear", true)
                        .addOption(OptionType.STRING, "time", "Duration", false)
                        .addOption(OptionType.STRING, "reason", "Reason for the timeout", false)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                        .setContexts(InteractionContextType.GUILD),
                CommandType.MODERATION,
                false
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member target = event.getOption("user").getAsMember();
        String timeStr = event.getOption("time") != null ? event.getOption("time").getAsString().toLowerCase() : null;
        String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "No reason";
        User moderator = event.getUser();

        if (target == null) {
            event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                    .setDescription(":interrobang: Invalid member.")
                    .build()).queue();
            return;
        }

        if (!event.getGuild().getSelfMember().canInteract(target)) {
            event.replyEmbeds(new CustomEmbed()
                    .create(CustomEmbed.Type.ERROR)
                    .setDescription(":interrobang: I cannot interact with this member. They might have a higher role than me or I'm missing permissions.")
                    .build()).queue();
            return;
        }

        if (timeStr == null || timeStr.isEmpty()) {
            if (!target.isTimedOut()) {
                event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                        .setDescription(":interrobang: User is not timed out.")
                        .build()).queue();
                return;
            }
            target.removeTimeout().reason(reason).queue(unused -> {
                event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.SUCCESS)
                        .setDescription(":white_check_mark: Timeout cleared for " + target.getUser().getAsMention())
                        .build()).queue();

                BotHelper.safeDM(target.getUser(),
                        new CustomEmbed().create(CustomEmbed.Type.SUCCESS)
                                .setTitle(":hourglass: Your timeout has been cleared")
                                .addField("Moderator:", "@" + moderator.getName() + " (" + moderator.getAsMention() + ")", false)
                                .addField("Reason:", reason, false)
                                .setFooter(BotApplication.getInstance().getConfig().getBotName(),
                                        BotApplication.getInstance().getJda().getSelfUser().getAvatarUrl())
                                .setTimestamp(Instant.now()));
            }, err -> event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                    .setDescription(":interrobang: Failed to clear timeout.\nError: " + err.getMessage())
                    .build()).queue());

            return;
        }

        long untilMillis = DateHelper.stringToTime(timeStr);
        String durationStr = DateHelper.formatDateDiff(untilMillis);

        if (untilMillis <= 0) {
            event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                    .setDescription(":interrobang: Invalid time format. Use like `24h`, `2d12h`, `14d2h15m`.")
                    .build()).queue();
            return;
        }

        long durationMillis = untilMillis - System.currentTimeMillis();
        if (durationMillis <= 0) {
            event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                    .setDescription(":interrobang: The timeout must be in the future.")
                    .build()).queue();
            return;
        }

        if (durationMillis > 90L * 24 * 60 * 60 * 1000) {
            event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.WARNING)
                    .setDescription(":interrobang: Timeout cannot exceed 90 days.")
                    .build()).queue();
            return;
        }

        final User targetUser = target.getUser();

        BotHelper.safeDM(targetUser, new CustomEmbed().create(CustomEmbed.Type.WARNING)
                        .setTitle(":hourglass: You have been timed out.")
                        .addField("Duration:", durationStr, false)
                        .addField("Reason:", reason, false)
                        .addField("Moderator:", "@" + moderator.getName() + " (" + moderator.getAsMention() + ")", false)
                        .setFooter(BotApplication.getInstance().getConfig().getBotName(),
                                BotApplication.getInstance().getJda().getSelfUser().getAvatarUrl())
                        .setTimestamp(Instant.now()),
                unused -> applyTimeout(target, durationMillis, reason, event, moderator, durationStr),
                ex -> applyTimeout(target, durationMillis, reason, event, moderator, durationStr));
    }

    private void applyTimeout(Member target, long durationMillis, String reason, SlashCommandInteractionEvent event, User moderator, String durationStr) {
        target.timeoutFor(Duration.ofMillis(durationMillis))
                .reason(reason + " | Moderator: @" + moderator.getName())
                .queue(unused -> event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.SUCCESS)
                                .setDescription(":white_check_mark: User @" + target.getUser().getName() + " (" + target.getAsMention() + ") has been timed out.")
                                .addField("Duration:", durationStr, false)
                                .addField("Reason", reason, false)
                                .addField("Moderator", "@" + moderator.getName() + " (" + moderator.getAsMention() + ")", false)
                                .build()).queue(),
                        err -> event.replyEmbeds(new CustomEmbed().create(CustomEmbed.Type.ERROR)
                                .setDescription("Failed to timeout.\nError: " + err.getMessage())
                                .build()).queue());
    }
}
