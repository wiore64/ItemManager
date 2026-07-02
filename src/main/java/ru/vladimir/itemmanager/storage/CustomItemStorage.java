package ru.vladimir.itemmanager.storage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import net.kyori.adventure.text.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import com.google.common.collect.Multimap;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ru.vladimir.itemmanager.ItemManager;
import ru.vladimir.itemmanager.utils.Logger;

public final class CustomItemStorage {

    private static final MiniMessage MINI_MESSAGE_PARSER = MiniMessage.miniMessage();
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

            final byte[] parsedItemData = deserializeItemEntryIntoBytes(itemId, section);
            if (parsedItemData == null) {
                Logger.getInstance().warn(this, "Failed to parse '%s'.".formatted(itemId));
                continue;
            }

            itemRegistry.put(itemId, parsedItemData);
        }

        registryRefreshed();
    }

    private boolean appendItemToStorage(File file, FileConfiguration itemConfig, String itemId, ItemStack item) {
        refreshItemRegistry(itemConfig);

        if (itemConfig.contains(itemId)) return false;

        final ConfigurationSection sectionToCopyFrom = itemConfig.createSection(itemId);
        serializeItemIntoSection(sectionToCopyFrom, item);

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

    private byte[] deserializeItemEntryIntoBytes(String itemId, ConfigurationSection itemEntry) {
        final String materialName = itemEntry.getString("material");
        if (materialName == null) {
            Logger.getInstance().warn(this, "Failed to parse '%s': No material name.".formatted(itemId));
            return null;
        }

        final Material material = Material.matchMaterial(materialName.strip().toUpperCase(Locale.ROOT));
        if (material == null) {
            Logger.getInstance().warn(this, "Failed to parse '%s': Bad material name '%s'.".formatted(itemId, materialName));
            return null;
        }

        final String rawDisplayName = itemEntry.getString("name");
        if (rawDisplayName == null) {
            Logger.getInstance().warn(this, "Failed to parse '%s': No item name.".formatted(itemId));
            return null;
        }

        final Component displayName = MINI_MESSAGE_PARSER.deserialize(rawDisplayName);

        final List<?> rawLore = itemEntry.getList("lore");
        if (rawLore == null) {
            Logger.getInstance().warn(this, "Failed to parse '%s': No lore.".formatted(itemId));
            return null;
        }

        final List<Component> lore = new ArrayList<>();

        for (final Object rawSupposedLine : rawLore) {
            if (!(rawSupposedLine instanceof final String rawLine)) {
                Logger.getInstance().warn(this, "Failed to parse line of lore of '%s': Not a string '%s'.".formatted(itemId, rawSupposedLine));
                continue;
            }
            lore.add(MINI_MESSAGE_PARSER.deserialize(rawLine));
        }

        final List<?> rawEnchantments = itemEntry.getList("enchantments");
        if (rawEnchantments == null) {
            Logger.getInstance().warn(this, "Failed to parse '%s': Enchantments not found.".formatted(itemId));
            return null;
        }

        final Set<EnchantmentEntry> enchantments = new HashSet<>();
        final Set<String> addedEnchantments = new HashSet<>();
    
        for (final Object rawEnchantment : rawEnchantments) {
            if (!(rawEnchantment instanceof final Map<?, ?> entry)) {
                Logger.getInstance().warn(this, "Failed to parse enchantment of '%s': '%s' is not entry.".formatted(itemId, rawEnchantment));
                continue;
            }

            if (!entry.containsKey("name") || !entry.containsKey("level")) {
                Logger.getInstance().warn(this, "Failed to parse enchantment of '%s': '%s' is not valid entry.".formatted(itemId, entry));
                continue;
            }

            final String key = String.valueOf(entry.get("name")).strip().toLowerCase(Locale.ROOT);

            if (addedEnchantments.contains(key)) {
                Logger.getInstance().warn(this, "Failed to parse enchantment '%s' of '%s': A duplicate.".formatted(key, itemId));
                continue;
            }

            final Object supposedLevel = entry.get("level");
            final int level;

            try {
                level = Integer.parseInt(String.valueOf(supposedLevel));
            } catch (NumberFormatException e) {
                Logger.getInstance().warn(this, "Failed to parse enchantment '%s' of '%s': Invalid level '%s'.".formatted(key, itemId, supposedLevel));
                continue;
            }

            if (level < 0 || level > 255) {
                Logger.getInstance().warn(this, "Enchantment '%s' of '%s' has a level exceeding the capacity 0-255 (%d).".formatted(key, itemId, level));
            }

            enchantments.add(new EnchantmentEntry(key, level));
            addedEnchantments.add(key);
        }

        final List<?> rawAttributes = itemEntry.getList("attributes");
        if (rawAttributes == null) {
            Logger.getInstance().warn(this, "Failed to parse '%s': Attributes not found.".formatted(itemId));
            return null;
        }

        final Set<AttributeEntry> attributes = new HashSet<>();
        final Set<String> addedAttributes = new HashSet<>();

        for (final Object rawAttribute : rawAttributes) {
            if (!(rawAttribute instanceof final Map<?, ?> entry)) {
                Logger.getInstance().warn(this, "Failed to parse attribute of '%s': '%s' is not entry.".formatted(itemId, rawAttribute));
                continue;
            }

            if (!entry.containsKey("name") || !entry.containsKey("modifiers")) {
                Logger.getInstance().warn(this, "Failed to parse attribute of '%s': '%s' is not valid entry.".formatted(itemId, entry));
                continue;
            }

            final String key = String.valueOf(entry.get("name")).strip().toLowerCase(Locale.ROOT);
            
            if (addedAttributes.contains(key)) {
                Logger.getInstance().warn(this, "Failed to parse attribute '%s' of '%s': A duplicate.".formatted(key, itemId));
                continue;
            }

            final Object supposedRawModifiers = entry.get("modifiers");
            if (!(supposedRawModifiers instanceof final List<?> rawModifiers)) {
                Logger.getInstance().warn(this, "Failed to parse attribute '%s' of '%s': '%s' is not a valid list of modifiers.".formatted(key, itemId, supposedRawModifiers));
                continue;
            }

            final List<AttributeModifierEntry> modifiers = new ArrayList<>();

            for (final Object rawModifier : rawModifiers) {
                if (!(rawModifier instanceof final Map<?, ?> modifierEntry)) {
                    Logger.getInstance().warn(this, "Failed to parse attribute modifier of '%s' of '%s': '%s' is not entry.".formatted(key, itemId, rawModifier));
                    continue;
                }

                if (!modifierEntry.containsKey("operation") || !modifierEntry.containsKey("amount")) {
                    Logger.getInstance().warn(this, "Failed to parse attribute modifier of '%s' of '%s': '%s' is not valid entry.".formatted(key, itemId, modifierEntry));
                    continue;
                }

                final Object supposedAmount = modifierEntry.get("amount");
                final double amount;

                try {
                    amount = Double.parseDouble(String.valueOf(supposedAmount));
                } catch (NumberFormatException e) {
                    Logger.getInstance().warn(this, "Failed to parse attribute modifier of '%s' of '%s': '%s' is not valid amount.".formatted(key, itemId, supposedAmount));
                    continue;
                }

                final Object supposedSlotGroupName = modifierEntry.get("slot");
                final String slotGroupName = supposedSlotGroupName == null ? "any" : String.valueOf(supposedSlotGroupName);

                modifiers.add(new AttributeModifierEntry(
                    slotGroupName, 
                    String.valueOf(modifierEntry.get("operation")), 
                    amount
                ));
            }

            attributes.add(new AttributeEntry(key, modifiers));
            addedAttributes.add(key);
        }

        final List<?> rawKeys = itemEntry.getList("keys");
        if (rawKeys == null) {
            Logger.getInstance().warn(this, "Failed to parse '%s': Keys not found.".formatted(itemId));
            return null;
        }

        final Set<NamespacedKey> keys = new HashSet<>();
        final Set<String> addedKeys = new HashSet<>();

        for (final Object supposedRawKey : rawKeys) {
            if (!(supposedRawKey instanceof String)) {
                Logger.getInstance().warn(this, "Failed to parse key of '%s': '%s' is not key.".formatted(itemId, supposedRawKey));
                continue;
            }

            final String rawKey = String.valueOf(supposedRawKey).strip().toLowerCase(Locale.ROOT);

            if (addedKeys.contains(rawKey)) {
                Logger.getInstance().warn(this, "Failed to parse key '%s' of '%s': A duplicate.".formatted(rawKey, itemId));
                continue;
            }

            final String[] splitKey = rawKey.split(":");

            if (splitKey.length == 1 && splitKey[0].isEmpty()) {
                Logger.getInstance().warn(this, "Failed to parse key '%s' of '%s': Invalid format.".formatted(rawKey, itemId));
                continue;
            } else if (splitKey.length == 2 && (splitKey[0].isEmpty() || splitKey[1].isEmpty())) {
                Logger.getInstance().warn(this, "Failed to parse key '%s' of '%s': Invalid format.".formatted(rawKey, itemId));
                continue;
            } else if (splitKey.length > 2) {
                Logger.getInstance().warn(this, "Failed to parse key '%s' of '%s': Invalid format.".formatted(rawKey, itemId));
                continue;
            }

            if (splitKey.length == 1) {
                keys.add(new NamespacedKey(plugin, splitKey[0]));
            } else {
                keys.add(new NamespacedKey(splitKey[0], splitKey[1]));
            }

            addedKeys.add(rawKey);
        }

        final ItemStack item = ItemStack.of(material);
        final ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);

        if (meta == null) {
            Logger.getInstance().warn(this, "Material '%s' of '%s' does not support item meta; configured display name, lore, enchantments, attributes, and keys were ignored for it.".formatted(materialName, itemId));
            return item.serializeAsBytes();
        }

        meta.displayName(displayName);
        meta.lore(lore);

        for (final EnchantmentEntry entry : enchantments) {

            final Enchantment enchantment = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(new NamespacedKey("minecraft", entry.key()));

            if (enchantment == null) {
                Logger.getInstance().warn(this, "Failed to parse enchantment '%s' of '%s': Invalid enchantment.".formatted(entry.key(), itemId));
                continue;
            }

            meta.addEnchant(enchantment, entry.level(), true);
        }

        for (final AttributeEntry entry : attributes) {

            final String key = entry.key();
            final Attribute attribute = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ATTRIBUTE)
                .get(new NamespacedKey("minecraft", key));

            if (attribute == null) {
                Logger.getInstance().warn(this, "Failed to parse attribute '%s' of '%s': Invalid attribute.".formatted(key, itemId));
                continue;
            }

            for (final AttributeModifierEntry modifierEntry : entry.modifiers()) {

                final String modifierOperationName = modifierEntry.operationName();
                final Operation modifierOperation;

                try {
                    modifierOperation = Operation.valueOf(modifierOperationName);
                } catch (IllegalArgumentException e) {
                    Logger.getInstance().warn(this, "Failed to parse modifier of attribute '%s' of '%s': '%s' is not valid operation.".formatted(key, itemId, modifierOperationName));
                    continue;
                }

                final String slotGroupName = modifierEntry.slotGroupName();
                final EquipmentSlotGroup slotGroup = slotGroupName == null ? null : EquipmentSlotGroup.getByName(slotGroupName);

                if (slotGroup == null) {
                    meta.addAttributeModifier(attribute, new AttributeModifier(new NamespacedKey(plugin, UUID.randomUUID().toString()), modifierEntry.amount(), modifierOperation));
                } else {
                    meta.addAttributeModifier(attribute, new AttributeModifier(new NamespacedKey(plugin, UUID.randomUUID().toString()), modifierEntry.amount(), modifierOperation, slotGroup));
                }
            }
        }

        final PersistentDataContainer container = meta.getPersistentDataContainer();

        for (final NamespacedKey key : keys) {
            container.set(key, PersistentDataType.BOOLEAN, true);
        }

        item.setItemMeta(meta);

        return item.serializeAsBytes();
    }

    private void serializeItemIntoSection(ConfigurationSection section, ItemStack item) {
        final Material material = item.getType();
        final String materialName = material.toString().toUpperCase(Locale.ROOT);

        final Component displayName = stripTranslatableContainers(item.displayName());
        final String rawDisplayName;

        if (isDefaultDisplayName(displayName)) {
            final char capitalLetter = materialName.charAt(0);
            final String materialNameRest = materialName.substring(1).toLowerCase(Locale.ROOT);
            rawDisplayName = "<italic:false>" + (capitalLetter + materialNameRest).replace("_", " ");
        } else {
            rawDisplayName = MINI_MESSAGE_PARSER.serialize(displayName);
        }

        final List<Component> lore = item.lore();
        final List<String> rawLore = new ArrayList<>();

        if (lore != null) {
            for (final Component line : lore) {
                Logger.getInstance().info(this, "Serializing this line of lore: %s".formatted(line));

                rawLore.add(MINI_MESSAGE_PARSER.serialize(line));

                Logger.getInstance().info(this, "Serialized it into this: %s".formatted(MINI_MESSAGE_PARSER.serialize(line)));
            }
        }

        section.set("material", materialName);
        section.set("name", rawDisplayName);
        section.set("lore", rawLore);

        final ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta == null) {
            section.set("enchantments", List.of());
            section.set("attributes", List.of());
            section.set("keys", List.of());
            return;
        }

        final Map<Enchantment, Integer> enchantments = itemMeta.getEnchants();
        final List<Map<String, Object>> rawEnchantments = new ArrayList<>();

        if (!enchantments.isEmpty()) {
            for (final var entry : enchantments.entrySet()) {
                rawEnchantments.add(Map.of(
                        "name", entry.getKey().getKey().getKey(),
                        "level", entry.getValue()
                ));
            }
        }

        final Multimap<Attribute, AttributeModifier> attributes = itemMeta.getAttributeModifiers();
        final List<Map<String, Object>> rawAttributes = new ArrayList<>();

        if (attributes != null) {
            for (final var entry : attributes.asMap().entrySet()) {

                final List<Map<String, Object>> rawAttributeModifiers = new ArrayList<>();
                for (final var modifierEntry : entry.getValue()) {
                    rawAttributeModifiers.add(Map.of(
                            "operation", modifierEntry.getOperation().toString().toUpperCase(Locale.ROOT),
                            "amount", modifierEntry.getAmount(),
                            "slot", modifierEntry.getSlotGroup().toString()
                    ));
                }

                rawAttributes.add(Map.of(
                        "name", entry.getKey().getKey().getKey(),
                        "modifiers", rawAttributeModifiers
                ));
            }
        }

        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        final List<String> rawKeys = new ArrayList<>();

        for (final NamespacedKey key : container.getKeys()) {
            rawKeys.add(key.toString());
        }

        section.set("enchantments", rawEnchantments);
        section.set("attributes", rawAttributes);
        section.set("keys", rawKeys);
    }

    public Component stripTranslatableContainers(Component component) {
        component = component.hoverEvent(null).clickEvent(null).insertion(null);

        if (component instanceof final TranslatableComponent ts) {
            final List<Component> contents = new ArrayList<>();

            for (final TranslationArgument arg : ts.arguments()) {
                contents.add(stripTranslatableContainers((Component) arg.value()));
            }

            for (final Component c : ts.children()) {
                contents.add(stripTranslatableContainers(c));
            }

            return Component.text().style(ts.style()).build().children(contents);
        }

        if (component instanceof final TextComponent tc) {
            final List<Component> cleanedChildren = new ArrayList<>();

            for (final Component c : tc.children()) {
                cleanedChildren.add(stripTranslatableContainers(c));
            }

            return tc.children(cleanedChildren);
        }

        return component;
    }

    private boolean isDefaultDisplayName(Component component) {
        boolean result;
        for (final Component c : component.children()) {
            if (!c.children().isEmpty()) {
                result = isDefaultDisplayName(c);
                if (!result) return false;
            }
            if (c instanceof final TextComponent tc) {
                result = tc.content().isEmpty();
                if (!result) return false;
            }
        }
        return true;
    }

    private record EnchantmentEntry(String key, int level) {
        private EnchantmentEntry(String key, int level) {
            this.key = key.strip().toLowerCase(Locale.ROOT);
            this.level = level;
        }
    }

    private record AttributeEntry(String key, List<AttributeModifierEntry> modifiers) {
        private AttributeEntry(String key, List<AttributeModifierEntry> modifiers) {
            this.key = key.strip().toLowerCase(Locale.ROOT);
            this.modifiers = modifiers;
        }
    }
    private record AttributeModifierEntry(String slotGroupName, String operationName, double amount) {
        private AttributeModifierEntry(String slotGroupName, String operationName, double amount) {
            this.slotGroupName = slotGroupName.strip().toLowerCase(Locale.ROOT);
            this.operationName = operationName.strip().toUpperCase(Locale.ROOT);
            this.amount = amount;
        }
    }
}
