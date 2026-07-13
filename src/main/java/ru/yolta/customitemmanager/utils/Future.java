package ru.yolta.customitemmanager.utils;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import ru.yolta.customitemmanager.CustomItemManager;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Future {
    private static CustomItemManager plugin;

    private Future() {}

    public static void setPlugin(@NotNull CustomItemManager plugin) {
        Future.plugin = plugin;
    }

    public static <T> CompletableFuture<T> runAsyncTask(Supplier<T> task) {
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

    public static <T> void runAsyncTask(Supplier<CompletableFuture<T>> asyncTask, Consumer<T> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final T result = asyncTask.get().join();
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(result));
            } catch (Throwable t) {
                Logger.error(Future.class, "Failed to execute task.", t);
            }
        });
    }
}
