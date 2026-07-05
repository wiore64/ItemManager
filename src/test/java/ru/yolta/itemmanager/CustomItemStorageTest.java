package ru.yolta.itemmanager;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import ru.yolta.itemmanager.storage.CustomItemBuilder;
import ru.yolta.itemmanager.storage.CustomItemStorage;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("deprecation")
public class CustomItemStorageTest {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private ServerMock server;
    private CustomItemStorage storage;
    private CustomItemBuilder builder;

    @Before
    public void setUp() {
        server = MockBukkit.mock();
        storage = new CustomItemStorage(MockBukkit.load(ItemManager.class));
        builder = new CustomItemBuilder(storage);
    }

    @After
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void registerBasicItem() {
        final ItemStack item = ItemStack.of(Material.DIAMOND_SWORD);

        Assert.assertTrue(storage.registerCustomItem("test", item));
        Assert.assertTrue(storage.isCustomItem("test"));
        Assert.assertFalse(storage.getCustomItemIds().isEmpty());
    }

    @Test
    public void registerBasicItemWithAmount() {
        final ItemStack item = ItemStack.of(Material.DIRT, 72);

        Assert.assertTrue(storage.registerCustomItem("test", item));
        Assert.assertTrue(storage.isCustomItem("test"));
        Assert.assertFalse(storage.getCustomItemIds().isEmpty());
    }

    @Test
    public void registerAdvancedItem() {
        final String invalidMaterialName = "test";
        final Material invalidMaterial = Material.matchMaterial(invalidMaterialName);

        final ItemStack item = new ItemStack(Material.NETHER_BRICK);
        final ItemMeta meta = server.getItemFactory().getItemMeta(Material.NETHER_BRICK);

        meta.setCustomModelData(5);
        meta.lore(List.of(MINI_MESSAGE.deserialize("<gold>Crafted</gold> by <aqua>dwarfs.")));
        meta.addEnchant(Enchantment.BINDING_CURSE, 5, true);
        meta.getPersistentDataContainer().set(new NamespacedKey("test", "key"), PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);

        Assert.assertTrue(storage.registerCustomItem("test", item));
        Assert.assertTrue(storage.isCustomItem("test"));
        Assert.assertFalse(storage.getCustomItemIds().isEmpty());
    }

    @Test
    public void getBasicItem() {
        final ItemStack item = ItemStack.of(Material.DIAMOND_SWORD);

        storage.registerCustomItem("test", item);

        final Optional<ItemStack> optionalItem = builder.build("test");

        Assert.assertTrue(optionalItem.isPresent());

        final ItemStack retrievedItem = optionalItem.get();

        Assert.assertEquals(item.getType(), retrievedItem.getType());

        final ItemMeta itemMeta = server.getItemFactory().getItemMeta(Material.DIAMOND_SWORD);
        final ItemMeta retrievedItemMeta = retrievedItem.getItemMeta();

        Assert.assertEquals(itemMeta.lore(), retrievedItemMeta.lore());
        Assert.assertEquals(itemMeta.hasCustomModelData(), retrievedItemMeta.hasCustomModelData());
        Assert.assertEquals(itemMeta.getEnchants(), retrievedItemMeta.getEnchants());
        Assert.assertEquals(itemMeta.getPersistentDataContainer().getKeys(), retrievedItemMeta.getPersistentDataContainer().getKeys());
    }

    @Test
    public void getAdvancedItem() {
        final ItemStack item = ItemStack.of(Material.DIAMOND_SWORD);
        final ItemMeta meta = server.getItemFactory().getItemMeta(Material.NETHER_BRICK);

        meta.setCustomModelData(5);
        meta.lore(List.of(MINI_MESSAGE.deserialize("<gold>Crafted</gold> by <aqua>dwarfs.")));
        meta.addEnchant(Enchantment.BINDING_CURSE, 5, true);
        meta.getPersistentDataContainer().set(new NamespacedKey("test", "key"), PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);

        storage.registerCustomItem("test", item);

        final Optional<ItemStack> optionalItem = builder.build("test");

        Assert.assertTrue(optionalItem.isPresent());

        final ItemStack retrievedItem = optionalItem.get();

        Assert.assertEquals(item.getType(), retrievedItem.getType());

        final ItemMeta itemMeta = item.getItemMeta();
        final ItemMeta retrievedItemMeta = retrievedItem.getItemMeta();

        Assert.assertEquals(itemMeta.lore(), retrievedItemMeta.lore());
        Assert.assertEquals(itemMeta.getCustomModelData(), retrievedItemMeta.getCustomModelData());
        Assert.assertEquals(itemMeta.getEnchants(), retrievedItemMeta.getEnchants());
        Assert.assertEquals(itemMeta.getPersistentDataContainer().getKeys(), retrievedItemMeta.getPersistentDataContainer().getKeys());
    }

    @Test
    public void removeBasicItem() {
        final ItemStack item = ItemStack.of(Material.DIAMOND_SWORD);

        storage.registerCustomItem("test", item);

        Assert.assertTrue(storage.unregisterCustomItem("test"));
        Assert.assertFalse(storage.isCustomItem("test"));
        Assert.assertTrue(storage.getCustomItemIds().isEmpty());
    }
}
