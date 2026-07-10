package ru.yolta.customitemmanager.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import ru.yolta.customitemmanager.CustomItemManager;

public final class Logger {

    private static final ComponentLogger logger = ComponentLogger.logger(CustomItemManager.PLUGIN_NAME);
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static Level minLevel = Level.INFO;

    private Logger() {}

    public static void setLevel(@NotNull Level level) {
        minLevel = level;
    }
    
    public static void debug(Object origin, @NotNull String message, @NotNull Object @NotNull ... args) {
        log(Level.DEBUG, origin, message, args);
    }

    public static void info(Object origin, @NotNull String message, @NotNull Object @NotNull ... args) {
        log(Level.INFO, origin, message, args);
    }
    
    public static void warn(Object origin, @NotNull String message, @NotNull Object @NotNull ... args) {
        log(Level.WARN, origin, message, args);
    }

    public static void warn(Object origin, @NotNull String message, @NotNull Throwable throwable) {
        log(Level.WARN, origin, message, throwable);
    }

    public static void error(Object origin, @NotNull String message, @NotNull Object @NotNull ... args) {
        log(Level.ERROR, origin, message, args);
    }

    public static void error(Object origin, @NotNull String message, @NotNull Throwable throwable) {
        log(Level.ERROR, origin, message, throwable);
    }

    private static void log(Level level, Object origin, String message, Throwable throwable) {
        if (level.intLevel() > minLevel.intLevel()) return;

        final String originName = getOriginName(origin);
        final Component formattedMessage = MINI_MESSAGE.deserialize("[" + originName + "]" + " - " + message);

        switch (level.intLevel()) {
            case 200 -> logger.error(formattedMessage, throwable);
            case 300 -> logger.warn(formattedMessage, throwable);
            default -> throw new IllegalArgumentException("Invalid log level with throwable: " + level);
        }
    }

    private static void log(Level level, Object origin, String message, Object... args) {
        if (level.intLevel() > minLevel.intLevel()) return;

        final String originName = getOriginName(origin);
        final Component formattedMessage = MINI_MESSAGE.deserialize("[" + originName + "]" + " - " + message);

        switch (level.intLevel()) {
            case 200 -> logger.error(formattedMessage, args);
            case 300 -> logger.warn(formattedMessage, args);
            case 400 -> logger.info(formattedMessage, args);
            case 500 -> logger.info(formattedMessage, args); // we use info, since debugging is very painful to configure
            default -> throw new IllegalArgumentException("Invalid log level: " + level);
        }
    }

    private static String getOriginName(Object o) {
        return switch (o) {
            case null -> "Unknown";
            case String ignored -> o.toString();
            case Class<?> clazz -> clazz.getSimpleName();
            default -> o.getClass().getSimpleName();
        };
    }
}
