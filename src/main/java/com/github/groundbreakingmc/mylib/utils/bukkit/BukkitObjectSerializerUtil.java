package com.github.groundbreakingmc.mylib.utils.bukkit;

import lombok.experimental.UtilityClass;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@UtilityClass
@SuppressWarnings("unused")
public class BukkitObjectSerializerUtil {

    public static <T> byte @NotNull [] serialize(@NotNull T inventory) {
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        try (final ObjectOutputStream outputStream = new BukkitObjectOutputStream(byteOutputStream)) {
            outputStream.writeObject(inventory);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }

        return byteOutputStream.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte @NotNull [] blob, Class<T> clazz) {
        try (final ByteArrayInputStream byteIn = new ByteArrayInputStream(blob)) {
            final BukkitObjectInputStream in = new BukkitObjectInputStream(byteIn);
            return (T) in.readObject();
        } catch (final IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
