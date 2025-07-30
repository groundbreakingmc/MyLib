package com.github.groundbreakingmc.mylib.utils.time;

import org.jetbrains.annotations.NotNull;

public class TimeFormatter {

    private final TimeValues timeValues;

    public TimeFormatter(@NotNull TimeValues timeValues) {
        this.timeValues = timeValues;
    }

    public String getTime(long totalSeconds) {
        return TimeFormatterUtil.getTime(totalSeconds, this.timeValues);
    }
}
