package ru.yolta.customitemmanager.storage;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import ru.yolta.customitemmanager.utils.Logger;

import java.util.*;

final class CustomItemDeserializer {

    private static final String LOG_NAME = "CustomItemDeserializer";
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private CustomItemDeserializer() {}

    static byte[] deserializeItem(@NotNull String pluginName, @NotNull String itemId, @NotNull ConfigurationSection itemEntry) {
        final Material material = resolveMaterial(itemId, itemEntry.getString("material"));
        if (material == null) return null;

        final ItemStack item = ItemStack.of(material);
        final ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);

        meta.customName(resolveDisplayName(itemId, itemEntry.getString("display-name")));
        meta.lore(resolveLore(itemId, itemEntry.getStringList("lore")));
        meta.setCustomModelData(resolveCustomModelDataId(itemId, itemEntry.getString("model-id")));

        for (final var entry : resolveEnchantments(itemId, itemEntry.getList("enchantments")).entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        for (final var entry : resolveAttributes(itemId, itemEntry.getList("attributes")).entrySet()) {
            for (final var modifier : entry.getValue()) {
                meta.addAttributeModifier(entry.getKey(), modifier);
            }
        }

        final PersistentDataContainer container = meta.getPersistentDataContainer();

        for (final NamespacedKey key : resolvePersistentKeys(pluginName, itemId, itemEntry.getStringList("keys"))) {
            container.set(key, PersistentDataType.BOOLEAN, true);
        }

        for (final NamespacedKey key : resolveInternalKeys(itemId, itemEntry)) {
            container.set(key, PersistentDataType.BOOLEAN, true);
        }

        item.setItemMeta(meta);

        return item.serializeAsBytes();
    }

    private static Material resolveMaterial(String itemId, String materialName) {
        if (materialName == null) {
            Logger.warn(LOG_NAME,
                    "Item '{}' missing required field: material", itemId
            );
            return null;
        }

        final Material material = Material.matchMaterial(materialName);

        if (material == null) {
            Logger.warn(LOG_NAME,
                    "Item '{}' invalid material: {}", itemId, materialName
            );
            return null;
        }

        return material;
    }

    private static Component resolveDisplayName(String itemId, String rawDisplayName) {
        if (rawDisplayName == null) return null;

        return MINI_MESSAGE.deserialize("<!italic>" + rawDisplayName);
    }

    private static List<Component> resolveLore(String itemId, List<String> rawLore) {
        if (rawLore == null) return null;

        final List<Component> lore = new ArrayList<>(rawLore.size());

        for (final String rawLine : rawLore) {
            lore.add(MINI_MESSAGE.deserialize(rawLine));
        }

        return lore;
    }

    private static Integer resolveCustomModelDataId(String itemId, String rawCustomModelDataId) {
        if (rawCustomModelDataId == null) return null;

        try {
            return Integer.parseInt(rawCustomModelDataId.replace(" ", ""));
        } catch (NumberFormatException e) {
            Logger.warn(
                    LOG_NAME, "Item '{}' invalid custom model data ID: {}", itemId, rawCustomModelDataId
            );
            return null;
        }
    }

