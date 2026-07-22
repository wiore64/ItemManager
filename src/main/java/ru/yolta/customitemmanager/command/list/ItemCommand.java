package ru.yolta.customitemmanager.command.list;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.command.SubCommand;
import ru.yolta.customitemmanager.config.MessageConfig;
import ru.yolta.customitemmanager.utils.Messenger;

import java.util.*;
import java.util.stream.IntStream;

public class ItemCommand implements SubCommand {

    public static final Set<String> ALIASES = Set.of("item");
    public static final Permission PERMISSION = new Permission("customitemmanager.command.item");
    private final CustomItemManager plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final MessageConfig.SharedCmdSection sharedMessages;
    private final MessageConfig.ManageItemCmdSection cmdMessages;

    public ItemCommand(
            @NotNull CustomItemManager plugin,
            @NotNull MessageConfig.SharedCmdSection sharedMessages,
            @NotNull MessageConfig.ManageItemCmdSection cmdMessages
    ) {
        this.plugin = plugin;
        this.sharedMessages = sharedMessages;
        this.cmdMessages = cmdMessages;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length < 2) {
            Messenger.sendMessage(sender, sharedMessages.invalidArguments(), Map.of(
                    "USAGE", "/cim item <name|lore|model|enchantments|attributes|keys>")
            );
            return;
        }

        if (!(sender instanceof final Player player)) {
            Messenger.sendMessage(sender, sharedMessages.playerOnlyCommand());
            return;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            Messenger.sendMessage(sender, sharedMessages.mustHoldItem());
            return;
        }

        final ItemMeta meta = item.getItemMeta() == null
                ? Bukkit.getItemFactory().getItemMeta(item.getType())
                : item.getItemMeta();

