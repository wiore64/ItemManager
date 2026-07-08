package ru.yolta.customitemmanager.config;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
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
        this.messageConfig = parseMessageConfig(new File(plugin.getDataFolder(), MESSAGE_CONFIG_FILE_NAME), getMessageFileConfig(plugin));

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

    private void saveMessageFileConfig(File file, FileConfiguration fileConfig) {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private MessageConfig parseMessageConfig(File file, FileConfiguration fileConfig) {
        ConfigurationSection section = fileConfig.getConfigurationSection("messages");

        boolean isSectionExists = true;

        if (section == null) {
            Logger.getInstance().warn(ConfigManager.class, "Failed to find the section for messages. A new one will be created.");
            isSectionExists = false;
            section = fileConfig.createSection("messages");
        }

        final var config = new MessageConfig(
                getValueFromMessageConfig(fileConfig, "prefix", true),
                getValueFromMessageConfig(section, "no-permission", !isSectionExists),
                getValueFromMessageConfig(section, "plugin-description", !isSectionExists),
                getValueFromMessageConfig(section, "invalid-command", !isSectionExists),
                getValueFromMessageConfig(section, "player-only-command", !isSectionExists),
                getValueFromMessageConfig(section, "invalid-arguments", !isSectionExists),
                getValueFromMessageConfig(section, "must-hold-item", !isSectionExists),
                getValueFromMessageConfig(section, "item-registered", !isSectionExists),
                getValueFromMessageConfig(section, "item-already-registered", !isSectionExists),
                getValueFromMessageConfig(section, "player-not-found", !isSectionExists),
                getValueFromMessageConfig(section, "item-not-found", !isSectionExists),
                getValueFromMessageConfig(section, "item-given", !isSectionExists),
                getValueFromMessageConfig(section, "invalid-amount", !isSectionExists),
                getValueFromMessageConfig(section, "item-list", !isSectionExists),
                getValueFromMessageConfig(section, "plugin-reloaded", !isSectionExists),
                getValueFromMessageConfig(section, "item-unregistered", !isSectionExists),
                getValueFromMessageConfig(section, "plugin-help", !isSectionExists)
        );

        if (!isSectionExists) {
            saveMessageFileConfig(file, fileConfig);
        }

        return config;
    }

    private String getValueFromMessageConfig(ConfigurationSection section, String key, boolean shouldReport) {
        final String value = section.getString(key);

        if (value == null) {

            if (shouldReport) {
                Logger.getInstance().warn(this, "Failed to parse '%s' in '%s': Value not found. Using default.".formatted(key, MESSAGE_CONFIG_FILE_NAME));
            }

            final String defValue = MessageConfig.DEFAULT_VALUES.get(key);

            section.set(key, defValue);
            return defValue;
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