    private static Map<Enchantment, Integer> resolveEnchantments(String itemId, List<?> rawEnchantments) {
        if (rawEnchantments == null) return Map.of();

        final Map<Enchantment, Integer> enchantments = new HashMap<>(rawEnchantments.size());
        final Set<String> addedEnchantmentKeys = new HashSet<>(rawEnchantments.size());

        for (final Object obj : rawEnchantments) {
            if (!(obj instanceof final Map<?, ?> rawEnchantment)) {
                Logger.warn(LOG_NAME,
                        "Item '{}' invalid enchantment entry (not a map): {}", itemId, obj
                );
                continue;
            }

            if (!rawEnchantment.containsKey("name") || !rawEnchantment.containsKey("level")) {
                Logger.warn(LOG_NAME,
                        "Item '{}' enchantment entry missing 'name' or 'level': {}", itemId, rawEnchantment
                );
                continue;
            }

            final String rawKey = String.valueOf(rawEnchantment.get("name"))
                    .strip()
                    .replace(" ", "_")
                    .toLowerCase(Locale.ROOT);

            if (!addedEnchantmentKeys.add(rawKey)) {
                Logger.warn(LOG_NAME,
                        "Item '{}' duplicate enchantment key: {}", itemId, rawKey
                );
                continue;
            }

            final String[] rawKeyParts = rawKey.split(":");

            if (rawKeyParts.length < 1 || rawKeyParts.length > 2) {
                Logger.warn(LOG_NAME,
                        "Item '{}' invalid enchantment namespace format: {}", itemId, rawKey
                );
                continue;
            }

            final NamespacedKey enchantmentKey = rawKeyParts.length == 1
                    ? new NamespacedKey(NamespacedKey.MINECRAFT, rawKeyParts[0])
                    : new NamespacedKey(rawKeyParts[0], rawKeyParts[1]);

            final Enchantment enchantment = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.ENCHANTMENT)
                    .get(enchantmentKey);

            if (enchantment == null) {
                Logger.warn(LOG_NAME,
                        "Item '{}' enchantment not found: {}", itemId, rawKey
                );
                continue;
            }

            final String rawLevel = String.valueOf(rawEnchantment.get("level"))
                    .replace(" ", "");

            final int level;

            try {
                level = Integer.parseInt(rawLevel);
            } catch (NumberFormatException e) {
                Logger.warn(LOG_NAME,
                        "Item '{}' invalid enchantment level '{}' for '{}'", itemId, rawLevel, rawKey
                );
                continue;
            }

            if (level < 0 || level > 255) {
                Logger.warn(LOG_NAME,
                        "Item '{}' enchantment level %d for '{}' out of range (0-255). It was clamped.", itemId, level, rawKey
                );
            }

            enchantments.put(enchantment, Math.clamp(level, 0, 255));
        }

