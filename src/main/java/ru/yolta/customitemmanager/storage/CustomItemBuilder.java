package ru.yolta.customitemmanager.storage;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class CustomItemBuilder {

    private static final long ITEM_CACHE_ENTRY_EXPIRE_IN = 180000L; // 3 minutes in ms
    private final CustomItemStorage itemStorage;
    private final Map<String, ItemCacheEntry> itemCache;

    public CustomItemBuilder(@NotNull CustomItemStorage itemStorage) {
        this.itemStorage = itemStorage;
        this.itemCache = new ConcurrentHashMap<>();
    }
    
    public @NotNull Optional<ItemStack> build(@NotNull String itemId) {
        // IMPLICIT RELATIONSHIP Builder -> Storage -- START
        if (itemStorage.consumeCacheInvalidationSignal()) {
            clearCache();
        } else {
            clearOldCacheEntries();
        }
        // IMPLICIT RELATIONSHIP Builder -> Storage -- END

        if (!itemStorage.isCustomItem(itemId)) return Optional.empty();
        if (itemCache.containsKey(itemId)) return Optional.of(itemCache.get(itemId).item);

        final Optional<ItemStack> customItem = itemStorage.getCustomItem(itemId);
        if (customItem.isEmpty()) return Optional.empty();

        addNewCacheEntry(itemId, customItem.get());

        return customItem;
    }

    private void clearOldCacheEntries() {
        if (itemCache.isEmpty()) return;

        final Instant timeNow = Instant.now();

        for (final var entry : Set.copyOf(itemCache.entrySet())) {
            if (!entry.getValue().isExpired(timeNow)) continue;
            itemCache.remove(entry.getKey());
        }
    }

    private void clearCache() {
        itemCache.clear();
    }

    private void addNewCacheEntry(String itemId, ItemStack item) {
        itemCache.put(itemId, new ItemCacheEntry(item, Instant.now()));
    }

    private record ItemCacheEntry(ItemStack item, Instant timestamp) {
        private boolean isExpired(Instant newestTimestamp) {
            return Duration.between(newestTimestamp, this.timestamp).toMillis() > ITEM_CACHE_ENTRY_EXPIRE_IN;
        }
    }
}
