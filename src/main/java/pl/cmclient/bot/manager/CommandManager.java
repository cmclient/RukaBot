package pl.cmclient.bot.manager;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.command.impl.*;
import pl.cmclient.bot.helper.StringHelper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class CommandManager {

    private final Map<String, Command> commands = new LinkedHashMap<>();

    private transient final List<Command> availableCommands = List.of(
            // GLOBAL
            (new ConfigCommand()),

            // GENERAL
            (new HelpCommand()),
            (new AvatarCommand()),
            (new PingCommand()),
            (new UserInfoCommand()),

            // ADMINISTRATION
            (new ClearInvitesCommand()),
            (new UnbanAllCommand()),

            // MODERATION
            (new KickCommand()),
            (new BanCommand()),
            (new ClearCommand()),

            // MUSIC
            (new PlayCommand()),
            (new StopCommand()),
            (new LeaveCommand()),
            (new NowPlayingCommand()),
            (new LoopCommand()),
            (new SeekCommand()),
            (new SkipCommand()),
            (new QueueCommand()),
            (new ClearQueueCommand()),
            (new VolumeCommand())
    );

    public void load(BotApplication bot, JDA jda) {
        this.availableCommands.forEach(command -> command.register(bot));
        List<SlashCommandData> data = this.commands.values().stream().map(Command::getData).collect(Collectors.toList());
        jda.updateCommands().addCommands(data).queue();
        bot.getLogger().info("Loaded {} commands.", this.commands.size());
    }

    public Optional<Command> get(String name) {
        return Optional.of(this.commands.get(name));
    }

    public String getCommandsList() {
        StringBuilder sb = new StringBuilder("\n");
        for (CommandType type : CommandType.values()) {
            List<String> list = this.commands.values().stream().filter(command -> command.getCommandType() == type).map(command -> "`" + command.getData().getName() + "`").collect(Collectors.toList());
            if (!list.isEmpty()) {
                sb.append(type.getName()).append(":\n");
                sb.append(StringHelper.join(list, ", ")).append("\n\n");
            }
        }
        return sb.toString();
    }
}
