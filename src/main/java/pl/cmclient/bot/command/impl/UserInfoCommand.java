package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

import java.util.stream.Collectors;

public class UserInfoCommand extends Command {

    public UserInfoCommand() {
        super(Commands.slash("userinfo", "Informations about an user")
                        .addOption(OptionType.USER, "user", "Select user", false),
                CommandType.GENERAL, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("member");
        Member member = userOption == null ? event.getMember() : userOption.getAsMember();
        User user = member.getUser();

        event.replyEmbeds(new CustomEmbed()
                        .create(CustomEmbed.Type.SUCCESS)
                        .setDescription(member.getAsMention() + "'s informations")
                        .addField("Display name", user.getGlobalName() == null ? "none" : user.getGlobalName(), true)
                        .addField("User name", user.getName(), true)
                        .addField("User Id", user.getId(), true)
                        .addField("Activity", member.getActivities().isEmpty() ? "none" : member.getActivities().stream().map(Activity::getName).collect(Collectors.joining("\n")), true)
                        .setImage(user.getAvatarUrl())
                        .build())
                //.setEphemeral(true)
                .queue();
    }
}
