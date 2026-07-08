package ru.yolta.customitemmanager.config;

import org.jetbrains.annotations.NotNull;

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
    static final Map<String, String> DEFAULT_VALUES;

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
}
