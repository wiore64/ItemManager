package ru.yolta.customitemmanager.utils;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.yolta.customitemmanager.CustomItemManager;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Future {
    private static CustomItemManager plugin;

    private Future() {}

    public static void setPlugin(@Nullable CustomItemManager plugin) {
        Future.plugin = plugin;
    }

    public static <T> CompletableFuture<T> runAsyncTask(@NotNull Supplier<T> task) {
        ensurePluginEnabled();

        final CompletableFuture<T> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                future.complete(task.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    public static <T> void runAsyncTask(@NotNull Supplier<CompletableFuture<T>> asyncTask, @NotNull Consumer<T> callback) {
        ensurePluginEnabled();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final T result = asyncTask.get().join();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    ensurePluginEnabled();

                    callback.accept(result);
                });
            } catch (Throwable t) {
                Logger.error(Future.class, "Failed to execute task.", t);
            }
        });
    }

    private static void ensurePluginEnabled() {
        if (plugin == null || !plugin.isEnabled()) throw new IllegalStateException("Attempted to execute task after plugin was disabled.");
    }
}
