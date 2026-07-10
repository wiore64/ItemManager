package ru.yolta.customitemmanager.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import ru.yolta.customitemmanager.utils.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public record MessageConfig(
    @NotNull String prefix,
    @NotNull String noPermission,
    @NotNull String pluginDescription,
    @NotNull String invalidCommand,
    @NotNull String playerOnlyCommand,
    @NotNull String invalidArguments,
    @NotNull String mustHoldItem,
    @NotNull String itemRegistered,
    @NotNull String itemAlreadyRegistered,
    @NotNull String playerNotFound,
    @NotNull String itemNotFound,
    @NotNull String itemGiven,
    @NotNull String invalidAmount,
    @NotNull String itemList,
    @NotNull String pluginReloaded,
    @NotNull String itemUnregistered,
    @NotNull String pluginHelp
) {
    private static final Map<String, String> DEFAULT_VALUES;
    private static boolean shouldSaveConfig = false;

    static {
        final Map<String, String> map = new HashMap<>();

        map.put("prefix", "<gradient:#F47854:#B67E54>[CIM]:</gradient>");

        map.put("no-permission", "<red>You do not have permission to run this command!</red>");
        map.put("invalid-arguments", "<red>Invalid arguments.</red> Usage: <gold>{USAGE}</gold>");
        map.put("invalid-command", "<red>Unknown command.<red> Use <gold>/customitemmanager help.</gold>");
        map.put("player-only-command", "<red>This command can only be used by players.</red>");

        map.put("must-hold-item", "<gold>You must be holding an item.</gold>");
        map.put("item-already-registered", "<gold>Item {ITEM} is already registered.</gold>");
        map.put("item-not-found", "<gold>Item {ITEM} was not found.</gold>");
        map.put("player-not-found", "<gold>Player {PLAYER} was not found.</gold>");
        map.put("invalid-amount", "<gold>Invalid amount {AMOUNT}. Must be a positive integer.</gold>");

        map.put("item-registered", "<green>Item {ITEM} registered.</green>");
        map.put("item-unregistered", "<green>Item {ITEM} unregistered.</green>");
        map.put("item-given", "<green>Gave {AMOUNT}x {ITEM} to {PLAYER}.</green>");

        map.put("plugin-description", "A <gold>powerful and centralized</gold> custom <aqua>item management</aqua> plugin.");
        map.put("plugin-help", "List of commands: <gold>\n/cim add <key>: Adds a new item held in your hand with key serving as identifier\n/cim remove <key>: Removes the specified item\n/cim give <player> <key> [amount]: Gives item to player in the specified amount\n/cim list: Lists all currently registered items\n/cim reload: Reloads the plugin\n/cim help: Reveals this message");
        map.put("item-list", "Registered items: <gold>{ITEMS}</gold>");
        map.put("plugin-reloaded", "<green>Plugin reloaded.</green>");

        DEFAULT_VALUES = Map.copyOf(map);
    }

    static @NotNull MessageConfig parseMessageConfig(@NotNull ConfigProvider manager, @NotNull File file, @NotNull FileConfiguration fileConfig) {
        final int configVersion = fileConfig.getInt("config-version", -1);

        if (configVersion == -1) {
            Logger.info(MessageConfig.class, "Your config has been updated to include 'config-version'.");

            shouldSaveConfig = true;
            fileConfig.set("config-version", 1);
        }

        ConfigurationSection section = fileConfig.getConfigurationSection("messages");

        if (section == null) {
            Logger.warn(MessageConfig.class, "Failed to parse section 'messages': Not found.");

            shouldSaveConfig = true;
            section = fileConfig.createSection("messages");
        }

        final var config = new MessageConfig(
                getValue(fileConfig, "prefix", DEFAULT_VALUES.get("prefix")),
                getValue(section, "no-permission", DEFAULT_VALUES.get("no-permission")),
                getValue(section, "invalid-arguments", DEFAULT_VALUES.get("invalid-arguments")),
                getValue(section, "invalid-command", DEFAULT_VALUES.get("invalid-command")),
                getValue(section, "player-only-command", DEFAULT_VALUES.get("player-only-command")),
                getValue(section, "must-hold-item", DEFAULT_VALUES.get("must-hold-item")),
                getValue(section, "item-already-registered", DEFAULT_VALUES.get("item-already-registered")),
                getValue(section, "item-not-found", DEFAULT_VALUES.get("item-not-found")),
                getValue(section, "player-not-found", DEFAULT_VALUES.get("player-not-found")),
                getValue(section, "invalid-amount", DEFAULT_VALUES.get("invalid-amount")),
                getValue(section, "item-registered", DEFAULT_VALUES.get("item-registered")),
                getValue(section, "item-unregistered", DEFAULT_VALUES.get("item-unregistered")),
                getValue(section, "item-given", DEFAULT_VALUES.get("item-given")),
                getValue(section, "plugin-description", DEFAULT_VALUES.get("plugin-description")),
                getValue(section, "plugin-help", DEFAULT_VALUES.get("plugin-help")),
                getValue(section, "item-list", DEFAULT_VALUES.get("item-list")),
                getValue(section, "plugin-reloaded", DEFAULT_VALUES.get("plugin-reloaded"))
        );

        if (shouldSaveConfig) {
            shouldSaveConfig = false;
            manager.saveConfig(file, fileConfig);
        }

        return config;
    }

    private static String getValue(ConfigurationSection section, String key, String defValue) {
        final String value = section.getString(key);

        if (value == null) {
            Logger.warn(MessageConfig.class, "Failed to parse '{}': Not found.", key);

            shouldSaveConfig = true;
            section.set(key, defValue);

            return defValue;
        }

        return value;
    }
}
