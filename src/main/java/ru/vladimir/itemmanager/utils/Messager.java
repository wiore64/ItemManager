package ru.vladimir.itemmanager.utils;

import java.util.Map;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class Messager {
    private Messager() {}

    public static void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sendMessage(sender, message, Map.of());
    }

    public static void sendMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull Map<String, String> params) {
        for (final var entry : params.entrySet()) {
            message = message.replaceAll(entry.getKey(), entry.getValue());
        }

        // Turn into component, and do not forget about colors via MiniMessage or so.
    }
}
