package ru.yolta.customitemmanager.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SubCommand {
    void onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args);
    @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args);
}
