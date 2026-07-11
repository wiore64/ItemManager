package ru.yolta.customitemmanager.storage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.utils.Logger;

public final class CustomItemStorage {

    static final NamespacedKey CIM_ITEM_NAMESPACEDKEY = new NamespacedKey(CustomItemManager.PLUGIN_NAME.toLowerCase(Locale.ROOT), "item");
    static final String CIM_ITEM_INTERNAL_ID_NAMESPACE = CustomItemManager.PLUGIN_NAME.toLowerCase(Locale.ROOT) + "_internal_id";

    private static final String FILE_STORAGE_NAME = "items.yml";
    private final CustomItemManager plugin;
    private final File configFile;
    private final Map<String, byte[]> itemRegistry;
    // HIDDEN RELATIONSHIP -- START
    private final AtomicBoolean invalidateBuilderCache;
    // HIDDEN RELATIONSHIP -- END

    public CustomItemStorage(@NotNull CustomItemManager plugin) {
        Logger.debug(this, "Initializing...");

        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), FILE_STORAGE_NAME);
        this.itemRegistry = new ConcurrentHashMap<>();
        this.invalidateBuilderCache = new AtomicBoolean();

        ensureConfigFileExists();

        refreshItemRegistry(getItemConfig(configFile));

        Logger.debug(this, "Initialized successfully.");
    }

    private void refreshItemRegistry(FileConfiguration itemConfig) {
        itemRegistry.clear();
        
        final Set<String> itemIds = itemConfig.getKeys(false);
        for (final String itemId : itemIds) {
            
            final ConfigurationSection section = itemConfig.getConfigurationSection(itemId);
            if (section == null) {
                Logger.warn(this, "Item '{}' is not a config section.", itemId);
                continue;
            }

            final byte[] parsedItemData = CustomItemDeserializer.deserializeItem(
                    CustomItemManager.PLUGIN_NAME.toLowerCase(Locale.ROOT),
                    itemId,
                    section
            );
            if (parsedItemData == null) {
                Logger.warn(this, "Failed to parse '{}'.", itemId);
                continue;
            }

            itemRegistry.put(itemId, parsedItemData);
        }

        // Should save it during refresh or handle it differently?
        // In reference to the management of internal IDs
        saveItemConfig(itemConfig);

        registryRefreshed();
    }

    private boolean appendItemToStorage(File file, FileConfiguration itemConfig, String itemId, ItemStack item) {
        refreshItemRegistry(itemConfig);

        if (itemConfig.contains(itemId)) return false;

        final ConfigurationSection sectionToCopyFrom = itemConfig.createSection(itemId);
        CustomItemSerializer.serializeItemAndWriteToSection(item, sectionToCopyFrom);

        final Set<String> sectionToCopyFromKeys = sectionToCopyFrom.getKeys(true);
        if (sectionToCopyFromKeys.isEmpty()) {
            Logger.warn(this, "Failed to serialize '{}' into section.", itemId);
            return false;
        }

        final ConfigurationSection sectionToCopyTo = itemConfig.createSection(itemId);

        for (final String key : sectionToCopyFromKeys) {
            final Object value = sectionToCopyFrom.get(key);
            sectionToCopyTo.set(key, value);
        }

        saveItemConfig(itemConfig);

        refreshItemRegistry(itemConfig);

        return true;
    }

    private boolean removeItemFromStorage(File file, FileConfiguration itemConfig, String itemId) {
        refreshItemRegistry(itemConfig);

        if (!itemConfig.contains(itemId)) return false;
        
        itemConfig.set(itemId, null);

        saveItemConfig(itemConfig);

        refreshItemRegistry(itemConfig);

        return true;
    }

    private FileConfiguration getItemConfig(File configFile) {
        ensureConfigFileExists();

        return YamlConfiguration.loadConfiguration(configFile);
    }

    private void saveItemConfig(FileConfiguration config) {
        ensureConfigFileExists();

        try {
            if (config.getKeys(false).isEmpty()) {
                plugin.saveResource(FILE_STORAGE_NAME, true);
                return;
            }

            config.save(configFile);
        } catch (IOException e) {
            Logger.error(this, "Failed to save file configuration to '{}'.", FILE_STORAGE_NAME, e);
        }
    }

    private void ensureConfigFileExists() {
        if (!configFile.exists()) {
            Logger.warn(this, "'{}' does not exist. Creating it now.", FILE_STORAGE_NAME);
            plugin.saveResource(FILE_STORAGE_NAME, false);
        }
    }

    public boolean registerCustomItem(@NotNull String itemId, @NotNull ItemStack item) {
        if (isCustomItem(itemId)) return false;
        
        return appendItemToStorage(configFile, getItemConfig(configFile), itemId, item);
    }

    public boolean unregisterCustomItem(@NotNull String itemId) {
        if (!isCustomItem(itemId)) return false;

        return removeItemFromStorage(configFile, getItemConfig(configFile), itemId);
    }

    public boolean isCustomItem(@NotNull String itemId) {
        return itemRegistry.containsKey(itemId);
    }

    @NotNull Optional<ItemStack> getCustomItem(@NotNull String itemId) {
        if (!isCustomItem(itemId)) return Optional.empty();

        return Optional.of(ItemStack.deserializeBytes(itemRegistry.get(itemId)));
    }

    public @NotNull @Unmodifiable Set<String> getCustomItemIds() {
        return Set.copyOf(itemRegistry.keySet());
    }

    // HIDDEN RELATIONSHIP -- START
    void registryRefreshed() {
        invalidateBuilderCache.set(true);
    }

    boolean consumeCacheInvalidationSignal() {
        return invalidateBuilderCache.getAndSet(false);
    }
    // HIDDEN RELATIONSHIP -- END
}
