package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

import java.util.concurrent.CompletableFuture;

public class ClearCommand extends Command {

    public ClearCommand() {
        super(Commands.slash("clear", "Purge the chat")
                        .addOption(OptionType.INTEGER, "amount", "Amount of messages to delete", true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
                        .setGuildOnly(true),
                CommandType.MODERATION, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        int amount = event.getOption("amount").getAsInt();

        event.deferReply(true).queue(interactionHook -> event.getChannel().getHistory().retrievePast(amount).queue(messages -> {
            event.getChannel().purgeMessages(messages).forEach(CompletableFuture::join);
            interactionHook.editOriginalEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.SUCCESS)
                            .setTitle("Purged " + amount + " messages.")
                            .build())
                    .queue();
        }));
    }
}
