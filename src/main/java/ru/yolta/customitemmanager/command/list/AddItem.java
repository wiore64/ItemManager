package ru.yolta.customitemmanager.command.list;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.Unmodifiable;
import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.command.SubCommand;
import ru.yolta.customitemmanager.config.MessageConfig;
import ru.yolta.customitemmanager.utils.Messenger;

public final class AddItem implements SubCommand {

    private static final Set<String> ALIASES = Set.of("add");
    private static final Permission PERMISSION = new Permission("customitemmanager.command.add");
    private final MessageConfig messages;

    public AddItem(@NotNull MessageConfig messages) {
        this.messages = messages;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (!(sender instanceof final Player player)) {
            Messenger.sendMessage(sender, messages.playerOnlyCommand());
            return;
        }
        
        if (args.length != 2) {
            Messenger.sendMessage(sender, messages.invalidArguments(), Map.of("USAGE", "/cim add <name>"));
            return;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType().isAir()) {
            Messenger.sendMessage(sender, messages.mustHoldItem());
            return;
        }

        final String itemName = args[1];

        final boolean success = CustomItemManager.getApi().registerCustomItem(itemName, item);

        if (success) {
            Messenger.sendMessage(sender, messages.itemRegistered(), Map.of("ITEM", itemName));
        } else {
            Messenger.sendMessage(sender, messages.itemAlreadyRegistered(), Map.of("ITEM", itemName));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }

    public static @NotNull @Unmodifiable Set<String> getAliases() {
        return ALIASES;
    }

    public static @NotNull Permission getPermission() {
        return PERMISSION;
    }
}
