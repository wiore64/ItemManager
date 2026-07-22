package ru.yolta.customitemmanager.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.command.list.*;
import ru.yolta.customitemmanager.config.MessageConfig;
import ru.yolta.customitemmanager.utils.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CommandService {
    private final Map<String, SubCommandWrapper> subCommandRegistry;

    public CommandService(@NotNull CustomItemManager plugin, @NotNull MessageConfig messages) {
        Logger.debug(this, "Initializing...");

        this.subCommandRegistry = new ConcurrentHashMap<>();

        registerSubCommands(plugin, messages);

        Logger.debug(this, "Initialized successfully.");
    }

    private void registerSubCommands(CustomItemManager plugin, MessageConfig messages) {
        final var addWrapper = new SubCommandWrapper(new AddItem(messages.sharedCmd(), messages.addItemCmd()), AddItem.getAliases(), AddItem.getPermission());
        registerSubCommand(addWrapper.aliases(), addWrapper);

        final var removeWrapper = new SubCommandWrapper(new RemoveItem(messages.sharedCmd(), messages.removeItemCmd()), RemoveItem.getAliases(), RemoveItem.getPermission());
        registerSubCommand(removeWrapper.aliases(), removeWrapper);

        final var giveWrapper = new SubCommandWrapper(new GiveItem(messages.sharedCmd(), messages.giveItemCmd()), GiveItem.getAliases(), GiveItem.getPermission());
        registerSubCommand(giveWrapper.aliases(), giveWrapper);

        final var reloadWrapper = new SubCommandWrapper(new ReloadPlugin(messages.sharedCmd(), messages.reloadPluginCmd()), ReloadPlugin.getAliases(), ReloadPlugin.getPermission());
        registerSubCommand(reloadWrapper.aliases(), reloadWrapper);

        final var helpWrapper = new SubCommandWrapper(new PluginHelp(messages.sharedCmd(), messages.helpPluginCmd()), PluginHelp.getAliases(), PluginHelp.getPermission());
        registerSubCommand(helpWrapper.aliases(), helpWrapper);

        final var itemWrapper = new SubCommandWrapper(new ItemCommand(plugin, messages.sharedCmd(), messages.manageItemCmd()), ItemCommand.ALIASES, ItemCommand.PERMISSION);
        registerSubCommand(itemWrapper.aliases(), itemWrapper);
    }

    private void registerSubCommand(Set<String> aliases, SubCommandWrapper wrapper) {
        for (final String alias : aliases) {
            if (subCommandRegistry.containsKey(alias)) {
                Logger.warn(this, "Failed to add alias '{}': Already registered.", alias);
                continue;
            }

            subCommandRegistry.put(alias, wrapper);
        }
    }

    Optional<SubCommandWrapper> getWrapperForAlias(String alias) {
        return Optional.ofNullable(subCommandRegistry.get(alias));
    }

    Set<String> getAliasesFor(CommandSender sender) {
        final Set<String> aliases = new HashSet<>();

        for (final var entry : subCommandRegistry.entrySet()) {
            if (!sender.hasPermission(entry.getValue().permission())) continue;

            aliases.add(entry.getKey());
        }

        return aliases;
    }
}
