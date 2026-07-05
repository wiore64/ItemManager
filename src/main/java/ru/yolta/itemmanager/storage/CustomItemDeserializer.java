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

        final Component displayName = resolveDisplayName(itemId, itemEntry.getString("display-name"));
        if (displayName == null) return null;

        final List<Component> lore = resolveLore(itemId, itemEntry.getList("lore"));
        if (lore == null) return null;

        final int customModelDataId = resolveCustomModelDataId(itemId, itemEntry.getInt("model-id", -1));

        final Map<Enchantment, Integer> enchantments = resolveEnchantments(itemId, itemEntry.getList("enchantments"));
        if (enchantments == null) return null;

        final Map<Attribute, List<AttributeModifier>> attributes = resolveAttributes(itemId, itemEntry.getList("attributes"));
        if (attributes == null) return null;

        final Set<NamespacedKey> pdcKeys = resolvePersistentKeys(pluginName, itemId, itemEntry.getList("keys"));
        if (pdcKeys == null) return null;

        final ItemStack item = ItemStack.of(Material.ACACIA_BUTTON);
        final ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);

        meta.displayName(displayName);
        meta.lore(lore);

        if (customModelDataId != -1) {
            meta.setCustomModelData(customModelDataId);
        }

        for (final Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        for (final Map.Entry<Attribute, List<AttributeModifier>> entry : attributes.entrySet()) {
            for (final AttributeModifier element : entry.getValue()) {
                meta.addAttributeModifier(entry.getKey(), element);
            }
        }

        final PersistentDataContainer container = meta.getPersistentDataContainer();
        for (final NamespacedKey key : pdcKeys) {
            container.set(key, PersistentDataType.BOOLEAN, true);
        }

        item.setItemMeta(meta);
        return item.serializeAsBytes();
    }

    private static Material resolveMaterial(String itemId, String materialName) {
        if (materialName == null) {
            Logger.getInstance().warn(LOG_SOURCE,
                    "Item '%s' is missing required field: material".formatted(itemId)
            );
            return null;
        }

        final Material material = Material.matchMaterial(materialName);

        if (material == null) {
            Logger.getInstance().warn(LOG_SOURCE,
                    "Item '%s' has invalid material '%s'".formatted(itemId, materialName)
            );
            return null;
        }

        return material;
    }

    private static Component resolveDisplayName(String itemId, String rawDisplayName) {
        if (rawDisplayName == null) {
            Logger.getInstance().warn(LOG_SOURCE,
                    "Item '%s' is missing required field: display-name".formatted(itemId)
            );
            return null;
        }

        return MINI_MESSAGE.deserialize("<!italic>" + rawDisplayName);
    }

    private static List<Component> resolveLore(String itemId, List<?> rawLore) {
        if (rawLore == null) {
            Logger.getInstance().warn(LOG_SOURCE,
                    "Item '%s' is missing required field: lore".formatted(itemId)
            );
            return null;
        }

        final List<Component> lore = new ArrayList<>();

        for (final Object rawLine : rawLore) {
            lore.add(MINI_MESSAGE.deserialize(String.valueOf(rawLine)));
        }

        return lore;
    }

    private static int resolveCustomModelDataId(String itemId, int modelDataId) {
        return modelDataId;
    }

    private static Map<Enchantment, Integer> resolveEnchantments(String itemId, List<?> rawEnchantments) {

        if (rawEnchantments == null) {
            Logger.getInstance().warn(LOG_SOURCE,
                    "Item '%s' is missing enchantments section".formatted(itemId)
            );
            return null;
        }

        final Map<Enchantment, Integer> enchantments = new HashMap<>();
        final Set<String> addedEnchantmentKeys = new HashSet<>();

        for (final Object obj : rawEnchantments) {

            if (!(obj instanceof final Map<?, ?> enchantmentMap)) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' invalid enchantment entry (not a map): %s".formatted(itemId, obj)
                );
                continue;
            }

            if (!enchantmentMap.containsKey("name") || !enchantmentMap.containsKey("level")) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' enchantment entry missing 'name' or 'level': %s".formatted(itemId, enchantmentMap)
                );
                continue;
            }

            final String rawKey = String.valueOf(enchantmentMap.get("name"))
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
                    ? new NamespacedKey("minecraft", rawKeyParts[0])
                    : new NamespacedKey(rawKeyParts[0], rawKeyParts[1]);

            final Enchantment enchantment = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.ENCHANTMENT)
                    .get(enchantmentKey);

            if (enchantment == null) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' unknown enchantment '%s'".formatted(itemId, rawKey)
                );
                continue;
            }

            final String rawLevel = String.valueOf(enchantmentMap.get("level"))
                    .strip()
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
                        "Item '%s' enchantment level %d for '%s' out of range (0-255), clamped".formatted(itemId, level, rawKey)
                );
            }

            enchantments.put(enchantment, Math.clamp(level, 0, 255));
        }

        return enchantments;
    }

    private static Map<Attribute, List<AttributeModifier>> resolveAttributes(String itemId, List<?> rawAttributes) {

        if (rawAttributes == null) {
            Logger.getInstance().warn(LOG_SOURCE,
                    "Item '%s' is missing attributes section".formatted(itemId)
            );
            return null;
        }

        final Map<Attribute, List<AttributeModifier>> attributes = new HashMap<>();
        final Set<String> addedAttributeKeys = new HashSet<>();

        for (final Object obj : rawAttributes) {

            if (!(obj instanceof final Map<?, ?> map)) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' invalid attribute entry (not a map): %s".formatted(itemId, obj)
                );
                continue;
            }

            if (!map.containsKey("name") || !map.containsKey("modifiers")) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' attribute missing 'name' or 'modifiers': %s".formatted(itemId, map)
                );
                continue;
            }

            final String rawKey = String.valueOf(map.get("name"))
                    .strip()
                    .replace(" ", "_")
                    .toLowerCase(Locale.ROOT);

            if (!addedAttributeKeys.add(rawKey)) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' duplicate attribute: '%s'".formatted(itemId, rawKey)
                );
                continue;
            }

            final String[] rawKeyParts = rawKey.split(":");

            if (rawKeyParts.length < 1 || rawKeyParts.length > 2) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' invalid attribute key '%s'".formatted(itemId, rawKey)
                );
                continue;
            }

            final NamespacedKey attributeKey = rawKeyParts.length == 1
                    ? new NamespacedKey("minecraft", rawKeyParts[0])
                    : new NamespacedKey(rawKeyParts[0], rawKeyParts[1]);

            final Attribute attribute = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.ATTRIBUTE)
                    .get(attributeKey);

            if (attribute == null) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' unknown attribute '%s'".formatted(itemId, rawKey)
                );
                continue;
            }

            final Object supposedRawModifiers = map.get("modifiers");

            if (!(supposedRawModifiers instanceof final List<?> rawModifiers)) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' attribute '%s' invalid modifiers list: %s".formatted(itemId, rawKey, supposedRawModifiers)
                );
                continue;
            }

            final List<AttributeModifier> modifiers = new ArrayList<>();

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

                modifiers.add(new AttributeModifier(new NamespacedKey("minecraft", UUID.randomUUID().toString()),
                        amount,
                        operation,
                        slotGroup
                ));
            }

            attributes.put(attribute, modifiers);
        }

        return attributes;
    }

    private static Set<NamespacedKey> resolvePersistentKeys(String pluginName, String itemId, List<?> rawKeys) {
        if (rawKeys == null) {
            Logger.getInstance().warn(
                    LOG_SOURCE, "Item '%s' missing persistent keys section".formatted(itemId)
            );
            return null;
        }

        final Set<NamespacedKey> keys = new HashSet<>();
        final Set<String> addedKeys = new HashSet<>();

        for (final Object rawKey : rawKeys) {

            final String rawKeyString = String.valueOf(rawKey)
                    .strip()
                    .replace(" ", "_")
                    .toLowerCase(Locale.ROOT);

            if (!addedKeys.add(rawKeyString)) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' duplicate persistent key: '%s'".formatted(itemId, rawKeyString)
                );
                continue;
            }

            final String[] rawKeyParts = rawKeyString.split(":");

            if (rawKeyParts.length < 1 || rawKeyParts.length > 2) {
                Logger.getInstance().warn(LOG_SOURCE,
                        "Item '%s' invalid persistent key format (expected namespace:key): '%s'".formatted(itemId, rawKeyString)
                );
                continue;
            }

            if (rawKeyParts.length == 1) {
                keys.add(new NamespacedKey(pluginName, rawKeyParts[0]));
                continue;
            }

            keys.add(new NamespacedKey(rawKeyParts[0], rawKeyParts[1]));
        }

        return keys;
    }
}
