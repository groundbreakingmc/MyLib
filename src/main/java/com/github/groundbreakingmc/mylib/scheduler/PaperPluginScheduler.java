package com.github.groundbreakingmc.mylib.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.github.groundbreakingmc.mylib.scheduler.Checks.*;

/**
 * Paper/Folia implementation of {@link PluginScheduler}.
 *
 * <p>Global tasks run on the global region, location tasks on their owning region,
 * and entity tasks follow the entity across region boundaries. Asynchronous delays
 * are measured in real time, not server ticks.
 *
 * <p>Folia rejects tick delays below one, so a delay of zero is raised to one tick,
 * which matches the "next available tick" contract of {@link PluginScheduler}.
 */
public final class PaperPluginScheduler implements PluginScheduler {

    private final Plugin plugin;

    /**
     * @param plugin the plugin that owns the scheduled tasks
     */
    public PaperPluginScheduler(@NotNull Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task async(@NotNull Runnable task) {
        Objects.requireNonNull(task, "task");
        return new PaperTaskHandle(Bukkit.getAsyncScheduler().runNow(this.plugin,
                scheduled -> task.run()));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task asyncLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        Objects.requireNonNull(task, "task");
        Objects.requireNonNull(unit, "unit");
        checkDelay(delay);
        if (delay == 0L) return this.async(task);
        return new PaperTaskHandle(Bukkit.getAsyncScheduler().runDelayed(this.plugin,
                scheduled -> task.run(), delay, unit));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task asyncTimer(@NotNull Consumer<Task> task, long delay, long period,
                                    @NotNull TimeUnit unit) {
        Objects.requireNonNull(task, "task");
        Objects.requireNonNull(unit, "unit");
        checkTimer(delay, period);
        return new PaperTaskHandle(Bukkit.getAsyncScheduler().runAtFixedRate(this.plugin,
                new TimerBody(task), delay, period, unit));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task global(@NotNull Runnable task) {
        Objects.requireNonNull(task, "task");
        return new PaperTaskHandle(Bukkit.getGlobalRegionScheduler().run(this.plugin,
                scheduled -> task.run()));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task globalLater(@NotNull Runnable task, long delay) {
        Objects.requireNonNull(task, "task");
        return new PaperTaskHandle(Bukkit.getGlobalRegionScheduler().runDelayed(this.plugin,
                scheduled -> task.run(), foliaTicks(delay)));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task globalTimer(@NotNull Consumer<Task> task, long delay, long period) {
        Objects.requireNonNull(task, "task");
        checkPeriod(period);
        return new PaperTaskHandle(Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin,
                new TimerBody(task), foliaTicks(delay), period));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task at(@NotNull Location location, @NotNull Runnable task) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(task, "task");
        return new PaperTaskHandle(Bukkit.getRegionScheduler().run(this.plugin,
                location, scheduled -> task.run()));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task atLater(@NotNull Location location, @NotNull Runnable task, long delay) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(task, "task");
        return new PaperTaskHandle(Bukkit.getRegionScheduler().runDelayed(this.plugin,
                location, scheduled -> task.run(), foliaTicks(delay)));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task atTimer(@NotNull Location location, @NotNull Consumer<Task> task,
                                 long delay, long period) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(task, "task");
        checkPeriod(period);
        return new PaperTaskHandle(Bukkit.getRegionScheduler().runAtFixedRate(this.plugin,
                location, new TimerBody(task), foliaTicks(delay), period));
    }

    /** {@inheritDoc} */
    @Override
    public void execute(@NotNull Location location, @NotNull Runnable task) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(task, "task");
        if (Bukkit.isOwnedByCurrentRegion(location)) task.run();
        else this.at(location, task);
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task at(@NotNull Entity entity, @NotNull Runnable task,
                            @Nullable Runnable retired) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        return wrap(entity.getScheduler().run(this.plugin, scheduled -> task.run(), retired));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task atLater(@NotNull Entity entity, @NotNull Runnable task,
                                 @Nullable Runnable retired, long delay) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        return wrap(entity.getScheduler().runDelayed(this.plugin,
                scheduled -> task.run(), retired, foliaTicks(delay)));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task atTimer(@NotNull Entity entity, @NotNull Consumer<Task> task,
                                 @Nullable Runnable retired, long delay, long period) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        checkPeriod(period);
        return wrap(entity.getScheduler().runAtFixedRate(this.plugin,
                new TimerBody(task), retired, foliaTicks(delay), period));
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(@NotNull Entity entity, @NotNull Runnable task,
                           @Nullable Runnable retired) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        if (Bukkit.isOwnedByCurrentRegion(entity)) {
            if (!entity.isValid()) return false;
            task.run();
            return true;
        }
        return this.at(entity, task, retired) != Task.CANCELLED;
    }

    /** {@inheritDoc} */
    @Override
    public void cancelAll() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(this.plugin);
        Bukkit.getAsyncScheduler().cancelTasks(this.plugin);
    }

    /**
     * Validates a tick delay and adapts it to Folia, which requires at least one tick.
     *
     * @param delay the requested delay in ticks
     * @return {@code delay}, or {@code 1} if it was zero
     * @throws IllegalArgumentException if {@code delay} is negative
     */
    private static long foliaTicks(long delay) {
        checkDelay(delay);
        return Math.max(1L, delay);
    }

    /**
     * The entity scheduler returns {@code null} when the entity is already retired.
     *
     * @param scheduledTask the scheduler result, possibly {@code null}
     * @return a handle for the task, or {@link Task#CANCELLED} if it was not scheduled
     */
    private static @NotNull Task wrap(@Nullable ScheduledTask scheduledTask) {
        return scheduledTask == null ? Task.CANCELLED : new PaperTaskHandle(scheduledTask);
    }

    /**
     * Adapts a {@link Consumer} of {@link Task} to Paper's {@link ScheduledTask} callback.
     * It holds no state: each run wraps the given {@link ScheduledTask} in a new
     * {@link PaperTaskHandle}. Handles of the same task are equal.
     *
     * @param body the user callback that receives the task handle
     */
    private record TimerBody(@NotNull Consumer<Task> body) implements Consumer<ScheduledTask> {

        /** {@inheritDoc} */
        @Override
        public void accept(ScheduledTask scheduled) {
            this.body.accept(new PaperTaskHandle(scheduled));
        }
    }

    /**
     * Wraps a Paper/Folia task without exposing its scheduler-specific type.
     *
     * @param delegate the wrapped scheduler task
     */
    private record PaperTaskHandle(@NotNull ScheduledTask delegate) implements Task {

        /** {@inheritDoc} */
        @Override
        public void cancel() {
            this.delegate.cancel();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCancelled() {
            return this.delegate.isCancelled();
        }
    }
}
