package ru.yolta.customitemmanager.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.yolta.customitemmanager.config.MessageConfig;
import ru.yolta.customitemmanager.utils.Messenger;

public final class CustomItemManagerCommand implements TabExecutor {

    private static final String PRIMARY_PERMISSION = "customitemmanager.command";
    private final CommandService service;
    private final MessageConfig messages;

    public CustomItemManagerCommand(@NotNull CommandService service, @NotNull MessageConfig messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(PRIMARY_PERMISSION)) {
            Messenger.sendMessage(sender, messages.noPermission());
            return true;
        }
        
        if (args.length == 0) {
            Messenger.sendMessage(sender, messages.pluginDescription());
            return true;
        }

        final var optionalWrapper = service.getWrapperForAlias(args[0]);

        if (optionalWrapper.isEmpty()) {
            Messenger.sendMessage(sender, messages.invalidCommand());
            return true;
        }

        final var wrapper = optionalWrapper.get();

        if (!sender.hasPermission(wrapper.permission())) {
            Messenger.sendMessage(sender, messages.noPermission());
            return true;
        }

        wrapper.command().onCommand(sender, args);
        
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(PRIMARY_PERMISSION))
            return List.of();

        if (args.length == 1)
            return List.copyOf(service.getAliasesFor(sender));

        final var optionalWrapper = service.getWrapperForAlias(args[0]);

        if (optionalWrapper.isEmpty())
            return List.of();

        final var wrapper = optionalWrapper.get();

        if (!sender.hasPermission(wrapper.permission()))
            return List.of();
        
        return wrapper.command().onTabComplete(sender, args);
    }
}
