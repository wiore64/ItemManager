package ru.yolta.itemmanager.storage;

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
import ru.yolta.itemmanager.utils.Logger;

import java.util.*;

final class CustomItemDeserializer {

    private static final String LOG_SOURCE = "CustomItemDeserializer";
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private CustomItemDeserializer() {}

    static byte[] deserializeItem(String pluginName, String itemId, ConfigurationSection itemEntry) {
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

        Logger.getInstance().info("TEST", "Deserializing: %s".formatted(item));

        return item.serializeAsBytes();
    }

    private static Material resolveMaterial(String itemId, String materialName) {
        if (materialName == null) {
            Logger.getInstance().warn(LOG_SOURCE,
                    "Item '%s' missing required field: material".formatted(itemId)
            );
            return null;
        }

        final Material material = Material.matchMaterial(materialName);

        if (material == null) {
            Logger.getInstance().warn(LOG_SOURCE,
                    "Item '%s' invalid material '%s'".formatted(itemId, materialName)
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
            Logger.getInstance().warn(
                    LOG_SOURCE, "Item '%s' invalid custom model data ID: %s".formatted(itemId, rawCustomModelDataId)
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
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' invalid enchantment entry (not a map): %s".formatted(itemId, obj)
                );
                continue;
            }

            if (!rawEnchantment.containsKey("name") || !rawEnchantment.containsKey("level")) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' enchantment entry missing 'name' or 'level': %s".formatted(itemId, rawEnchantment)
                );
                continue;
            }

            final String rawKey = String.valueOf(rawEnchantment.get("name"))
                    .strip()
                    .replace(" ", "_")
                    .toLowerCase(Locale.ROOT);

