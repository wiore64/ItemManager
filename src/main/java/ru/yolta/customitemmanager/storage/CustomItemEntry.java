package ru.yolta.customitemmanager.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

record CustomItemEntry(
        @NotNull UUID internalId,
        @NotNull String materialName,
        @Nullable String displayName,
        @Nullable List<String> lore,
        @Nullable Integer customModelDataId,
        @Nullable List<EnchantmentEntry> enchantmentEntries,
        @Nullable List<AttributeEntry> attributeEntries,
        @Nullable Set<String> keys
) {
    CustomItemEntry(
            @NotNull UUID internalId,
            @NotNull String materialName,
            @Nullable String displayName,
            @Nullable List<String> lore,
            @Nullable Integer customModelDataId,
            @Nullable List<EnchantmentEntry> enchantmentEntries,
            @Nullable List<AttributeEntry> attributeEntries,
            @Nullable Set<String> keys
    ) {
        this.internalId = internalId;
        this.materialName = materialName.strip().replace(" ", "_").toUpperCase(Locale.ROOT);
        this.displayName = displayName;
        this.lore = lore;
        this.customModelDataId = customModelDataId;
        this.enchantmentEntries = enchantmentEntries;
        this.attributeEntries = attributeEntries;
        this.keys = keys;
    }

    @Nullable List<Map<String, ?>> enchantmentEntriesToMap() {
        if (enchantmentEntries == null) return null;

        final List<Map<String, ?>> enchantments = new ArrayList<>();

        for (final EnchantmentEntry entry : enchantmentEntries) {
            enchantments.add(entry.toMap());
        }

        return List.copyOf(enchantments);
    }

    @Nullable List<Map<String, ?>> attributeEntriesToMap() {
        if (attributeEntries == null) return null;

        final List<Map<String, ?>> attributes = new ArrayList<>();

        for (final AttributeEntry entry : attributeEntries) {
            attributes.add(entry.toMap());
        }

        return List.copyOf(attributes);
    }

    @NotNull @Unmodifiable
    Map<String, ?> toMap() {
        return Map.of(
                "internal-id", internalId,
                "material", materialName,
                "display-name", displayName,
                "lore", lore,
                "model-id", customModelDataId,
                "enchantments", enchantmentEntriesToMap(),
                "attributes", attributeEntriesToMap(),
                "keys", keys
        );
    }

    record EnchantmentEntry(@NotNull String name, int level) {
        EnchantmentEntry(@NotNull String name, int level) {
            this.name = name.strip().replace(" ", "_").toLowerCase(Locale.ROOT);
            this.level = Math.clamp(level, 0, 255);
        }

        @NotNull @Unmodifiable
        Map<String, ?> toMap() {
            return Map.of(
                    "name", name,
                    "level", level
            );
        }
    }

    record AttributeEntry(@NotNull String name, @NotNull List<AttributeModifierEntry> modifierEntries) {
        AttributeEntry(@NotNull String name, @NotNull List<AttributeModifierEntry> modifierEntries) {
            this.name = name.strip().replace(" ", "_").toLowerCase(Locale.ROOT);
            this.modifierEntries = modifierEntries;
        }

        @NotNull @Unmodifiable
        Map<String, ?> toMap() {
            final List<Map<String, ?>> modifiers = new ArrayList<>();

            for (final AttributeModifierEntry entry : modifierEntries) {
                modifiers.add(entry.toMap());
            }

            return Map.of(
                    "name", name,
                    "modifiers", modifiers
            );
        }
    }

    record AttributeModifierEntry(@NotNull String operationName, double amount, @NotNull String slotName) {
        AttributeModifierEntry(@NotNull String operationName, double amount, @NotNull String slotName) {
            this.operationName = operationName.replace(" ", "").toUpperCase(Locale.ROOT);
            this.amount = amount;
            this.slotName = slotName.replace(" ", "").toUpperCase(Locale.ROOT);
        }

        @NotNull @Unmodifiable
        Map<String, ?> toMap() {
            return Map.of(
                    "operation", operationName,
                    "amount", amount,
                    "slot", slotName
            );
        }
    }
}
