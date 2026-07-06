package ru.yolta.itemmanager.storage;

import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.yolta.itemmanager.utils.Logger;

import java.util.*;

final class CustomItemSerializer {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private CustomItemSerializer() {}

    static void serializeItemAndWriteToSection(ItemStack item, ConfigurationSection section) {
        final CustomItemEntry entry = serializeItem(item);

        Logger.getInstance().info("TEST", "Serializing: %s".formatted(entry));

        section.set("internal-id", entry.internalId().toString());
        section.set("material", entry.materialName());
        section.set("display-name", entry.displayName());
        section.set("lore", entry.lore());
        section.set("model-id", entry.customModelDataId());
        section.set("enchantments", entry.enchantmentEntriesToMap());
        section.set("attributes", entry.attributeEntriesToMap());
        section.set("keys", entry.keys() == null ? null : List.copyOf(entry.keys()));
    }

    static CustomItemEntry serializeItem(ItemStack item) {
        final Material material = item.getType();
        final String materialName = material.toString();

        final ItemMeta meta = item.getItemMeta() == null
                ? Bukkit.getItemFactory().getItemMeta(material)
                : item.getItemMeta();

        final Component displayName = meta.displayName();
        final String rawDisplayName = displayName == null
                ? null :
                serializeDisplayName(displayName);

        final List<Component> lore = item.lore();
        final List<String> rawLore = lore == null
                ? null
                : new ArrayList<>(lore.size());

        if (lore != null) {
            for (final Component line : lore) {
                rawLore.add(MINI_MESSAGE.serialize(line));
            }
        }

        final Map<Enchantment, Integer> enchantments = item.getEnchantments().isEmpty()
                ? null
                : item.getEnchantments();
        final List<CustomItemEntry.EnchantmentEntry> rawEnchantments = enchantments == null
                ? null
                : new ArrayList<>(enchantments.size());

        if (enchantments != null) {
            for (final Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                rawEnchantments.add(new CustomItemEntry.EnchantmentEntry(entry.getKey().getKey().toString(), entry.getValue()));
            }
        }

        final Integer customDataModelId = meta.hasCustomModelData()
                ? meta.getCustomModelData()
                : null;

        final Multimap<Attribute, AttributeModifier> attributes = meta.getAttributeModifiers() == null
                ? null
                : meta.getAttributeModifiers();
        final List<CustomItemEntry.AttributeEntry> rawAttributes = attributes == null
                ? null
                : new ArrayList<>(attributes.keys().size());

        if (attributes != null) {
            for (final Map.Entry<Attribute, Collection<AttributeModifier>> entry : attributes.asMap().entrySet()) {

                final Collection<AttributeModifier> attributeModifiers = entry.getValue();
                final List<CustomItemEntry.AttributeModifierEntry> rawAttributeModifiers = new ArrayList<>(attributeModifiers.size());

                for (final AttributeModifier modifier : attributeModifiers) {
                    rawAttributeModifiers.add(new CustomItemEntry.AttributeModifierEntry(
                            modifier.getOperation().toString(),
                            modifier.getAmount(),
                            modifier.getSlotGroup().toString()
                    ));
                }

                rawAttributes.add(new CustomItemEntry.AttributeEntry(entry.getKey().getKey().toString(), rawAttributeModifiers));
            }
        }

        final Set<NamespacedKey> keys = item.getPersistentDataContainer().getKeys();
        final Set<String> rawKeys = keys.isEmpty()
                ? null
                : new HashSet<>();

        if (!keys.isEmpty()) {
            for (final NamespacedKey key : keys) {
                if (key.equals(CustomItemStorage.ITEM_ISSUED_BY_CIM_NAMESPACEDKEY)) continue;
                if (key.getNamespace().equals(CustomItemStorage.ITEM_INTERNAL_ID_FOR_CIM_NAMESPACE)) continue;

                rawKeys.add(key.toString());
            }
        }

        return new CustomItemEntry(
                UUID.randomUUID(),
                materialName,
                rawDisplayName,
                rawLore,
                customDataModelId,
                rawEnchantments,
                rawAttributes,
                (rawKeys == null ? null : (rawKeys.isEmpty() ? null : rawKeys))
        );
    }

    private static String serializeDisplayName(Component displayName) {
        displayName = displayName.clickEvent(null).hoverEvent(null).insertion(null);
        return MINI_MESSAGE.serialize(displayName).replaceAll("<[^>]*lang:chat\\.square_brackets[^>]*:'([^']*)'>", "$1");
    }
}
