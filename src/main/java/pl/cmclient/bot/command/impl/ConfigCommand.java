package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.helper.StringHelper;
import pl.cmclient.bot.object.ServerData;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super(Commands.slash("config", "Configure bot for your server")
                        .addOption(OptionType.STRING, "key", "Key", true)
                        .addOption(OptionType.STRING, "value", "Value", false)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                        .setGuildOnly(true),
                CommandType.GLOBAL, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String key = event.getOption("key").getAsString();
        String value = event.getOption("value") == null ? null : event.getOption("value").getAsString();
        ServerData serverData = this.getBot().getServerDataManager().get(event.getGuild().getIdLong());

        switch (key.toLowerCase()) {
            case "invitebans": {
                if (value == null) {
                    event.replyEmbeds(new CustomEmbed()
                                    .create(CustomEmbed.Type.SUCCESS)
                                    .setTitle("Automatic bans for sending invites are **" + (serverData.isInviteBans() ? "enabled" : "disabled") + "**")
                                    .build())
                            //.setEphemeral(true)
                            .queue();
                    return;
                }

                boolean inviteBans = Boolean.parseBoolean(value);
                serverData.setInviteBans(inviteBans);

                event.replyEmbeds(new CustomEmbed()
                                .create(CustomEmbed.Type.SUCCESS)
                                .setTitle("Automatic bans for sending has been **" + (inviteBans ? "enabled" : "disabled") + "**")
                                .build())
                        .setEphemeral(true)
                        .queue();
                break;
            }
            case "bannedwords": {
                if (value == null) {
                    event.replyEmbeds(new CustomEmbed()
                                    .create(CustomEmbed.Type.SUCCESS)
                                    .setTitle("Currently banned words:\n" + StringHelper.join(serverData.getBannedWords(), ", "))
                                    .build())
                            //.setEphemeral(true)
                            .queue();
                    return;
                }

                if (serverData.getBannedWords().contains(value)) {
                    serverData.removeBannedWord(value);
                    event.replyEmbeds(new CustomEmbed()
                                    .create(CustomEmbed.Type.SUCCESS)
                                    .setTitle("Removed banned word: **" + value + "**")
                                    .build())
                            //.setEphemeral(true)
                            .queue();
                } else {
                    serverData.addBannedWord(value);
                    event.replyEmbeds(new CustomEmbed()
                                    .create(CustomEmbed.Type.SUCCESS)
                                    .setTitle("Added banned word: **" + value + "**")
                                    .build())
                            //.setEphemeral(true)
                            .queue();
                }
            }
            default: {
                event.replyEmbeds(new CustomEmbed()
                                .create(CustomEmbed.Type.WARNING)
                                .setTitle("Available configuration keys: **inviteBans**, **bannedWords**")
                                .build())
                        //.setEphemeral(true)
                        .queue();
                break;
            }
        }
    }
}
