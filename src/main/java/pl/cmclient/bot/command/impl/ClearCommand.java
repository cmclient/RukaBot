package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ClearCommand extends Command {

    public ClearCommand() {
        super(Commands.slash("clear", "Purge the chat")
                        .addOption(OptionType.INTEGER, "amount", "Amount of messages to delete", true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
                        .setContexts(InteractionContextType.GUILD),
                CommandType.MODERATION,
                false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        AtomicInteger amount = new AtomicInteger(Math.max(event.getOption("amount").getAsInt(), 1));
        AtomicInteger deleted = new AtomicInteger(0);

        event.deferReply(true).queue(interactionHook -> {
            Runnable deleteBatch = new Runnable() {
                @Override
                public void run() {
                    int batchSize = Math.min(amount.get(), 100);
                    event.getChannel().getHistory().retrievePast(batchSize).queue(messages -> {
                        if (messages.isEmpty()) {
                            interactionHook.editOriginalEmbeds(new CustomEmbed()
                                            .create(CustomEmbed.Type.SUCCESS)
                                            .setTitle("<:cm_checkbox:1296554768747073549> Purged " + deleted.get() + " messages.")
                                            .build())
                                    .queue();
                            return;
                        }

                        event.getChannel().purgeMessages(messages).forEach(CompletableFuture::join);
                        deleted.addAndGet(messages.size());
                        amount.addAndGet(-messages.size());

                        interactionHook.editOriginalEmbeds(new CustomEmbed()
                                        .create(CustomEmbed.Type.WARNING)
                                        .setTitle("<:cm_information:1263254317897486437> Purging messages...")
                                        .setDescription("Deleted " + deleted.get() + "/" + (deleted.get() + amount.get()) + " so far")
                                        .build())
                                .queue();

                        if (amount.get() > 0) run();
                        else interactionHook.editOriginalEmbeds(new CustomEmbed()
                                        .create(CustomEmbed.Type.SUCCESS)
                                        .setTitle("<:cm_checkbox:1296554768747073549> Purged " + deleted.get() + " messages.")
                                        .build())
                                .queue();
                    });
                }
            };

            deleteBatch.run();
        });
    }
}
