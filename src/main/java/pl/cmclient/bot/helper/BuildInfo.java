package pl.cmclient.bot.helper;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Exposes build metadata that Maven filters into rukabot.properties at build time.
 */
public final class BuildInfo {

    private static final String VERSION = readVersion();

    private BuildInfo() {}

    public static String getVersion() {
        return VERSION;
    }

    /**
     * Reads the version Maven filtered into rukabot.properties, which works both from the jar and
     * from an IDE run. Falls back to the jar manifest, then to a placeholder.
     */
    private static String readVersion() {
        Properties properties = new Properties();
        try (InputStream stream = BuildInfo.class.getResourceAsStream("/rukabot.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException ex) {
            LoggerFactory.getLogger(BuildInfo.class).warn("Failed to read build properties", ex);
        }
        String version = properties.getProperty("version");
        if (version == null || version.isBlank() || version.startsWith("${")) {
            version = BuildInfo.class.getPackage().getImplementationVersion();
        }
        return version == null ? "dev build" : version;
    }
}
