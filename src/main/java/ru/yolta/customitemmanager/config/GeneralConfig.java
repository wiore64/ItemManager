package ru.yolta.customitemmanager.config;

import org.apache.logging.log4j.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import ru.yolta.customitemmanager.utils.Logger;

import java.io.File;
import java.util.Locale;

public record GeneralConfig(@NotNull Level loggingLevel) {
    private static boolean shouldSaveConfig = false;

    static @NotNull GeneralConfig parseGeneralConfig(@NotNull ConfigProvider manager, @NotNull File file, @NotNull FileConfiguration fileConfig) {
        final int configVersion = fileConfig.getInt("config-version", -1);

        if (configVersion == -1) {
            Logger.info(GeneralConfig.class, "Your config has been updated to include 'config-version'.");

            shouldSaveConfig = true;
            fileConfig.set("config-version", 1);
        }

        ConfigurationSection section = fileConfig.getConfigurationSection("settings");

        if (section == null) {
            Logger.warn(GeneralConfig.class, "Failed to parse section 'settings': Not found.");

            shouldSaveConfig = true;
            section = fileConfig.createSection("settings");
        }

        final String loggingLevelName = getValue(section, "logging-level", "INFO").replace(" ", "").toUpperCase(Locale.ROOT);
        final Level logginglevel = Level.getLevel(loggingLevelName) == null
                ? Level.INFO
                : Level.getLevel(loggingLevelName);

        if (shouldSaveConfig) {
            shouldSaveConfig = false;
            manager.saveConfig(file, fileConfig);
        }

        return new GeneralConfig(logginglevel);
    }

    private static String getValue(ConfigurationSection section, String key, String defValue) {
        final String value = section.getString(key);

        if (value == null) {
            Logger.warn(GeneralConfig.class, "Failed to parse '{}': Not found.", key);

            shouldSaveConfig = true;
            section.set(key, defValue);

            return defValue;
        }

        return value;
    }
}