        return enchantments;
    }

    private static Map<Attribute, List<AttributeModifier>> resolveAttributes(String itemId, List<?> rawAttributes) {
        if (rawAttributes == null) return Map.of();

        final Map<Attribute, List<AttributeModifier>> attributes = new HashMap<>(rawAttributes.size());
        final Set<String> addedAttributeKeys = new HashSet<>(rawAttributes.size());

        for (final Object obj : rawAttributes) {
            if (!(obj instanceof final Map<?, ?> rawAttribute)) {
                Logger.warn(LOG_NAME,
                        "Item '{}' invalid attribute entry (not a map): {}", itemId, obj
                );
                continue;
            }

            if (!rawAttribute.containsKey("name") || !rawAttribute.containsKey("modifiers")) {
                Logger.warn(LOG_NAME,
                        "Item '{}' attribute missing 'name' or 'modifiers': {}", itemId, rawAttribute
                );
                continue;
            }

            final String rawKey = String.valueOf(rawAttribute.get("name"))
                    .strip()
                    .replace(" ", "_")
                    .toLowerCase(Locale.ROOT);

            if (!addedAttributeKeys.add(rawKey)) {
                Logger.warn(LOG_NAME,
                        "Item '{}' duplicate attribute: {}", itemId, rawKey
                );
                continue;
            }

            final String[] rawKeyParts = rawKey.split(":");

            if (rawKeyParts.length < 1 || rawKeyParts.length > 2) {
                Logger.warn(LOG_NAME,
                        "Item '{}' invalid attribute key: {}", itemId, rawKey
                );
                continue;
            }

            final NamespacedKey attributeKey = rawKeyParts.length == 1
                    ? new NamespacedKey(NamespacedKey.MINECRAFT, rawKeyParts[0])
                    : new NamespacedKey(rawKeyParts[0], rawKeyParts[1]);

            final Attribute attribute = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.ATTRIBUTE)
                    .get(attributeKey);

            if (attribute == null) {
                Logger.warn(LOG_NAME,
                        "Item '{}' attribute not found: {}", itemId, rawKey
                );
                continue;
            }

            final Object supposedRawModifiers = rawAttribute.get("modifiers");

            if (!(supposedRawModifiers instanceof final List<?> rawModifiers)) {
                Logger.warn(LOG_NAME,
                        "Item '{}' attribute '{}' invalid modifiers list: {}", itemId, rawKey, supposedRawModifiers
                );
                continue;
            }

            final List<AttributeModifier> modifiers = new ArrayList<>(rawModifiers.size());

            for (final Object mObj : rawModifiers) {

                if (!(mObj instanceof final Map<?, ?> rawModifier)) {
                    Logger.warn(LOG_NAME,
                            "Item '{}' attribute '{}' invalid modifier entry: {}", itemId, rawKey, mObj
                    );
                    continue;
                }

                if (!rawModifier.containsKey("operation") || !rawModifier.containsKey("amount")) {
                    Logger.warn(LOG_NAME,
                            "Item '{}' attribute '{}' modifier missing fields: {}", itemId, rawKey, rawModifier
                    );
                    continue;
                }

                final String operationName = String.valueOf(rawModifier.get("operation"))
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "");

                final AttributeModifier.Operation operation;

                try {
                    operation = AttributeModifier.Operation.valueOf(operationName);
                } catch (IllegalArgumentException e) {
                    Logger.warn(LOG_NAME,
                            "Item '{}' attribute '{}' invalid operation: {}", itemId, rawKey, operationName
                    );
                    continue;
                }

                final String rawAmount = String.valueOf(rawModifier.get("amount"))
                        .replace(" ", "");

                final double amount;

                try {
                    amount = Double.parseDouble(rawAmount);
                } catch (NumberFormatException e) {
                    Logger.warn(LOG_NAME,
                            "Item '{}' attribute '{}' invalid amount: {}", itemId, rawKey, rawAmount
                    );
                    continue;
                }

                final String slotName = String.valueOf(rawModifier.get("slot") == null ? "any" : rawModifier.get("slot"))
                        .replace(" ", "")
                        .toLowerCase(Locale.ROOT);

                final EquipmentSlotGroup slotGroup = EquipmentSlotGroup.getByName(slotName);

                if (slotGroup == null) {
                    Logger.warn(LOG_NAME,
                            "Item '{}' attribute '{}' invalid slot: {}", itemId, rawKey, slotName
                    );
                    continue;
                }

                modifiers.add(new AttributeModifier(new NamespacedKey(NamespacedKey.MINECRAFT, UUID.randomUUID().toString()),
                        amount,
                        operation,
                        slotGroup
                ));
            }

            if (modifiers.isEmpty()) {
                Logger.warn(LOG_NAME,
                        "Item '{}' attribute '{}' has no modifiers.", itemId, attributeKey
                );
                continue;
            }

            attributes.put(attribute, modifiers);
        }

        return attributes;
    }

    private static Set<NamespacedKey> resolvePersistentKeys(String pluginName, String itemId, List<String> rawKeys) {
        if (rawKeys == null) return Set.of();

        final Set<NamespacedKey> keys = new HashSet<>(rawKeys.size());

        for (final String rawKey : rawKeys) {
            final String[] rawKeyParts = rawKey
                    .strip()
                    .replace(" ", "_")
                    .toLowerCase(Locale.ROOT)
                    .split(":");

            if (rawKeyParts.length < 1 || rawKeyParts.length > 2) {
                Logger.warn(LOG_NAME,
                        "Item '{}' invalid persistent key format (expected namespace:key or key): {}", itemId, rawKey
                );
                continue;
            }

            final NamespacedKey key = rawKeyParts.length == 1
                    ? new NamespacedKey(pluginName, rawKeyParts[0])
                    : new NamespacedKey(rawKeyParts[0], rawKeyParts[1]);

            if (!keys.add(key)) {
                Logger.warn(LOG_NAME,
                        "Item '{}' duplicate persistent key: {}", itemId, key.toString()
                );
            }
        }

        return keys;
    }

    private static Set<NamespacedKey> resolveInternalKeys(String itemId, ConfigurationSection itemEntry) {
        final Set<NamespacedKey> internalKeys = new HashSet<>(2);

        internalKeys.add(CustomItemStorage.CIM_ITEM_NAMESPACEDKEY);

        final String internalId = itemEntry.getString("internal-id");

        if (internalId == null) {
            Logger.debug(LOG_NAME,
                    "Item '{}' missing internal ID. Generating a new one...", itemId
            );

            final String newInternalId = UUID.randomUUID().toString();

            itemEntry.set("internal-id", newInternalId);
            internalKeys.add(new NamespacedKey(CustomItemStorage.CIM_ITEM_INTERNAL_ID_NAMESPACE, newInternalId));

            return internalKeys;
        }

        try {
            UUID.fromString(internalId);
            internalKeys.add(new NamespacedKey(CustomItemStorage.CIM_ITEM_INTERNAL_ID_NAMESPACE, internalId));
        } catch (IllegalArgumentException e) {
            Logger.warn(LOG_NAME,
                    "Item '{}' invalid internal key. Generating a new one...", itemId
            );

            final String newInternalId = UUID.randomUUID().toString();

            itemEntry.set("internal-id", newInternalId);
            internalKeys.add(new NamespacedKey(CustomItemStorage.CIM_ITEM_INTERNAL_ID_NAMESPACE, newInternalId));
        }

        return internalKeys;
    }
}
