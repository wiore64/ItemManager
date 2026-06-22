package ru.vladimir.itemmanager.config;

import java.util.logging.Level;

import org.jetbrains.annotations.NotNull;

public record Config(
    @NotNull Level loggingLevel
) {}
