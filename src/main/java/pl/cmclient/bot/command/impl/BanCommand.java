package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
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

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class BanCommand extends Command {

    public BanCommand() {
        super(Commands.slash("ban", "Ban a user from the server")
                .addOption(OptionType.USER, "user", "Select user", true)
                .addOption(OptionType.STRING, "reason", "Reason")
                .addOption(OptionType.INTEGER, "delete", "Delete messages from the last x days")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
                .setContexts(InteractionContextType.GUILD),
                CommandType.MODERATION, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        User user = event.getUser();

        if (!event.getGuild().getSelfMember().canInteract(member)) {
            event.replyEmbeds(new CustomEmbed()
                    .create(CustomEmbed.Type.ERROR)
                    .setDescription(":interrobang: I cannot interact with this member. They might have a higher role than me or I'm missing permissions.")
                    .build()).queue();
            return;
        }

        User other = event.getOption("user").getAsUser();
        String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "No reason";
        int delete = event.getOption("delete") != null ? Math.min(event.getOption("delete").getAsInt(), 7) : 0;

        BotHelper.safeDM(other, new CustomEmbed().create(CustomEmbed.Type.ERROR)
                        .setTitle(":interrobang: You have been banned.")
                        .addField("Reason:", reason, false)
                        .addField("Moderator:", "@" + user.getName() + " (" + user.getAsMention() + ")", false)
                        .setFooter(BotApplication.getInstance().getConfig().getBotName(),
                                BotApplication.getInstance().getJda().getSelfUser().getAvatarUrl())
                        .setTimestamp(Instant.now()),
                unused -> banUser(guild, other, delete, reason, user, event),
                ex -> banUser(guild, other, delete, reason, user, event));
    }

    private void banUser(Guild guild, User target, int deleteDays, String reason, User banner,
                         SlashCommandInteractionEvent event) {
        guild.ban(target, deleteDays, TimeUnit.DAYS)
                .reason(reason + " | Moderator: @" + banner.getName())
                .queue(unused -> {
                    event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.SUCCESS)
                            .setDescription(":white_check_mark: User @" + target.getName() + " (" + target.getAsMention() + ") has been banned.")
                            .addField("Reason", reason, false)
                            .addField("Moderator:", "@" + banner.getName() + " (" + banner.getAsMention() + ")", false)
                            .build()).queue();
                }, throwable -> event.replyEmbeds(new CustomEmbed()
                        .create(CustomEmbed.Type.ERROR)
                        .setDescription(":interrobang: Failed to ban this user.\nError: " + throwable.getMessage())
                        .build()).queue());
    }
}
