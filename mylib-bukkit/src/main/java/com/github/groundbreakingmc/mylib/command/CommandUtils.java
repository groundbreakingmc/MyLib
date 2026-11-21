package com.github.groundbreakingmc.mylib.command;

import com.github.groundbreakingmc.mylib.vanish.VisibleChecker;
import com.github.groundbreakingmc.mylib.vanish.VisibleCheckerFactory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Helpers for tab completion: reading a raw command buffer, such as the one of Paper's
 * asynchronous tab completion, and filtering completions by what was typed.
 */
@UtilityClass
@SuppressWarnings("unused")
public final class CommandUtils {

    /**
     * Counts the spaces in a raw command buffer, which is how many arguments were started after
     * the label: 2 for {@code "/give Steve "}.
     *
     * @param input the raw buffer
     * @return the number of spaces
     */
    public static int getLength(final String input) {
        int amount = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == ' ') {
                amount++;
            }
        }

        return amount;
    }

    /**
     * Reads an argument of a raw command buffer.
     *
     * @param input  the raw buffer, such as {@code "/give Steve dia"}
     * @param length the number of spaces in it, from {@link #getLength(String)}
     * @param index  the argument, {@code 0} for the first one after the label
     * @return the argument, an empty string if it is still empty, or {@code null} if it wasn't started
     */
    @Nullable
    public static String getArgument(final String input, final int length, final int index) {
        if (length <= index) {
            return null;
        }

        int wordStart = -1;
        int wordEnd;
        int wordCount = 0;

        for (int i = input.indexOf(' '); i < input.length(); i++) {
            char c = input.charAt(i);

            if (Character.isWhitespace(c)) {
                if (wordStart != -1) {
                    wordEnd = i;
                    if (wordCount == index) {
                        return input.substring(wordStart, wordEnd);
                    }
                    wordStart = -1;
                    wordCount++;
                }
            } else {
                if (wordStart == -1) {
                    wordStart = i;
                }
            }
        }

        if (wordStart != -1) {
            wordEnd = input.length();
            if (wordCount == index) {
                return input.substring(wordStart, wordEnd);
            }
        }

        return "";
    }

    /**
     * @deprecated Use {@link StringUtil#startsWithIgnoreCase(String, String)} instead
     */
    @Deprecated
    public static boolean startsWithIgnoreCase(final String input, final String completion) {
        if (completion == null || input == null) {
            return false;
        }

        return completion.regionMatches(true, 0, input, 0, input.length());
    }

    /**
     * Checks whether the word before the one being typed is exactly {@code search}, so an argument
     * can be completed by the one before it.
     *
     * @param buffer the raw buffer, such as {@code "/kit give vip "}
     * @param search the word to look for, compared with case
     * @param length the number of spaces in the buffer, from {@link #getLength(String)}
     * @return whether the previous word is {@code search}; {@code false} while fewer than three spaces were typed
     */
    public static boolean isUsedBefore(final String buffer, final String search, final int length) {
        if (length <= 2) {
            return false;
        }

        // With three spaces or more, a space precedes the previous word too.
        final int end = buffer.lastIndexOf(' ');
        final int start = buffer.lastIndexOf(' ', end - 1) + 1;
        return end - start == search.length() && buffer.startsWith(search, start);
    }

    /**
     * Completes the last argument with the names of all online players, hidden ones included.
     * To leave out players the sender can't see, use
     * {@link #smartTabCompletePlayerNames(CommandSender, String[], VisibleChecker)}.
     *
     * @param args the arguments
     * @return the matching names
     */
    public static List<String> tabCompletePlayerNames(final String[] args) {
        if (args.length == 0) {
            return List.of();
        }

        return tabCompletePlayerNames(args[args.length - 1]);
    }

    /**
     * Completes an input with the names of all online players, hidden ones included.
     *
     * @param input the typed part of the name
     * @return the matching names
     */
    public static List<String> tabCompletePlayerNames(final String input) {
        final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        final List<String> completions = new ObjectArrayList<>(onlinePlayers.size());

        final Predicate<String> check = input.isEmpty()
                ? (playerName) -> true
                : (playerName) -> StringUtil.startsWithIgnoreCase(playerName, input);

        for (final Player player : onlinePlayers) {
            final String playerName = player.getName();
            if (check.test(playerName)) {
                completions.add(playerName);
            }
        }

        return completions;
    }

    /**
     * Completes the last argument with the names of players who have ever joined.
     *
     * @param args the arguments
     * @return the matching names
     */
    public static List<String> tabCompleteOfflinePlayerNames(final String[] args) {
        if (args.length == 0) {
            return List.of();
        }

        return tabCompleteOfflinePlayerNames(args[args.length - 1]);
    }

    /**
     * Completes an input with the names of players who have ever joined. It goes through all of
     * them on each call, which on a large server is thousands of players per keystroke.
     *
     * @param input the typed part of the name
     * @return the matching names
     */
    public static List<String> tabCompleteOfflinePlayerNames(final String input) {
        final OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        final List<String> completions = new ObjectArrayList<>(offlinePlayers.length);

        final Predicate<String> check = input.isEmpty()
                ? (playerName) -> true
                : (playerName) -> StringUtil.startsWithIgnoreCase(playerName, input);

        // No DRY, because Bukkit#getOfflinePlayers returns an array
        for (final OfflinePlayer player : offlinePlayers) {
            final String playerName = player.getName();
            if (playerName != null && check.test(playerName)) {
                completions.add(playerName);
            }
        }

        return completions;
    }

    /**
     * Completes the last argument with the names of online players the sender can see. A player
     * doesn't get the ones hidden from them, such as vanished players; the console gets everyone.
     *
     * @param sender         who completes
     * @param args           the arguments
     * @param visibleChecker the vanish plugin's own check, from {@link VisibleCheckerFactory#create()},
     *                       or {@code null} to rely on {@link Player#canSee(Player)} alone
     * @return the matching names
     */
    @NotNull
    public static List<String> smartTabCompletePlayerNames(@NotNull CommandSender sender, @NotNull String[] args, @Nullable VisibleChecker visibleChecker) {
        if (args.length == 0) {
            return List.of();
        }

        final String input = args[args.length - 1];
        return sender instanceof Player player
                ? smartTabCompletePlayerNames(player, input, visibleChecker)
                : tabCompletePlayerNames(input);
    }

    /**
     * Completes an input with the names of online players the sender can see.
     *
     * <p>Vanish plugins hide players with {@link Player#hidePlayer}, see-permissions applied, so
     * {@link Player#canSee(Player)} leaves them out on its own. The checker is asked as well, for
     * plugins that also hide players by other means, such as PremiumVanish with packets.
     *
     * @param sender         who completes
     * @param input          the typed part of the name
     * @param visibleChecker the vanish plugin's own check, from {@link VisibleCheckerFactory#create()},
     *                       or {@code null} to rely on {@link Player#canSee(Player)} alone
     * @return the matching names
     */
    @NotNull
    public static List<String> smartTabCompletePlayerNames(@NotNull Player sender, @NotNull String input, @Nullable VisibleChecker visibleChecker) {
        final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        final List<String> completions = new ObjectArrayList<>(onlinePlayers.size());

        for (final Player player : onlinePlayers) {
            if (!sender.canSee(player) || visibleChecker != null && !visibleChecker.canSee(sender, player)) {
                continue;
            }
            final String playerName = player.getName();
            if (StringUtil.startsWithIgnoreCase(playerName, input)) {
                completions.add(playerName);
            }
        }

        return completions;
    }

    /**
     * Completes an input with the entries of a list that start with it, ignoring case.
     *
     * @param input the typed part
     * @param list  the possible completions
     * @return the matching entries; the list itself if the input is empty
     */
    @NotNull
    public static List<String> tabComplete(@NotNull String input, @NotNull List<String> list) {
        if (input.isEmpty()) {
            return list;
        }

        final List<String> result = new ObjectArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            final String string = list.get(i);
            if (StringUtil.startsWithIgnoreCase(string, input)) {
                result.add(string);
            }
        }

        return result;
    }
}
