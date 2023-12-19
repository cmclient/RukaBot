package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.helper.StringHelper;

import java.time.Duration;
import java.time.Instant;

public class UnbanAllCommand extends Command {

    public UnbanAllCommand() {
        super(Commands.slash("unbanall", "Unban all people from the server")
                        .addOption(OptionType.BOOLEAN, "accept", "Are you sure? This action is irreversible!", true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
                        .setGuildOnly(true),
                CommandType.ADMINISTRATION, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping acceptOption = event.getOption("accept");

        if (!acceptOption.getAsBoolean()) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle("You didn't accepted the question.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Guild guild = event.getGuild();

        guild.retrieveBanList().queue(bans -> event.replyEmbeds(new CustomEmbed()
                        .create(CustomEmbed.Type.WARNING)
                        .setTitle("Removing " + bans.size() + " bans...")
                        .build())
                //.setEphemeral(true)
                .queue(interactionHook -> {
                    Instant start = Instant.now();
                    bans.forEach(ban -> guild.unban(ban.getUser()).queue());
                    Instant end = Instant.now();
                    Duration duration = Duration.between(start, end);
                    String timeElapsed = StringHelper.formatDuration(duration);

                    interactionHook.editOriginalEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.SUCCESS)
                            .setTitle("Completed, removed " + bans.size() + " bans.")
                            .addField("Time Elapsed", timeElapsed, false)
                            .build()).queue();
                }));
    }
}
