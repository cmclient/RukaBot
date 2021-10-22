package pl.cmclient.bot.helper;

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
}
