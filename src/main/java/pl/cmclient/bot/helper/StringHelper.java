package pl.cmclient.bot.helper;

import java.time.Duration;
import java.util.Collection;

public final class StringHelper {

    private StringHelper() {}

    public static String join(Collection<String> collection, String separator) {
        StringBuilder builder = new StringBuilder();
        for (String s : collection) {
            if (builder.length() != 0) {
                builder.append(separator);
            }
            builder.append(s);
        }
        return builder.toString();
    }

    public static String join(String[] array, String separator, int start, int end) {
        int size = (end - start) * (((array[start] == null) ? 16 : array[start].length()) + separator.length());
        StringBuilder builder = new StringBuilder(size);
        for (int i = start; i < end; ++i) {
            if (i > start) {
                builder.append(separator);
            }
            if (array[i] != null) {
                builder.append(array[i]);
            }
        }
        return builder.toString();
    }

    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);

        return seconds < 0 ? "-" + positive : positive;
    }
}
