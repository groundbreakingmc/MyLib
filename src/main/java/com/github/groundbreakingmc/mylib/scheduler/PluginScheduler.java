package com.github.groundbreakingmc.mylib.scheduler;

import com.github.groundbreakingmc.mylib.server.ServerInfo;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Schedules plugin tasks on Bukkit/Spigot and Paper/Folia without exposing their task types.
 *
 * <p>Async delays take a {@link TimeUnit}. They are wall-clock on Paper/Folia, but are
 * approximated to server ticks on Bukkit/Spigot (50 ms per tick, rounded up). All other
 * delays are in server ticks. A zero tick delay means the next available tick.
 *
 * <p>On Folia, global tasks must not access arbitrary entities, blocks or regions.
 * Use {@link #at(Entity, Runnable)} or {@link #at(Location, Runnable)} instead.
 *
 * <p>If a task throws, the exception is handled by the server scheduler. Whether a repeating
 * task keeps running afterwards is server-defined, so catch exceptions inside timer bodies
 * if that matters.
 */
public interface PluginScheduler {

    /**
     * Creates the appropriate scheduler for the current server.
     *
     * @param plugin the plugin that owns scheduled tasks
     * @return a Bukkit or Paper/Folia scheduler
     */
    static @NotNull PluginScheduler of(@NotNull Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        return ServerInfo.hasFoliaScheduler()
                ? new PaperPluginScheduler(plugin)
                : new BukkitPluginScheduler(plugin);
    }

    /**
     * A cancellable task handle. Cancelling does not interrupt a currently executing task.
     * Completing a one-shot task does not make {@link #isCancelled()} return {@code true}.
     */
    interface Task {

        /**
         * A task that was never scheduled. It is returned when an entity task cannot be
         * scheduled because the entity is already retired. {@link #cancel()} is a no-op.
         */
        Task CANCELLED = new Task() {
            @Override
            public void cancel() {
            }

            @Override
            public boolean isCancelled() {
                return true;
            }
        };

        /** Cancels future execution(s) of this task. */
        void cancel();

        /**
         * Returns whether this task was cancelled, not whether it completed normally.
         *
         * @return {@code true} if the task is cancelled or was never scheduled
         */
        boolean isCancelled();
    }

    /**
     * Schedules an asynchronous task as soon as possible.
     *
     * @param task the operation to execute without using server-thread-only API
     * @return the cancellable task handle
     */
    @NotNull Task async(@NotNull Runnable task);

    /**
     * Schedules an asynchronous task after a non-negative delay.
     *
     * @param task  the asynchronous operation
     * @param delay the delay amount
     * @param unit  the unit of {@code delay} (tick-based approximation on Bukkit/Spigot)
     * @return the cancellable task handle
     */
    @NotNull Task asyncLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);

    /**
     * Repeats an asynchronous task after an initial delay. The task receives its own handle,
     * so it can cancel itself.
     *
     * @param task   the asynchronous operation
     * @param delay  non-negative initial delay
     * @param period strictly positive period
     * @param unit   the unit of {@code delay} and {@code period}
     * @return the cancellable task handle
     */
    @NotNull Task asyncTimer(@NotNull Consumer<Task> task, long delay, long period, @NotNull TimeUnit unit);

    /**
     * Schedules a task on the global region (main thread on Bukkit/Spigot).
     *
     * @param task the operation to execute
     * @return the cancellable task handle
     */
    @NotNull Task global(@NotNull Runnable task);

    /**
     * Schedules a global task after a non-negative tick delay.
     *
     * @param task  the operation to execute
     * @param delay delay in ticks
     * @return the cancellable task handle
     */
    @NotNull Task globalLater(@NotNull Runnable task, long delay);

    /**
     * Schedules a repeating global task. The task receives its own handle,
     * so it can cancel itself.
     *
     * @param task   the operation to execute
     * @param delay  non-negative initial delay in ticks
     * @param period strictly positive period in ticks
     * @return the cancellable task handle
     */
    @NotNull Task globalTimer(@NotNull Consumer<Task> task, long delay, long period);

    /**
     * Schedules a task on the region owning a fixed location.
     * On Bukkit/Spigot this uses the main thread.
     *
     * @param location the location whose region will own the task
     * @param task     the operation to execute
     * @return the cancellable task handle
     */
    @NotNull Task at(@NotNull Location location, @NotNull Runnable task);

    /**
     * Schedules a location-bound task after a non-negative tick delay.
     *
     * @param location the fixed location whose region will own the task
     * @param task     the operation to execute
     * @param delay    delay in ticks
     * @return the cancellable task handle
     */
    @NotNull Task atLater(@NotNull Location location, @NotNull Runnable task, long delay);

    /**
     * Schedules a repeating task bound to a fixed location. The task receives its own handle,
     * so it can cancel itself.
     *
     * @param location the fixed location whose region will own the task
     * @param task     the operation to execute
     * @param delay    non-negative initial delay in ticks
     * @param period   strictly positive period in ticks
     * @return the cancellable task handle
     */
    @NotNull Task atTimer(@NotNull Location location, @NotNull Consumer<Task> task, long delay, long period);

    /**
     * Runs the task immediately if the current thread already owns the location,
     * otherwise schedules it like {@link #at(Location, Runnable)}.
     *
     * @param location the location whose region must run the task
     * @param task     the operation to execute
     */
    void execute(@NotNull Location location, @NotNull Runnable task);

    /**
     * Schedules a task on the region currently owning an entity.
     *
     * @param entity the entity whose scheduler will execute the task
     * @param task   the operation to execute
     * @return the task handle, or {@link Task#CANCELLED} if the entity is already retired
     */
    default @NotNull Task at(@NotNull Entity entity, @NotNull Runnable task) {
        return this.at(entity, task, null);
    }

    /**
     * Schedules a task on an entity's owning region with an optional retirement callback.
     * The callback runs if the entity is retired before execution (best effort on Bukkit).
     * Paper/Folia retirement callbacks run in a restricted region context.
     *
     * @param entity  the target entity
     * @param task    the operation to execute
     * @param retired callback on entity retirement, or {@code null}
     * @return the task handle, or {@link Task#CANCELLED} if the entity is already retired
     */
    @NotNull Task at(@NotNull Entity entity, @NotNull Runnable task, @Nullable Runnable retired);

    /**
     * Schedules a task on the region currently owning an entity after a delay.
     *
     * @param entity the target entity
     * @param task   the operation to execute
     * @param delay  non-negative delay in ticks
     * @return the task handle, or {@link Task#CANCELLED} if the entity is already retired
     */
    default @NotNull Task atLater(@NotNull Entity entity, @NotNull Runnable task, long delay) {
        return this.atLater(entity, task, null, delay);
    }

    /**
     * Schedules a delayed entity task with an optional retirement callback.
     *
     * @param entity  the target entity
     * @param task    the operation to execute
     * @param retired optional callback on entity retirement
     * @param delay   non-negative delay in ticks
     * @return the task handle, or {@link Task#CANCELLED} if the entity is already retired
     */
    @NotNull Task atLater(@NotNull Entity entity, @NotNull Runnable task,
                          @Nullable Runnable retired, long delay);

    /**
     * Schedules a repeating task on the region currently owning an entity. The task receives
     * its own handle, so it can cancel itself.
     *
     * @param entity the target entity
     * @param task   the operation to execute
     * @param delay  non-negative initial delay in ticks
     * @param period strictly positive period in ticks
     * @return the task handle, or {@link Task#CANCELLED} if the entity is already retired
     */
    default @NotNull Task atTimer(@NotNull Entity entity, @NotNull Consumer<Task> task,
                                  long delay, long period) {
        return this.atTimer(entity, task, null, delay, period);
    }

    /**
     * Schedules a repeating entity task with an optional retirement callback.
     * The task is cancelled when the entity is found retired.
     *
     * @param entity  the target entity
     * @param task    the operation to execute
     * @param retired optional callback when the entity becomes unavailable
     * @param delay   non-negative initial delay in ticks
     * @param period  strictly positive period in ticks
     * @return the task handle, or {@link Task#CANCELLED} if the entity is already retired
     */
    @NotNull Task atTimer(@NotNull Entity entity, @NotNull Consumer<Task> task,
                          @Nullable Runnable retired, long delay, long period);

    /**
     * Runs the task immediately if the current thread already owns the entity,
     * otherwise schedules it like {@link #at(Entity, Runnable, Runnable)}.
     *
     * @param entity  the target entity
     * @param task    the operation to execute
     * @param retired optional callback if the entity is retired before a scheduled run
     * @return {@code true} if the task ran inline or was scheduled, {@code false} if the entity
     * was already retired (neither {@code task} nor {@code retired} is invoked)
     */
    boolean execute(@NotNull Entity entity, @NotNull Runnable task, @Nullable Runnable retired);

    /**
     * Same as {@link #execute(Entity, Runnable, Runnable)} without a retirement callback.
     *
     * @param entity the target entity
     * @param task   the operation to execute
     * @return {@code true} if the task ran inline or was scheduled, {@code false} if the
     * entity was already retired
     */
    default boolean execute(@NotNull Entity entity, @NotNull Runnable task) {
        return this.execute(entity, task, null);
    }

    /**
     * Cancels the tasks of the owning plugin. Tasks that are currently executing
     * are not interrupted.
     *
     * <p>On Bukkit/Spigot this cancels every task of the plugin, including tasks that were
     * scheduled directly through the Bukkit scheduler rather than through this class.
     *
     * <p>On Paper/Folia only global and asynchronous tasks are cancelled, because the region
     * and entity schedulers cannot cancel by plugin. Cancel those through their {@link Task}
     * handles.
     */
    void cancelAll();
}
