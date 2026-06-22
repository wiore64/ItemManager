package ru.vladimir.itemmanager.command.list;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.vladimir.itemmanager.command.SubCommand;

public class ListItems implements SubCommand {

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