        switch (args[1].strip().toLowerCase(Locale.ROOT)) {
            case "name" -> handleItemName(player, args, item, meta);
            case "lore" -> handleItemLore(player, args, item, meta);
            case "model" -> handleItemModel(player, args, item, meta);
            case "enchantments" -> handleItemEnchantments(player, args, item, meta);
            case "attributes" -> handleItemAttributes(player, args, item, meta);
            case "keys" -> handleItemKeys(player, args, item, meta);
            default -> Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                    "USAGE", "/cim item <name|lore|model|enchantments|attributes|keys>")
            );
        }
    }

    private void handleItemName(Player player, String[] args, ItemStack item, ItemMeta meta) {
        if (args.length < 3 || args.length > 4) {
            Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                    "USAGE", "\n/cim item name set <name>\n/cim item name clear")
            );
            return;
        }

        final String option = args[2]
                .strip()
                .toLowerCase(Locale.ROOT);

        if (option.equals("clear")) {
            if (args.length != 3) {
                Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                        "USAGE", "/cim item name clear")
                );
                return;
            }

            meta.displayName(null);
            item.setItemMeta(meta);

            Messenger.sendMessage(player, cmdMessages.itemCmdNameCleared());

            return;
        }

        if (option.equals("set")) {
            if (args.length != 4) {
                Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                        "USAGE", "/cim item name set <name>")
                );
                return;
            }

            meta.displayName(miniMessage.deserialize(args[3]));
            item.setItemMeta(meta);

            Messenger.sendMessage(player, cmdMessages.itemCmdNameUpdated());

            return;
        }

        Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                "USAGE", "\n/cim item name set <name>\n/cim item name clear")
        );
    }

    private void handleItemLore(Player player, String[] args, ItemStack item, ItemMeta meta) {
        if (args.length < 4 || args.length > 6) {
            Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                    "USAGE", "\n/cim item lore add <text>\n/cim item lore set <line> <text>\n/cim item lore remove <line>\n/cim item lore clear")
            );
            return;
        }

        final String option = args[3]
                .strip()
                .toLowerCase(Locale.ROOT);

        switch (option) {
            case "add" -> {
                if (args.length != 5) {
                    Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                            "USAGE", "/cim item lore add <text>"
                    ));
                    return;
                }

                final List<Component> lore = getItemLore(meta);

                lore.add(miniMessage.deserialize(args[4]));
                meta.lore(lore);

                item.setItemMeta(meta);

                Messenger.sendMessage(player, cmdMessages.itemCmdLoreLineAppended());
            }

            case "set" -> {
                if (args.length != 6) {
                    Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                            "USAGE", "/cim item lore set <line> <text>"
                    ));
                    return;
                }

                final int lineIndex;

                try {
                    lineIndex = Integer.parseInt(args[4].strip()) - 1;
                } catch (NumberFormatException e) {
                    Messenger.sendMessage(player, cmdMessages.itemCmdLoreBadIndex(), Map.of(
                            "INDEX", args[4]
                    ));
                    return;
                }

                final List<Component> lore = getItemLore(meta);

                if (lineIndex > lore.size()) {
                    Messenger.sendMessage(player, cmdMessages.itemCmdLoreLargeIndex(), Map.of(
                            "INDEX", args[4], "MAX_INDEX", lore.size()
                    ));
                    return;
                }

                lore.set(lineIndex, miniMessage.deserialize(args[5]));
                meta.lore(lore);

                item.setItemMeta(meta);

                Messenger.sendMessage(player, cmdMessages.itemCmdLoreLineUpdated());
            }

            case "remove" -> {
                if (args.length != 5) {
                    Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                            "USAGE", "/cim item lore remove <line>"
                    ));
                    return;
                }

                final int lineIndex;

                try {
                    lineIndex = Integer.parseInt(args[4].strip()) - 1;
                } catch (NumberFormatException e) {
                    Messenger.sendMessage(player, cmdMessages.itemCmdLoreBadIndex(), Map.of(
                            "INDEX", args[4]
                    ));
                    return;
                }

                final List<Component> lore = getItemLore(meta);

                if (lineIndex > lore.size()) {
                    Messenger.sendMessage(player, cmdMessages.itemCmdLoreLargeIndex(), Map.of(
                            "INDEX", args[4]
                    ));
                    return;
                }

                lore.remove(lineIndex);
                meta.lore(lore);

                item.setItemMeta(meta);

                Messenger.sendMessage(player, cmdMessages.itemCmdLoreLineRemoved());
            }

            case "clear" -> {
                if (args.length != 4) {
                    Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                            "USAGE", "/cim item lore clear"
                    ));
                    return;
                }

                meta.lore(null);
                item.setItemMeta(meta);

                Messenger.sendMessage(player, cmdMessages.itemCmdLoreCleared());
            }

            default -> Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                    "USAGE", "\n/cim item lore add <text>\n/cim item lore set <line> <text>\n/cim item lore remove <line>\n/cim item lore clear")
            );
        }
    }

    private void handleItemModel(Player player, String[] args, ItemStack item, ItemMeta meta) {
        if (args.length < 3 || args.length > 4) {
            Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                    "USAGE", "/cim item model <clear|set> [id]"
            ));
            return;
        }

        final String option = args[2]
                .strip()
                .toLowerCase(Locale.ROOT);

        if (option.equals("clear")) {
            if (args.length != 3) {
                Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                        "USAGE", "/cim item model clear")
                );
                return;
            }

            meta.setCustomModelData(null);
            item.setItemMeta(meta);

            Messenger.sendMessage(player, cmdMessages.itemCmdModelCleared());

            return;
        }

        if (option.equals("set")) {
            if (args.length != 4) {
                Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                        "USAGE", "/cim item model set <id>")
                );
                return;
            }

            final int model;

            try {
                model = Integer.parseInt(args[3].strip());
            } catch (NumberFormatException e) {
                Messenger.sendMessage(player, cmdMessages.itemCmdInvalidModelId(), Map.of(
                        "MODEL", args[3]
                ));
                return;
            }

            meta.setCustomModelData(model);
            item.setItemMeta(meta);

            Messenger.sendMessage(player, cmdMessages.itemCmdModelUpdated());

            return;
        }

        Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                "USAGE", "/cim item model <clear|set> [id]")
        );
    }

    private void handleItemEnchantments(Player player, String[] args, ItemStack item, ItemMeta meta) {
        if (args.length < 3 || args.length > 5) {
            Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                    "USAGE", "\n/cim item enchantments set <enchantment> <level>\n/cim item enchantments remove <enchantment>\n/cim item enchantments clear")
            );
            return;
        }

        final String option = args[2]
                .trim()
                .toLowerCase(Locale.ROOT);

        final Map<Enchantment, Integer> enchantments = meta.getEnchants();

        switch (option) {
            case "set" -> {
                if (args.length != 5) {
                    Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                            "USAGE", "/cim item enchantments set <enchantment> <level>")
                    );
                    return;
                }

                final Enchantment enchantment = RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.ENCHANTMENT)
                        .get(NamespacedKey.minecraft(args[3].trim().toLowerCase(Locale.ROOT)));

                if (enchantment == null) {
                    Messenger.sendMessage(player, cmdMessages.itemCmdUnknownEnchantment(), Map.of(
                            "ENCHANTMENT", args[3]
                    ));
                    return;
                }

                final int level;

                try {
                    level = Integer.parseInt(args[4].trim());
                } catch (NumberFormatException e) {
                    Messenger.sendMessage(player, cmdMessages.itemCmdInvalidEnchantmentLevel(), Map.of(
                            "LEVEL", args[4]
                    ));
                    return;
                }

                if (level < 1 || level > 255) {
                    Messenger.sendMessage(player, cmdMessages.itemCmdOutOfBoundsEnchantmentLevel(), Map.of(
                            "LEVEL", level
                    ));
                    return;
                }

                if (enchantments.containsKey(enchantment) && enchantments.get(enchantment) == level) {
                    Messenger.sendMessage(player, cmdMessages.itemCmdEnchantmentAlreadyPresent(), Map.of(
                            "ENCHANTMENT", args[3], "LEVEL", args[4]
                    ));
                    return;
                }

                meta.addEnchant(enchantment, level, true);
                item.setItemMeta(meta);

                Messenger.sendMessage(player, cmdMessages.itemCmdEnchantmentUpdated());
            }

            case "remove" -> {
                if (args.length != 4) {
                    Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                            "USAGE", "/cim item enchantments remove <enchantment>")
                    );
                    return;
                }

                final Enchantment enchantment = RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.ENCHANTMENT)
                        .get(NamespacedKey.minecraft(args[3].trim().toLowerCase(Locale.ROOT)));

                if (enchantment == null) {
                    Messenger.sendMessage(player, cmdMessages.itemCmdUnknownEnchantment(), Map.of(
                            "ENCHANTMENT", args[3]
                    ));
                    return;
                }

                if (!enchantments.containsKey(enchantment)) {
                    Messenger.sendMessage(player, cmdMessages.itemCmdEnchantmentNotPresent(), Map.of(
                            "ENCHANTMENT", args[3]
                    ));
                    return;
                }

                meta.removeEnchant(enchantment);
                item.setItemMeta(meta);

                Messenger.sendMessage(player, cmdMessages.itemCmdEnchantmentRemoved());
            }

            case "clear" -> {
                if (args.length != 3) {
                    Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                            "USAGE", "/cim item enchantments clear")
                    );
                    return;
                }

                meta.removeEnchantments();
                item.setItemMeta(meta);

                Messenger.sendMessage(player, cmdMessages.itemCmdEnchantmentsCleared());
            }

            default -> Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                    "USAGE", "\n/cim item enchantments set <enchantment> <level>\n/cim item enchantments remove <enchantment>\n/cim item enchantments clear")
            );
        }
    }

    private void handleItemAttributes(Player player, String[] args, ItemStack item, ItemMeta meta) {

    }

    private void handleItemKeys(Player player, String[] args, ItemStack item, ItemMeta meta) {
        if (args.length != 4) {
            Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                    "USAGE", "\n/cim item keys add <key>\n/cim item keys remove <key>")
            );
            return;
        }

        final String option = args[2]
                .strip()
                .toLowerCase(Locale.ROOT);

        final NamespacedKey key = NamespacedKey.fromString(args[3].strip().toLowerCase(Locale.ROOT), plugin);

        if (key == null) {
            Messenger.sendMessage(player, cmdMessages.itemCmdInvalidKey(), Map.of(
                    "KEY", args[3])
            );
            return;
        }

        final var container = meta.getPersistentDataContainer();

        switch (option) {
            case "add" -> {
                if (container.has(key, PersistentDataType.BOOLEAN)) {
                    Messenger.sendMessage(player, cmdMessages.itemCmdDuplicateKey(), Map.of(
                            "KEY", args[3])
                    );
                    return;
                }

                container.set(key, PersistentDataType.BOOLEAN, true);
                item.setItemMeta(meta);

                Messenger.sendMessage(player, cmdMessages.itemCmdKeyAdded());
            }

            case "remove" -> {
                if (!container.has(key, PersistentDataType.BOOLEAN)) {
                    Messenger.sendMessage(player, cmdMessages.itemCmdKeyNotFound(), Map.of(
                            "KEY", args[3])
                    );
                    return;
                }

                container.remove(key);
                item.setItemMeta(meta);

                Messenger.sendMessage(player, cmdMessages.itemCmdKeyRemoved());
            }

            default -> Messenger.sendMessage(player, sharedMessages.invalidArguments(), Map.of(
                    "USAGE", "\n/cim item keys add <key>\n/cim item keys remove <key>")
            );
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 2)
            return List.of("name", "lore", "model", "enchantments", "attributes", "keys");

        if (!(sender instanceof final Player player))
            return List.of();

        final ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir())
            return List.of();

        final ItemMeta meta = item.getItemMeta() == null
                ? Bukkit.getItemFactory().getItemMeta(item.getType())
                : item.getItemMeta();

        return switch (args[1].strip().toLowerCase(Locale.ROOT)) {
            case "name" -> handleItemNameTab(args);
            case "lore" -> handleItemLoreTab(args, meta);
            case "model" -> handleItemModelTab(args);
            case "enchantments" -> handleItemEnchantsTab(args, meta);
            case "attributes" -> handleItemAttributesTab(args, meta);
            case "keys" -> handleItemKeysTab(args, item);
            default -> List.of();
        };
    }

    private List<String> handleItemNameTab(String[] args) {
        if (args.length == 3)
            return List.of("set", "clear");

        return List.of();
    }

    private List<String> handleItemLoreTab(String[] args, ItemMeta meta) {
        if (args.length < 3 || args.length > 5)
            return List.of();

        if (args.length == 3)
            return List.of("add", "set", "remove", "clear");

        final List<Component> lore = getItemLore(meta);

        if (lore.isEmpty())
            return List.of();

        final String option = args[2]
                .strip()
                .toLowerCase(Locale.ROOT);

        if (args.length == 4 && (option.equals("set") || option.equals("remove")))
            return IntStream.rangeClosed(1, lore.size() + 1)
                    .boxed()
                    .map(String::valueOf)
                    .toList();

        if (args.length == 5 && option.equals("set")) {
            final int lineIndex;

            try {
                lineIndex = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                return List.of();
            }

            if (lineIndex > lore.size())
                return List.of();

            return List.of(miniMessage.serialize(lore.get(lineIndex)));
        }

        return List.of();
    }

    private List<String> handleItemModelTab(String[] args) {
        if (args.length == 3)
            return List.of("set", "clear");

        return List.of();
    }

    private List<String> handleItemEnchantsTab(String[] args, ItemMeta meta) {
        if (args.length == 3)
            return List.of("set", "remove", "clear");

        final String option = args[3]
                .trim()
                .toLowerCase(Locale.ROOT);

        if (args.length == 4 && option.equals("set"))
            return RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.ENCHANTMENT)
                    .stream()
                    .map(Enchantment::getKey)
                    .map(NamespacedKey::getKey)
                    .toList();

        if (args.length == 4 && option.equals("remove"))
            return meta.getEnchants().keySet()
                    .stream()
                    .map(Enchantment::getKey)
                    .map(NamespacedKey::getKey)
                    .toList();

        if (args.length == 5 && option.equals("set"))
            return IntStream.rangeClosed(1, 255)
                    .boxed()
                    .map(String::valueOf)
                    .toList();

        return List.of();
    }

    private List<String> handleItemAttributesTab(String[] args, ItemMeta meta) {
        return List.of();
    }

    private List<String> handleItemKeysTab(String[] args, ItemStack item) {
        if (args.length == 3)
            return List.of("add", "remove", "clear");

        if (args.length == 4 && args[2].strip().toLowerCase(Locale.ROOT).equals("remove"))
            return item.getPersistentDataContainer()
                    .getKeys()
                    .stream()
                    .map(NamespacedKey::asString)
                    .toList();

        return List.of();
    }

    private List<Component> getItemLore(ItemMeta meta) {
        if (!meta.hasLore()) return new ArrayList<>();
        return meta.lore();
    }
}