            if (!addedEnchantmentKeys.add(rawKey)) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' duplicate enchantment key: '%s'".formatted(itemId, rawKey)
                );
                continue;
            }

            final String[] rawKeyParts = rawKey.split(":");

            if (rawKeyParts.length < 1 || rawKeyParts.length > 2) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' invalid enchantment namespace format: '%s'".formatted(itemId, rawKey)
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
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' enchantment not found: %s".formatted(itemId, rawKey)
                );
                continue;
            }

            final String rawLevel = String.valueOf(rawEnchantment.get("level"))
                    .replace(" ", "");

            final int level;

            try {
                level = Integer.parseInt(rawLevel);
            } catch (NumberFormatException e) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' invalid enchantment level '%s' for '%s'".formatted(itemId, rawLevel, rawKey)
                );
                continue;
            }

            if (level < 0 || level > 255) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' enchantment level %d for '%s' out of range (0-255). It was clamped.".formatted(itemId, level, rawKey)
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
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' invalid attribute entry (not a map): %s".formatted(itemId, obj)
                );
                continue;
            }

            if (!rawAttribute.containsKey("name") || !rawAttribute.containsKey("modifiers")) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' attribute missing 'name' or 'modifiers': %s".formatted(itemId, rawAttribute)
                );
                continue;
            }

            final String rawKey = String.valueOf(rawAttribute.get("name"))
                    .strip()
                    .replace(" ", "_")
                    .toLowerCase(Locale.ROOT);

            if (!addedAttributeKeys.add(rawKey)) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' duplicate attribute: %s".formatted(itemId, rawKey)
                );
                continue;
            }

            final String[] rawKeyParts = rawKey.split(":");

            if (rawKeyParts.length < 1 || rawKeyParts.length > 2) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' invalid attribute key: %s".formatted(itemId, rawKey)
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
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' attribute not found: %s".formatted(itemId, rawKey)
                );
                continue;
            }

            final Object supposedRawModifiers = rawAttribute.get("modifiers");

            if (!(supposedRawModifiers instanceof final List<?> rawModifiers)) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' attribute '%s' invalid modifiers list: %s".formatted(itemId, rawKey, supposedRawModifiers)
                );
                continue;
            }

            final List<AttributeModifier> modifiers = new ArrayList<>(rawModifiers.size());

            for (final Object mObj : rawModifiers) {

                if (!(mObj instanceof final Map<?, ?> rawModifier)) {
                    Logger.getInstance().warn(LOG_SOURCE,
                            "Item '%s' attribute '%s' invalid modifier entry: %s".formatted(itemId, rawKey, mObj)
                    );
                    continue;
                }

                if (!rawModifier.containsKey("operation") || !rawModifier.containsKey("amount")) {
                    Logger.getInstance().warn(LOG_SOURCE,
                            "Item '%s' attribute '%s' modifier missing fields: %s".formatted(itemId, rawKey, rawModifier)
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
                    Logger.getInstance().warn(LOG_SOURCE,
                            "Item '%s' attribute '%s' invalid operation: '%s'".formatted(itemId, rawKey, operationName)
                    );
                    continue;
                }

                final String rawAmount = String.valueOf(rawModifier.get("amount"))
                        .replace(" ", "");

                final double amount;

                try {
                    amount = Double.parseDouble(rawAmount);
                } catch (NumberFormatException e) {
                    Logger.getInstance().warn(LOG_SOURCE,
                            "Item '%s' attribute '%s' invalid amount: '%s'".formatted(itemId, rawKey, rawAmount)
                    );
                    continue;
                }

                final String slotName = String.valueOf(rawModifier.get("slot") == null ? "any" : rawModifier.get("slot"))
                        .replace(" ", "")
                        .toLowerCase(Locale.ROOT);

                final EquipmentSlotGroup slotGroup = EquipmentSlotGroup.getByName(slotName);

                if (slotGroup == null) {
                    Logger.getInstance().warn(LOG_SOURCE,
                            "Item '%s' attribute '%s' invalid slot: '%s'".formatted(itemId, rawKey, slotName)
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
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' attribute '%s' has no modifiers.".formatted(itemId, attributeKey)
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
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' invalid persistent key format (expected namespace:key or key): %s".formatted(itemId, rawKey)
                );
                continue;
            }

            final NamespacedKey key = rawKeyParts.length == 1
                    ? new NamespacedKey(pluginName, rawKeyParts[0])
                    : new NamespacedKey(rawKeyParts[0], rawKeyParts[1]);

            if (!keys.add(key)) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' duplicate persistent key: %s".formatted(itemId, key.toString())
                );
            }
        }

        return keys;
    }

    private static Set<NamespacedKey> resolveInternalKeys(String itemId, ConfigurationSection itemEntry) {
        final Set<NamespacedKey> internalKeys = new HashSet<>(2);

        internalKeys.add(CustomItemStorage.ITEM_ISSUED_BY_CIM_NAMESPACEDKEY);

        final String internalId = itemEntry.getString("internal-id");

        if (internalId == null) {
            Logger.getInstance().debug(LOG_SOURCE,
                    "Item '%s' missing internal key. Generating a new one...".formatted(itemId)
            );

            final String newInternalId = UUID.randomUUID().toString();

            itemEntry.set("internal-id", newInternalId);
            internalKeys.add(new NamespacedKey(CustomItemStorage.ITEM_INTERNAL_ID_FOR_CIM_NAMESPACE, newInternalId));
        } else {
            try {
                UUID.fromString(internalId);
                internalKeys.add(new NamespacedKey(CustomItemStorage.ITEM_INTERNAL_ID_FOR_CIM_NAMESPACE, internalId));
            } catch (IllegalArgumentException e) {
                Logger.getInstance().debug(LOG_SOURCE,
                        "Item '%s' missing internal key. Generating a new one...".formatted(itemId)
                );

                final String newInternalId = UUID.randomUUID().toString();

                itemEntry.set("internal-id", newInternalId);
                internalKeys.add(new NamespacedKey(CustomItemStorage.ITEM_INTERNAL_ID_FOR_CIM_NAMESPACE, newInternalId));
            }
        }

        return internalKeys;
    }
}
