package com.github.groundbreakingmc.mylib.utils.player.settings;

import lombok.Builder;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

@Builder
public class EffectSettings {

    private PotionEffect potionEffect;

    public final PotionEffectType effectType;
    @Builder.Default
    public final int duration = 3;
    @Builder.Default
    public final int amplifier = 0;

    public PotionEffect getPotionEffect() {
        if (this.potionEffect != null) {
            return this.potionEffect;
        }

        return (this.potionEffect = new PotionEffect(
                this.effectType,
                this.duration,
                this.amplifier
        ));
    }

    @Nullable
    public static EffectSettings get(final String string) {
        if (string == null) {
            return null;
        }

        final String[] params = string.split(";");
        final EffectSettingsBuilder builder = builder();
        builder.effectType(PotionEffectType.getByName(params[0]));
        if (params.length > 1) {
            builder.duration(Integer.parseInt(params[1]));
        }
        if (params.length > 2) {
            builder.amplifier(Integer.parseInt(params[2]));
        }

        return builder.build();
    }

    @Override
    public String toString() {
        return this.effectType.getName() + ";" + this.duration + ";" + this.amplifier;
    }
}
