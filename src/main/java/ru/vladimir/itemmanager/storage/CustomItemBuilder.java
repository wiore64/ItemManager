package ru.vladimir.itemmanager.storage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class CustomItemBuilder {

    private final CustomItemStorage itemStorage;
    private final Map<String, ItemStack> itemCache;

    public CustomItemBuilder(@NotNull CustomItemStorage itemStorage) {
        this.itemStorage = itemStorage;
        this.itemCache = new ConcurrentHashMap<>();
    }
    
    public @NotNull Optional<ItemStack> build(@NotNull String itemId) {
        if (!itemStorage.isCustomItem(itemId))
            return Optional.empty();

        if (itemCache.containsKey(itemId))
            return Optional.of(itemCache.get(itemId));

        return itemStorage.getCustomItem(itemId);
    }
}
