package com.github.groundbreakingmc.mylib.utils.time;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public record TimeValues(
        String days,
        String hours,
        String minutes,
        String seconds
) {

    public TimeValues(@NotNull String days,
                      @NotNull String hours,
                      @NotNull String minutes,
                      @NotNull String seconds) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }
}
