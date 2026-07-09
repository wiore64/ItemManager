package ru.yolta.customitemmanager.utils;

import java.util.Map;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Messenger {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static String prefix = "";

    private Messenger() {}

    public static void setPrefix(@NotNull String prefix) {
        Messenger.prefix = prefix;
    }

    public static void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sendMessage(sender, message, Map.of());
    }

    public static void sendMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull Map<String, String> args) {
        for (final var entry : args.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        if (prefix.isEmpty())
            sender.sendMessage(MINI_MESSAGE.deserialize(message));
        else
            sender.sendMessage(MINI_MESSAGE.deserialize(prefix + " " + message));
    }
}
