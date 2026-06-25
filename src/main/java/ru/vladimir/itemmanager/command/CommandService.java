package ru.vladimir.itemmanager.command;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import ru.vladimir.itemmanager.command.list.AddItem;
import ru.vladimir.itemmanager.command.list.GiveItem;
import ru.vladimir.itemmanager.command.list.ListItems;
import ru.vladimir.itemmanager.command.list.ReloadConfig;
import ru.vladimir.itemmanager.command.list.RemoveItem;
import ru.vladimir.itemmanager.utils.Logger;

public final class CommandService {
    private static CommandService instance;
    private Map<String, SubCommandWrapper> subCommandRegistry;

    private CommandService() {}

    public static @NotNull CommandService getInstance() {
        if (instance == null)
            throw new IllegalStateException("Attempted to get instance before it was initialized.");
        return instance;
    }

    public static void init() {
        if (instance != null) {
            Logger.warn(instance, "Attempted to initialize an instance while it is already initialized.");
            return;
        }

        instance = new CommandService();

        instance.subCommandRegistry = new ConcurrentHashMap<>();
        instance.registerSubCommands();

        Logger.info(instance, "Successfully initialized.");
    }

    public static void destroy() {
        if (instance == null) {
            Logger.warn(CommandService.class, "Attempted to destroy an instance while there is none.");
            return;
        }

        instance = null;

        Logger.info(CommandService.class, "Successfully destroyed.");
    }

    private void registerSubCommands() {
        final var addWrapper = new SubCommandWrapper(new AddItem(), Set.of("add"), new Permission("itemmanager.command.add"));
        registerSubCommand(addWrapper.aliases(), addWrapper);

        final var removeWrapper = new SubCommandWrapper(new RemoveItem(), Set.of("remove"), new Permission("itemmanager.command.remove"));
        registerSubCommand(removeWrapper.aliases(), removeWrapper);

        final var giveWrapper = new SubCommandWrapper(new GiveItem(), Set.of("give"), new Permission("itemmanager.command.give"));
        registerSubCommand(giveWrapper.aliases(), giveWrapper);

        final var listWrapper = new SubCommandWrapper(new ListItems(), Set.of("list"), new Permission("itemmanager.command.list"));
        registerSubCommand(listWrapper.aliases(), listWrapper);

        final var reloadWrapper = new SubCommandWrapper(new ReloadConfig(), Set.of("reload"), new Permission("itemmanager.command.reload"));
        registerSubCommand(reloadWrapper.aliases(), reloadWrapper);
    }

    private void registerSubCommand(Iterable<String> aliases, SubCommandWrapper wrapper) {
        for (final String alias : aliases) {
            final boolean isAdded = subCommandRegistry.putIfAbsent(alias, wrapper) == null;
            if (isAdded)
                Logger.debug(this, "Registered subcommand with alias: %s.".formatted(alias));
            else
                Logger.warn(this, "Attempted to register subcommand with alias: %s, but it is already registered.".formatted(alias));
        }
    }

    Optional<SubCommandWrapper> getWrapperForAlias(String alias) {
        return Optional.ofNullable(subCommandRegistry.get(alias));
    }
}
