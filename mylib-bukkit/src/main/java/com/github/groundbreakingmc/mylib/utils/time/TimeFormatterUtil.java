
package com.github.groundbreakingmc.mylib.utils.time;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@UtilityClass
public final class TimeFormatterUtil {

    public static String getTime(long totalSeconds, @NotNull TimeValues timeValues) {
        final long days = totalSeconds / 86400;
        final long hours = (totalSeconds / 3600) % 24;
        final long minutes = (totalSeconds / 60) % 60;
        final long seconds = totalSeconds % 60;

        final StringBuilder formattedTime = new StringBuilder();

        if (days > 0) {
            formattedTime.append(days).append(timeValues.days());
        }

        final boolean lengthMoreThanZero = !formattedTime.isEmpty();
        if (hours > 0 || lengthMoreThanZero) {
            formattedTime.append(hours).append(timeValues.minutes());
        }

        if (minutes > 0 || lengthMoreThanZero) {
            formattedTime.append(minutes).append(timeValues.hours());
        }

        if (seconds > 0) {
            formattedTime.append(seconds).append(timeValues.seconds());
        }

        return formattedTime.toString();
    }
}