package ru.vladimir.itemmanager.storage;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import ru.vladimir.itemmanager.ItemManager;

public final class CustomItemStorage {
    private final Map<String, byte[]> itemRegistry;

    public CustomItemStorage(@NotNull ItemManager plugin) {
        itemRegistry = readUserStorage(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "items.yml")));
    }

    private Map<String, byte[]> readUserStorage(FileConfiguration config) {
        // Do something.
        return Map.of();
    }

    private void appendItemToUserStorage(String itemId, ItemStack item) {

    }

    private void removeItemFromUserStorage(String itemId) {

    }

    private void writePluginStorage() {

    }

    public boolean registerCustomItem(@NotNull String itemId, @NotNull ItemStack item) {
        if (isCustomItem(itemId)) return false;
        appendItemToUserStorage(itemId, item);
        itemRegistry.put(itemId, item.serializeAsBytes());
        writePluginStorage();
        return true;
    }

    public boolean unregisterCustomItem(@NotNull String itemId) {
        if (!isCustomItem(itemId)) return false;
        removeItemFromUserStorage(itemId);
        itemRegistry.remove(itemId);
        writePluginStorage();
        return true;
    }

    public boolean isCustomItem(@NotNull String itemId) {
        return itemRegistry.containsKey(itemId);
    }

    @NotNull Optional<ItemStack> getCustomItem(@NotNull String itemId) {
        if (!isCustomItem(itemId)) return Optional.empty();

        return Optional.ofNullable(ItemStack.deserializeBytes(itemRegistry.get(itemId)));
    }

    public @NotNull @Unmodifiable Set<String> getCustomItemIds() {
        return Set.copyOf(itemRegistry.keySet());
    }
}
