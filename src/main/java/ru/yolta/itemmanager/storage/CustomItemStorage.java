package ru.yolta.itemmanager.storage;

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

import ru.yolta.itemmanager.ItemManager;
import ru.yolta.itemmanager.utils.Logger;

public final class CustomItemStorage {

    static final NamespacedKey ITEM_ISSUED_BY_CIM_NAMESPACEDKEY = new NamespacedKey(ItemManager.getPluginShortName().toLowerCase(Locale.ROOT), "item");
    static final String ITEM_INTERNAL_ID_FOR_CIM_NAMESPACE = "cim_internal_id";
    private static final String FILE_STORAGE_NAME = "items.yml";
    private final ItemManager plugin;
    private final File configFile;
    private final Map<String, byte[]> itemRegistry;
    private final AtomicBoolean invalidateBuilderCache;

    public CustomItemStorage(@NotNull ItemManager plugin) {
        Logger.getInstance().debug(this, "Initializing...");

        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), FILE_STORAGE_NAME);
        this.itemRegistry = new ConcurrentHashMap<>();
        this.invalidateBuilderCache = new AtomicBoolean();

        ensureConfigFileExists();

        refreshItemRegistry(getItemConfig(configFile));

        Logger.getInstance().debug(this, "Initialized successfully.");
    }

    private void refreshItemRegistry(FileConfiguration itemConfig) {
        itemRegistry.clear();
        
        final Set<String> itemIds = itemConfig.getKeys(false);
        for (final String itemId : itemIds) {
            
            final ConfigurationSection section = itemConfig.getConfigurationSection(itemId);
            if (section == null) {
                Logger.getInstance().warn(this, "Item '%s' is not a configuration section.".formatted(itemId));
                continue;
            }

            final byte[] parsedItemData = CustomItemDeserializer.deserializeItem(plugin.getName().toLowerCase(Locale.ROOT), itemId, section);
            if (parsedItemData == null) {
                Logger.getInstance().warn(this, "Failed to parse '%s'.".formatted(itemId));
                continue;
            }

            itemRegistry.put(itemId, parsedItemData);
        }

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
            Logger.getInstance().warn(this, "Failed to serialize '%s' into section.".formatted(itemId));
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
            Logger.getInstance().error(this, "Failed to save file configuration to '%s'.".formatted(FILE_STORAGE_NAME), e);
        }
    }

    private void ensureConfigFileExists() {
        if (!configFile.exists()) {
            Logger.getInstance().info(this, "'%s' does not exist. A default one will be created.".formatted(FILE_STORAGE_NAME));
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
