package pl.cmclient.bot.helper;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DateHelper {

    private static final Pattern PATTERN = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);

    private static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
        int fromYear = fromDate.get(Calendar.YEAR);
        int toYear = toDate.get(Calendar.YEAR);
        if (Math.abs(fromYear - toYear) > 100000) {
            toDate.set(Calendar.YEAR, fromYear + (future ? 100000 : -100000));
        }
        int diff = 0;
        long savedDate = fromDate.getTimeInMillis();
        while ((future && !fromDate.after(toDate)) || (!future && !fromDate.before(toDate))) {
            savedDate = fromDate.getTimeInMillis();
            fromDate.add(type, future ? 1 : -1);
            ++diff;
        }
        --diff;
        fromDate.setTimeInMillis(savedDate);
        return diff;
    }

    public static String formatDateDiff(long date) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(date);
        Calendar now = new GregorianCalendar();
        return formatDateDiff(now, c);
    }

    private static String formatDateDiff(Calendar fromDate, Calendar toDate) {
        boolean future = false;
        if (toDate.equals(fromDate)) {
            return "now";
        }
        if (toDate.after(fromDate)) {
            future = true;
        }
        StringBuilder sb = new StringBuilder();
        int[] types = { 1, 2, 5, 11, 12, 13 };
        String[] names = {
                "year", "years",
                "month", "months",
                "day", "days",
                "hour", "hours",
                "minute", "minutes",
                "second", "seconds"
        };
        for (int accuracy = 0, i = 0; i < types.length && accuracy <= 2; ++i) {
            int diff = dateDiff(types[i], fromDate, toDate, future);
            if (diff > 0) {
                ++accuracy;
                sb.append(" ").append(diff).append(" ").append(names[i * 2 + ((diff > 1) ? 1 : 0)]);
            }
        }
        if (sb.length() == 0) {
            return "now";
        }
        return sb.toString().trim();
    }
    
    public static long stringToTime(String time) {
        try {
            Matcher m = PATTERN.matcher(time);
            int years = 0;
            int months = 0;
            int weeks = 0;
            int days = 0;
            int hours = 0;
            int minutes = 0;
            int seconds = 0;
            boolean found = false;
            while (m.find()) {
                if (m.group() != null && !m.group().isEmpty()) {
                    for (int i = 0; i < m.groupCount(); ++i) {
                        if (m.group(i) != null && !m.group(i).isEmpty()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        continue;
                    }
                    if (m.group(1) != null && !m.group(1).isEmpty()) {
                        years = Integer.parseInt(m.group(1));
                    }
                    if (m.group(2) != null && !m.group(2).isEmpty()) {
                        months = Integer.parseInt(m.group(2));
                    }
                    if (m.group(3) != null && !m.group(3).isEmpty()) {
                        weeks = Integer.parseInt(m.group(3));
                    }
                    if (m.group(4) != null && !m.group(4).isEmpty()) {
                        days = Integer.parseInt(m.group(4));
                    }
                    if (m.group(5) != null && !m.group(5).isEmpty()) {
                        hours = Integer.parseInt(m.group(5));
                    }
                    if (m.group(6) != null && !m.group(6).isEmpty()) {
                        minutes = Integer.parseInt(m.group(6));
                    }
                    if (m.group(7) == null) {
                        break;
                    }
                    if (m.group(7).isEmpty()) {
                        break;
                    }
                    seconds = Integer.parseInt(m.group(7));
                    break;
                }
            }
            if (!found) {
                return -1L;
            }
            Calendar c = new GregorianCalendar();
            if (years > 0) {
                c.add(Calendar.YEAR, years);
            }
            if (months > 0) {
                c.add(Calendar.MONTH, months);
            }
            if (weeks > 0) {
                c.add(Calendar.WEEK_OF_YEAR, weeks);
            }
            if (days > 0) {
                c.add(Calendar.DATE, days);
            }
            if (hours > 0) {
                c.add(Calendar.HOUR_OF_DAY, hours);
            }
            if (minutes > 0) {
                c.add(Calendar.MINUTE, minutes);
            }
            if (seconds > 0) {
                c.add(Calendar.SECOND, seconds);
            }
            Calendar max = new GregorianCalendar();
            max.add(Calendar.YEAR, 10);
            if (c.after(max)) {
                return max.getTimeInMillis();
            }
            return c.getTimeInMillis();
        }
        catch (Exception e) {
            return -1L;
        }
    }
}
