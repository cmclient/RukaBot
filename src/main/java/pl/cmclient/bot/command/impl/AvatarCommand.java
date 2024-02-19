package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

public class AvatarCommand extends Command {

    public AvatarCommand() {
        super(Commands.slash("avatar", "Sends your avatar")
                .addOption(OptionType.USER, "user", "Select user", false), CommandType.GENERAL, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        User user = userOption == null ? event.getUser() : userOption.getAsUser();

        event.replyEmbeds(new CustomEmbed()
                                .create(CustomEmbed.Type.SUCCESS)
                                .setDescription(user.getAsMention() + "'s avatar")
                                .setImage(user.getAvatarUrl())
                                .build())
                .setEphemeral(true)
                .queue();
    }
}
