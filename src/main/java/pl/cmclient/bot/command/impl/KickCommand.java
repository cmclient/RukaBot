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

public class KickCommand extends Command {

    public KickCommand() {
        super(Commands.slash("kick", "Kick user from server")
                        .addOption(OptionType.USER, "user", "Select user", true)
                        .addOption(OptionType.STRING, "reason", "Reason", false)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS))
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

        BotHelper.safeDM(other, new CustomEmbed().create(CustomEmbed.Type.ERROR)
                        .setTitle(":interrobang: You have been kicked.")
                        .addField("Reason:", reason, false)
                        .addField("Moderator:", "@" + user.getName() + " (" + user.getAsMention() + ")", false)
                        .setFooter(BotApplication.getInstance().getConfig().getBotName(),
                                BotApplication.getInstance().getJda().getSelfUser().getAvatarUrl())
                        .setTimestamp(Instant.now()),
                unused -> kickUser(guild, other, reason, user, event),
                ex -> kickUser(guild, other, reason, user, event));
    }

    private void kickUser(Guild guild, User target, String reason, User kicker,
                          SlashCommandInteractionEvent event) {
        guild.kick(target).reason(reason + " | Moderator: @" + kicker.getName()).queue(unused -> {
            event.replyEmbeds(new CustomEmbed()
                    .create(CustomEmbed.Type.SUCCESS)
                    .setDescription(":white_check_mark: User @" + target.getName() + " (" + target.getAsMention() + ") has been kicked.")
                    .addField("Reason", reason, false)
                    .addField("Moderator", "@" + kicker.getName() + " (" + kicker.getAsMention() + ")", false)
                    .build()).queue();
        }, throwable -> event.replyEmbeds(new CustomEmbed()
                .create(CustomEmbed.Type.ERROR)
                .setDescription(":interrobang: Failed to kick this user.\nError: " + throwable.getMessage())
                .build()).queue());
    }
}
