package ru.vladimir.itemmanager.utils;

import java.util.Map;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Messager {
    private static final MiniMessage MINI_MESSAGE_PARSER = MiniMessage.miniMessage();

    private Messager() {}

    public static void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sendMessage(sender, message, Map.of());
    }

    public static void sendMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull Map<String, String> params) {
        for (final var entry : params.entrySet()) {
            message = message.replaceAll("{" + entry.getKey() + "}", entry.getValue());
        }

        sender.sendMessage(MINI_MESSAGE_PARSER.deserialize(message));
    }
}
