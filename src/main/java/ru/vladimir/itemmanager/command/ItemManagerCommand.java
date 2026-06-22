package ru.vladimir.itemmanager.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemManagerCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        // With the help of the service, dispatch to the appropriate sub command.
        // Otherwise, handle it yourself. For example, a description message.
        
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        // With the help of the service, dispatch to the appropriate sub command.
        // Otherwise, return either List.of() or null, depending on the case.
        
        return List.of();
    }
}
