package pl.cmclient.bot.command.impl;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import dev.lavalink.youtube.YoutubeSource;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteJDBCLoader;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.manager.MusicManager;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

public class BotInfoCommand extends Command {

    private static final String REPOSITORY = "https://github.com/cmclient/RukaBot";
    private static final Properties VERSION = readBuildProperties();

    public BotInfoCommand() {
        super(Commands.slash("botinfo", "Everything about this bot"), CommandType.GENERAL, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        SelfUser self = event.getJDA().getSelfUser();
        String avatar = self.getEffectiveAvatarUrl();
        MusicManager musicManager = this.getBot().getMusicManager();

        long servers = event.getJDA().getGuildCache().size();
        long users = event.getJDA().getGuildCache().stream().mapToLong(Guild::getMemberCount).sum();

        event.replyEmbeds(new CustomEmbed()
                        .create(CustomEmbed.Type.SUCCESS)
                        .setAuthor(self.getName(), REPOSITORY, avatar)
                        .setThumbnail(avatar)
                        .setTitle(":robot: Bot information")
                        .setDescription("An open-source Discord music bot written in Java. Use `/help` to see what I can do.")
                        .addField(":earth_americas: Servers", String.valueOf(servers), true)
                        .addField(":busts_in_silhouette: Users", String.format("%,d", users), true)
                        .addField(":jigsaw: Commands", String.valueOf(this.getBot().getCommandManager().getCommands().size()), true)
                        .addField(":headphones: Playing now", musicManager.getActivePlayerCount() + " / " + servers, true)
                        .addField(":watch: Ping", event.getJDA().getGatewayPing() + "ms", true)
                        .addField(":hourglass: Uptime", getUptime(), true)
                        .addField(":musical_note: Music engine", getMusicEngine(musicManager), true)
                        .addField(":floppy_disk: Memory", getMemoryUsage(), true)
                        .addField(":gear: Version", getBotVersion(), true)
                        .addField(":books: Built with", getLibraries(), true)
                        .addField(":computer: Running on", getRuntimeInfo(), true)
                        .addField(":link: Links", "[Invite me](" + event.getJDA().getInviteUrl(Permission.ADMINISTRATOR) + ")\n[Source code](" + REPOSITORY + ")", true)
                        .build())
                .queue();
    }

    /**
     * Tells the user which YouTube backend is live, since playback quality depends entirely on it.
     */
    private static String getMusicEngine(MusicManager musicManager) {
        String version = musicManager.getYtDlpVersion();
        String engine = "`" + musicManager.getYoutubeSourceName() + (version == null ? "" : " " + version) + "`";
        return engine + "\nSoundCloud, direct links";
    }

    private static String getLibraries() {
        // JDAInfo.VERSION is "6.7.0_7c90001"; the trailing commit hash is noise for an info embed.
        return "JDA `" + JDAInfo.VERSION.split("_")[0] + "`"
                + "\nLavaplayer `" + PlayerLibrary.VERSION.trim() + "`"
                + "\nyoutube-source `" + YoutubeSource.VERSION.trim() + "`"
                + "\nSQLite `" + SQLiteJDBCLoader.getVersion() + "`"
                + "\nSpotify Web API, Gson";
    }

    private static String getRuntimeInfo() {
        return "Java `" + System.getProperty("java.version") + "`"
                + "\n" + System.getProperty("os.name")
                + "\n" + Runtime.getRuntime().availableProcessors() + " CPU core(s)";
    }

    private static String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long used = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        long max = runtime.maxMemory() / 1048576L;
        return used + " MB / " + max + " MB";
    }

    /**
     * Reads the version Maven filtered into rukabot.properties, which works both from the jar and
     * from an IDE run. Falls back to the jar manifest, then to a placeholder.
     */
    private static String getBotVersion() {
        String version = VERSION.getProperty("version");
        if (version == null || version.isBlank() || version.startsWith("${")) {
            version = BotInfoCommand.class.getPackage().getImplementationVersion();
        }
        return "`" + (version == null ? "dev build" : version) + "`";
    }

    private static Properties readBuildProperties() {
        Properties properties = new Properties();
        try (InputStream stream = BotInfoCommand.class.getResourceAsStream("/rukabot.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException ex) {
            LoggerFactory.getLogger(BotInfoCommand.class).warn("Failed to read build properties", ex);
        }
        return properties;
    }

    private static String getUptime() {
        Instant startedAt = ProcessHandle.current().info().startInstant().orElse(null);
        if (startedAt == null) {
            return "unknown";
        }
        Duration uptime = Duration.between(startedAt, Instant.now());
        long days = uptime.toDays();
        long hours = uptime.toHoursPart();
        long minutes = uptime.toMinutesPart();
        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m " + uptime.toSecondsPart() + "s";
    }
}
