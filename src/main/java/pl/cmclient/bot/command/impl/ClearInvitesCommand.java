package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.helper.StringHelper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClearInvitesCommand extends Command {

    public ClearInvitesCommand() {
        super(Commands.slash("clearinvites", "Removes unused invites")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                        .setGuildOnly(true),
                CommandType.ADMINISTRATION, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.getGuild().retrieveInvites().queue(invites -> {
            List<Invite> filtered = invites.stream().filter(invite -> invite.getUses() < 5).toList();

            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.WARNING)
                            .setTitle("Removing " + filtered.size() + " invites...")
                            .build())
                    //.setEphemeral(true)
                    .queue(interactionHook -> {
                        Instant start = Instant.now();

                        // Use AtomicInteger to count the number of invites deleted
                        AtomicInteger deletedCount = new AtomicInteger(0);

                        filtered.forEach(invite -> invite.delete().queue(
                                success -> {
                                    // Increment the counter for each successful deletion
                                    deletedCount.incrementAndGet();

                                    // Check if all invites are deleted
                                    if (deletedCount.get() == filtered.size()) {
                                        Instant end = Instant.now();
                                        Duration duration = Duration.between(start, end);
                                        String timeElapsed = StringHelper.formatDuration(duration);

                                        // Edit the message after all invites are deleted
                                        interactionHook.editOriginalEmbeds(new CustomEmbed()
                                                .create(CustomEmbed.Type.SUCCESS)
                                                .setTitle("Completed, removed " + deletedCount.get() + " invites.")
                                                .addField("Time Elapsed", timeElapsed, false)
                                                .build()).queue();
                                    }
                                },
                                error -> interactionHook.editOriginalEmbeds(new CustomEmbed()
                                        .create(CustomEmbed.Type.ERROR)
                                        .setTitle("Failed to clear invites.")
                                        .addField("Error", error.toString(), false)
                                        .build()).queue()
                        ));
                    });
        });
    }
}
