package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
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
import java.util.concurrent.atomic.AtomicInteger;

public class UnbanAllCommand extends Command {

    public UnbanAllCommand() {
        super(Commands.slash("unbanall", "Unban all people from the server")
                        .addOption(OptionType.BOOLEAN, "accept", "Are you sure? This action is irreversible!", true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .setContexts(InteractionContextType.GUILD),
                CommandType.ADMINISTRATION, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping acceptOption = event.getOption("accept");
        Guild guild = event.getGuild();

        guild.retrieveBanList().queue(bans -> {
            if (!acceptOption.getAsBoolean()) {
                event.replyEmbeds(new CustomEmbed()
                                .create(CustomEmbed.Type.ERROR)
                                .setTitle("You didn't accept the question.")
                                .setDescription("There are **" + bans.size() + "** banned users.")
                                .build())
                        .setEphemeral(true)
                        .queue();
                return;
            }

            int totalBans = bans.size();
            if (totalBans == 0) {
                event.replyEmbeds(new CustomEmbed()
                                .create(CustomEmbed.Type.WARNING)
                                .setTitle("No banned users found.")
                                .build())
                        .setEphemeral(true)
                        .queue();
                return;
            }

            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.WARNING)
                            .setTitle("Removing " + totalBans + " bans...")
                            .setDescription("Progress: 0/" + totalBans)
                            .build())
                    .queue(interactionHook -> {
                        Instant start = Instant.now();
                        AtomicInteger unbannedCount = new AtomicInteger(0);
                        AtomicInteger lastUpdate = new AtomicInteger(0);

                        bans.forEach(ban -> guild.unban(ban.getUser()).queue(
                                success -> {
                                    int current = unbannedCount.incrementAndGet();
                                    
                                    if (current - lastUpdate.get() >= 10 || current == totalBans) {
                                        lastUpdate.set(current);
                                        
                                        if (current == totalBans) {
                                            Instant end = Instant.now();
                                            Duration duration = Duration.between(start, end);
                                            String timeElapsed = StringHelper.formatDuration(duration);

                                            interactionHook.editOriginalEmbeds(new CustomEmbed()
                                                    .create(CustomEmbed.Type.SUCCESS)
                                                    .setTitle("Completed, removed " + totalBans + " bans.")
                                                    .addField("Time Elapsed", timeElapsed, false)
                                                    .build()).queue();
                                        } else {
                                            interactionHook.editOriginalEmbeds(new CustomEmbed()
                                                    .create(CustomEmbed.Type.WARNING)
                                                    .setTitle("Removing " + totalBans + " bans...")
                                                    .setDescription("Progress: " + current + "/" + totalBans)
                                                    .build()).queue();
                                        }
                                    }
                                },
                                error -> {
                                    int current = unbannedCount.incrementAndGet();
                                    if (current == totalBans) {
                                        Instant end = Instant.now();
                                        Duration duration = Duration.between(start, end);
                                        String timeElapsed = StringHelper.formatDuration(duration);

                                        interactionHook.editOriginalEmbeds(new CustomEmbed()
                                                .create(CustomEmbed.Type.SUCCESS)
                                                .setTitle("Completed, removed " + totalBans + " bans.")
                                                .addField("Time Elapsed", timeElapsed, false)
                                                .build()).queue();
                                    }
                                }
                        ));
                    });
        });
    }
}
