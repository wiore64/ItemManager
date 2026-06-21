package ru.vladimir.itemmanager.utils;

import java.util.logging.Level;

public final class Logger {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("ItemManager");
    private static Level minLevel = Level.ALL;

    private Logger() {}

    public static void setLevel(Level l) {
        minLevel = l;
    }
    
    public static void debug(Object origin, String message) {
        log(Level.FINE, origin, message, null);
    }

    public static void info(Object origin, String message) {
        log(Level.INFO, origin, message, null);
    }
    
    public static void warn(Object origin, String message) {
        log(Level.WARNING, origin, message, null);
    }

    public static void warn(Object origin, String message, Throwable t) {
        log(Level.WARNING, origin, message, t);
    }

    public static void error(Object origin, String message) {
        log(Level.SEVERE, origin, message, null);
    }

    public static void error(Object origin, String message, Throwable t) {
        log(Level.SEVERE, origin, message, t);
    }

    private static void log(Level l, Object o, String m, Throwable t) {
        if (l.intValue() < minLevel.intValue()) return;

        final String oN = getOriginName(o);

        if (t == null)
            LOGGER.log(l, "(%s) %s".formatted(oN, m));
        else
            LOGGER.log(l, "(%s) %s".formatted(oN, m), t);
    }

    private static String getOriginName(Object o) {
        if (o == null) return "Unknown";
        if (o instanceof String) return o.toString();
        return o.getClass().getSimpleName();
    }
}