package ru.yolta.customitemmanager.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class Logger {

    private static Logger instance;
    private ComponentLogger logger;
    private LogLevel minLevel;

    public enum LogLevel {
        DEBUG(0),
        INFO(1),
        WARNING(2),
        ERROR(3);

        private final int intValue;

        LogLevel(int intValue) {
            this.intValue = intValue;
        }

        public static LogLevel getLogLevel(String name) {
            try {
                return LogLevel.valueOf(name.strip().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    private Logger() {}

    public static Logger getInstance() {
        if (instance == null)
            throw new IllegalStateException("Accessed before it was initialized.");
        return instance;
    }

    public static void init(@NotNull ComponentLogger logger) {
        if (instance != null) {
            instance.warn(instance, "Already initialized.");
            return;
        }

        instance = new Logger();

        instance.logger = logger;
        instance.minLevel = LogLevel.INFO;
    }

    public static void destroy() {
        if (instance == null) {
            instance.warn(instance, "Not initialized.");
            return;
        }

        instance = null;
    }

    public void setLevel(@NotNull LogLevel l) {
        minLevel = l;
    }
    
    public void debug(Object origin, @NotNull String message) {
        log(LogLevel.DEBUG, origin, message, null);
    }

    public void info(Object origin, @NotNull String message) {
        log(LogLevel.INFO, origin, message, null);
    }
    
    public void warn(Object origin, @NotNull String message) {
        log(LogLevel.WARNING, origin, message, null);
    }

    public void warn(Object origin, @NotNull String message, @NotNull Throwable t) {
        log(LogLevel.WARNING, origin, message, t);
    }

    public void error(Object origin, @NotNull String message) {
        log(LogLevel.ERROR, origin, message, null);
    }

    public void error(Object origin, @NotNull String message, @NotNull Throwable t) {
        log(LogLevel.ERROR, origin, message, t);
    }

    private void log(LogLevel l, Object o, String m, Throwable t) {
        if (l.intValue < minLevel.intValue) return;

        final String oN = getOriginName(o);

        final Component cM = Component.text("(%s) %s".formatted(oN, m));

        switch (l) {
            case DEBUG, INFO -> logger.info(cM);

            case WARNING -> {
                if (t == null)
                    logger.warn(cM);
                else
                    logger.warn(cM, t);
            }

            case ERROR -> {
                if (t == null)
                    logger.error(cM);
                else
                    logger.error(cM, t);
            }
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
