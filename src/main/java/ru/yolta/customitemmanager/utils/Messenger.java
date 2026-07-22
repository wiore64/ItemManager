package ru.yolta.customitemmanager.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class Messenger {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static String prefix = "";

    private Messenger() {
    }

    public static void setPrefix(@NotNull String prefix) {
        Messenger.prefix = prefix;
    }

    public static void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sendMessage(sender, message, Map.of());
    }

    public static void sendMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull Map<String, Object> args) {
        for (final var entry : args.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }

        if (prefix.isEmpty())
            sender.sendMessage(MINI_MESSAGE.deserialize(message));
        else
            sender.sendMessage(MINI_MESSAGE.deserialize(prefix + " " + message));
    }
}
