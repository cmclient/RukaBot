package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

public class KickCommand extends Command {

    public KickCommand() {
        super(Commands.slash("kick", "Kick user from server")
                        .addOption(OptionType.USER, "user", "Select user", true)
                        .addOption(OptionType.STRING, "reason", "Reason", false)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS))
                        .setGuildOnly(true),
                CommandType.MODERATION, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getOption("user").getAsMember();
        String reason = event.getOption("reason") == null ? "No reason" : event.getOption("reason").getAsString();

        if (!event.getGuild().getSelfMember().canInteract(member)) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle(":warning: I do not have permission to interact with this user.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        member.kick().queue(unused -> event.replyEmbeds(new CustomEmbed()
                        .create(CustomEmbed.Type.SUCCESS)
                        .setDescription(":white_check_mark: User " + member.getAsMention() + " has been kicked from this server")
                        .addField("Reason", reason, false).build()).queue(),
                throwable -> event.replyEmbeds(new CustomEmbed()
                                .create(CustomEmbed.Type.ERROR)
                                .setTitle(":warning: Unable to kick user " + member.getAsMention() + ".")
                                .addField("Error", throwable.toString(), false)
                                .build())
                        .setEphemeral(true)
                        .queue());
    }
}
