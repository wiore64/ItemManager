package ru.vladimir.itemmanager.config;

import org.jetbrains.annotations.NotNull;

public record Messages(
    @NotNull String noPermission,
    @NotNull String description,
    @NotNull String invalidSubCommand
) {}
