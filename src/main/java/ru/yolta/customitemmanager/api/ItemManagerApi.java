package ru.yolta.customitemmanager.api;

import java.util.Optional;
import java.util.Set;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.storage.CustomItemBuilder;
import ru.yolta.customitemmanager.storage.CustomItemStorage;

public final class ItemManagerApi {

    private final CustomItemManager plugin;
    private final CustomItemStorage itemStorage;
    private final CustomItemBuilder itemBuilder;

    public ItemManagerApi(@NotNull CustomItemManager plugin, @NotNull CustomItemStorage itemStorage, @NotNull CustomItemBuilder itemBuilder) {
        this.plugin = plugin;
        this.itemStorage = itemStorage;
        this.itemBuilder = itemBuilder;
    }
    
    public void reloadPlugin() {
        plugin.onReload();
    }

    public boolean registerCustomItem(@NotNull String itemId, @NotNull ItemStack item) {
        return itemStorage.registerCustomItem(itemId, item);
    }

    public boolean unregisterCustomItem(@NotNull String itemId) {
        return itemStorage.unregisterCustomItem(itemId);
    }
    
    public boolean isCustomItem(@NotNull String itemId) {
        return itemStorage.isCustomItem(itemId);
    }
    
    public @NotNull Optional<ItemStack> getCustomItem(@NotNull String itemId) {
        return itemBuilder.build(itemId);
    }
    
    public @NotNull @Unmodifiable Set<String> getAllCustomItemIds() {
        return itemStorage.getCustomItemIds();
    }
}
