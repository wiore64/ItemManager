package ru.vladimir.itemmanager.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.vladimir.itemmanager.config.ConfigManager;
import ru.vladimir.itemmanager.utils.Messager;

public class ItemManagerCommand implements TabExecutor {
    private static final String PRIMARY_PERMISSION = "itemmanager.command";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(PRIMARY_PERMISSION)) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().noPermission());
            return true;
        }
        
        if (args.length == 0) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().description());
            return true;
        }

        final var optionalWrapper = CommandService.getInstance().getWrapperForAlias(args[0]);

        if (optionalWrapper.isEmpty()) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().invalidSubCommand());
            return true;
        }

        final var wrapper = optionalWrapper.get();

        if (!sender.hasPermission(wrapper.permission())) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().noPermission());
            return true;
        }

        wrapper.command().onCommand(sender, args);
        
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(PRIMARY_PERMISSION))
            return List.of();

        if (args.length == 0)
            return List.of();

        final var optionalWrapper = CommandService.getInstance().getWrapperForAlias(args[0]);

        if (optionalWrapper.isEmpty())
            return List.of();

        final var wrapper = optionalWrapper.get();

        if (!sender.hasPermission(wrapper.permission()))
            return List.of();
        
        return wrapper.command().onTabComplete(sender, args);
    }
}
