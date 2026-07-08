package ru.yolta.customitemmanager.command;

import java.util.Set;

import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

record SubCommandWrapper(
    @NotNull SubCommand command,
    @NotNull Set<String> aliases,
    @NotNull Permission permission
) {}
