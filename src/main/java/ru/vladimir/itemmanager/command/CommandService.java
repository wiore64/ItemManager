package ru.vladimir.itemmanager.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.vladimir.itemmanager.utils.Logger;

final class CommandService {
    private static final Map<String, SubCommandWrapper> SUB_COMMAND_REGISTRY = new ConcurrentHashMap<>();

    static {
        // Here we register the commands.
    }

    static void registerSubCommand(String alias, SubCommandWrapper wrapper) {
        final boolean isAdded = SUB_COMMAND_REGISTRY.putIfAbsent(alias, wrapper) == null;
        if (isAdded) return;

        Logger.warn(CommandService.class, "Failed to add a command for alias (%s)".formatted(alias));
    }

    static void registerSubCommand(Iterable<String> aliases, SubCommandWrapper wrapper) {
        for (final String alias : aliases) {
            registerSubCommand(alias, wrapper);
        }
    }
}
