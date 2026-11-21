package com.github.groundbreakingmc.mylib.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.github.groundbreakingmc.mylib.scheduler.Checks.*;

/**
 * Bukkit/Spigot implementation of {@link PluginScheduler}.
 *
 * <p>All global, location and entity tasks run on the primary server thread.
 * Async delays are rounded up to 50 ms server ticks, so they are not wall-clock
 * accurate when the server lags.
 */
public final class BukkitPluginScheduler implements PluginScheduler {

    private static final long NANOS_PER_TICK = 50_000_000L;

    private final Plugin plugin;

    /**
     * @param plugin the plugin that owns the scheduled tasks
     */
    public BukkitPluginScheduler(@NotNull Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task async(@NotNull Runnable task) {
        Objects.requireNonNull(task, "task");
        return new BukkitTaskHandle(Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task asyncLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        Objects.requireNonNull(task, "task");
        long ticks = toTicks(delay, unit);
        if (ticks == 0L) return this.async(task);
        return new BukkitTaskHandle(Bukkit.getScheduler()
                .runTaskLaterAsynchronously(this.plugin, task, ticks));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task asyncTimer(@NotNull Consumer<Task> task, long delay, long period,
                                    @NotNull TimeUnit unit) {
        Objects.requireNonNull(task, "task");
        checkPeriod(period);
        // period > 0 always rounds up to at least one tick
        return this.timer(task, toTicks(delay, unit), toTicks(period, unit), true);
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task global(@NotNull Runnable task) {
        Objects.requireNonNull(task, "task");
        return new BukkitTaskHandle(Bukkit.getScheduler().runTask(this.plugin, task));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task globalLater(@NotNull Runnable task, long delay) {
        Objects.requireNonNull(task, "task");
        checkDelay(delay);
        return new BukkitTaskHandle(Bukkit.getScheduler().runTaskLater(this.plugin, task, delay));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task globalTimer(@NotNull Consumer<Task> task, long delay, long period) {
        Objects.requireNonNull(task, "task");
        checkTimer(delay, period);
        return this.timer(task, delay, period, false);
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task at(@NotNull Location location, @NotNull Runnable task) {
        Objects.requireNonNull(location, "location");
        return this.global(task);
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task atLater(@NotNull Location location, @NotNull Runnable task, long delay) {
        Objects.requireNonNull(location, "location");
        return this.globalLater(task, delay);
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task atTimer(@NotNull Location location, @NotNull Consumer<Task> task,
                                 long delay, long period) {
        Objects.requireNonNull(location, "location");
        return this.globalTimer(task, delay, period);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(@NotNull Location location, @NotNull Runnable task) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(task, "task");
        if (Bukkit.isPrimaryThread()) task.run();
        else this.global(task);
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task at(@NotNull Entity entity, @NotNull Runnable task,
                            @Nullable Runnable retired) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        if (!entity.isValid()) return Task.CANCELLED;
        return new BukkitTaskHandle(Bukkit.getScheduler().runTask(this.plugin,
                () -> this.runIfValid(entity, task, retired)));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task atLater(@NotNull Entity entity, @NotNull Runnable task,
                                 @Nullable Runnable retired, long delay) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        checkDelay(delay);
        if (!entity.isValid()) return Task.CANCELLED;
        return new BukkitTaskHandle(Bukkit.getScheduler().runTaskLater(this.plugin,
                () -> this.runIfValid(entity, task, retired), delay));
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Task atTimer(@NotNull Entity entity, @NotNull Consumer<Task> task,
                                 @Nullable Runnable retired, long delay, long period) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        checkTimer(delay, period);
        if (!entity.isValid()) return Task.CANCELLED;
        return this.timer(self -> {
            if (!entity.isValid()) {
                self.cancel();
                if (retired != null) retired.run();
                return;
            }
            task.accept(self);
        }, delay, period, false);
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(@NotNull Entity entity, @NotNull Runnable task,
                           @Nullable Runnable retired) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        if (!entity.isValid()) return false;
        if (Bukkit.isPrimaryThread()) {
            task.run();
            return true;
        }
        return this.at(entity, task, retired) != Task.CANCELLED;
    }

    /** {@inheritDoc} */
    @Override
    public void cancelAll() {
        Bukkit.getScheduler().cancelTasks(this.plugin);
    }

    /**
     * Schedules a repeating task and binds the resulting Bukkit task to a {@link TimerHandle}.
     *
     * @param body   the callback that receives the task handle on every run
     * @param delay  initial delay in ticks
     * @param period period in ticks
     * @param async  whether the task runs off the primary thread
     * @return the handle of the repeating task
     */
    private @NotNull Task timer(@NotNull Consumer<Task> body, long delay, long period, boolean async) {
        final TimerHandle handle = new TimerHandle(body);
        final BukkitScheduler scheduler = Bukkit.getScheduler();
        final BukkitTask bukkitTask = async
                ? scheduler.runTaskTimerAsynchronously(this.plugin, handle, delay, period)
                : scheduler.runTaskTimer(this.plugin, handle, delay, period);
        handle.bind(bukkitTask);
        return handle;
    }

    /**
     * Runs {@code task} if the entity is still valid, otherwise runs {@code retired}.
     *
     * @param entity  the target entity
     * @param task    the operation to execute
     * @param retired optional callback if the entity is no longer valid
     */
    private void runIfValid(@NotNull Entity entity, @NotNull Runnable task,
                            @Nullable Runnable retired) {
        if (entity.isValid()) task.run();
        else if (retired != null) retired.run();
    }

    /**
     * Converts a duration to a whole number of 50 ms ticks, rounding up.
     *
     * @param amount the non-negative duration
     * @param unit   the unit of {@code amount}
     * @return the duration in ticks, saturating for very large values
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    private static long toTicks(long amount, @NotNull TimeUnit unit) {
        Objects.requireNonNull(unit, "unit");
        checkDelay(amount);
        long nanos = unit.toNanos(amount); // saturates at Long.MAX_VALUE instead of overflowing
        long ticks = nanos / NANOS_PER_TICK;
        return nanos % NANOS_PER_TICK == 0L ? ticks : ticks + 1L;
    }

    /**
     * Wraps a Bukkit task without exposing Bukkit's task type to callers.
     *
     * @param delegate the wrapped Bukkit task
     */
    private record BukkitTaskHandle(@NotNull BukkitTask delegate) implements Task {

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

    /**
     * Handle for repeating tasks. It is both the {@link Runnable} given to Bukkit and the
     * {@link Task} given to the body, so the body can cancel itself even if the first run
     * happens before {@link #bind(BukkitTask)} (possible for async timers).
     */
    private static final class TimerHandle implements Task, Runnable {

        private final Consumer<Task> body;
        private volatile BukkitTask delegate;
        private volatile boolean cancelled;

        private TimerHandle(@NotNull Consumer<Task> body) {
            this.body = body;
        }

        /**
         * Attaches the Bukkit task. If the handle was cancelled before this call,
         * the task is cancelled immediately.
         *
         * @param bukkitTask the scheduled Bukkit task
         */
        private void bind(@NotNull BukkitTask bukkitTask) {
            this.delegate = bukkitTask;
            if (this.cancelled) bukkitTask.cancel();
        }

        /** Runs the body unless the handle was cancelled. */
        @Override
        public void run() {
            if (!this.cancelled) this.body.accept(this);
        }

        /** {@inheritDoc} */
        @Override
        public void cancel() {
            this.cancelled = true;
            final BukkitTask bukkitTask = this.delegate;
            if (bukkitTask != null) bukkitTask.cancel();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCancelled() {
            if (this.cancelled) return true;
            final BukkitTask bukkitTask = this.delegate;
            return bukkitTask != null && bukkitTask.isCancelled();
        }
    }
}
