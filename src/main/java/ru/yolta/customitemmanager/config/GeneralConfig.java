package ru.yolta.customitemmanager.config;

import org.jetbrains.annotations.NotNull;
import ru.yolta.customitemmanager.utils.Logger;

public record GeneralConfig(
    @NotNull Logger.LogLevel loggingLevel
) {}
