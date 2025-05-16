package com.github.groundbreakingmc.mylib.colorizer.component;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.colorizer.legacy.StringColorizer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface ComponentColorizer extends Colorizer<Component> {

    @NotNull
    StringColorizer getStringColorizer();
}
