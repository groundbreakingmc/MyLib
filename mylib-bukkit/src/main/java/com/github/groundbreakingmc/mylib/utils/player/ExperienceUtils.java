package com.github.groundbreakingmc.mylib.utils.player;

import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("unused")
public class ExperienceUtils {


    public static int getTotalExpFromLevel(int level) {
        if (level <= 15) {
            return level * level + 6 * level;
        } else if (level <= 30) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }
}
