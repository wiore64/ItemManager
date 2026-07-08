package ru.yolta.customitemmanager.config;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.utils.Logger;

public final class ConfigManager {

    private static final String GENERAL_CONFIG_FILE_NAME = "config.yml";
    private static final String MESSAGE_CONFIG_FILE_NAME = "messages.yml";
    private final GeneralConfig generalConfig;
    private final MessageConfig messageConfig;

    public ConfigManager(@NotNull CustomItemManager plugin) {
        Logger.getInstance().debug(this, "Initializing...");

        this.generalConfig = parseGeneralConfig(getGeneralFileConfig(plugin));
        this.messageConfig = parseMessageConfig(getMessageFileConfig(plugin));

        Logger.getInstance().debug(this, "Initialized successfully.");
    }

    private FileConfiguration getGeneralFileConfig(CustomItemManager plugin) {
        final File configFile = new File(plugin.getDataFolder(), GENERAL_CONFIG_FILE_NAME);

        if (!configFile.exists()) {
            Logger.getInstance().info(this, "'%s' does not exist. A default one will be created.".formatted(GENERAL_CONFIG_FILE_NAME));
            plugin.saveResource(GENERAL_CONFIG_FILE_NAME, false);
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    private FileConfiguration getMessageFileConfig(CustomItemManager plugin) {
        final File configFile = new File(plugin.getDataFolder(), MESSAGE_CONFIG_FILE_NAME);

        if (!configFile.exists()) {
            Logger.getInstance().info(this, "'%s' does not exist. A default one will be created.".formatted(MESSAGE_CONFIG_FILE_NAME));
            plugin.saveResource(MESSAGE_CONFIG_FILE_NAME, false);
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    private GeneralConfig parseGeneralConfig(FileConfiguration config) {
        final String levelName = config.getString("logging-level");

        if (levelName == null) {
            Logger.getInstance().warn(this, "Failed to parse logging level in '%s': Level not found.".formatted(GENERAL_CONFIG_FILE_NAME));
            return new GeneralConfig(Logger.LogLevel.INFO);
        }

        final Logger.LogLevel level = Logger.LogLevel.getLogLevel(levelName);
        if (level == null) {
            Logger.getInstance().warn(this, "Failed to parse logging level: Invalid level '%s'.".formatted(levelName));
            return new GeneralConfig(Logger.LogLevel.INFO);
        }

        return new GeneralConfig(level);
    }

    private MessageConfig parseMessageConfig(FileConfiguration config) {
        return new MessageConfig(
                getMessage(config, "no-permission"),
                getMessage(config, "plugin-description"),
                getMessage(config, "invalid-command"),
                getMessage(config, "player-only-command"),
                getMessage(config, "invalid-arguments"),
                getMessage(config, "must-hold-item"),
                getMessage(config, "item-registered"),
                getMessage(config, "item-already-registered"),
                getMessage(config, "player-not-found"),
                getMessage(config, "item-not-found"),
                getMessage(config, "item-given"),
                getMessage(config, "invalid-amount"),
                getMessage(config, "item-list"),
                getMessage(config, "plugin-reloaded"),
                getMessage(config, "item-unregistered"),
                getMessage(config, "plugin-help")
        );
    }

    private String getMessage(FileConfiguration config, String key) {
        final String value = config.getString(key);

        if (value == null) {
            Logger.getInstance().warn(this, "Failed to parse '%s' in '%s': Message not found. Using default.".formatted(key, MESSAGE_CONFIG_FILE_NAME));
            return MessageConfig.DEFAULT_MESSAGES.get(key);
        }

        return value;
    }

    public @NotNull GeneralConfig getGeneralConfig() {
        return generalConfig;
    }

    public @NotNull MessageConfig getMessageConfig() {
        return messageConfig;
    }
}
