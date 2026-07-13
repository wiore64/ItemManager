package ru.yolta.customitemmanager.command.list;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.Unmodifiable;
import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.command.SubCommand;
import ru.yolta.customitemmanager.config.MessageConfig;
import ru.yolta.customitemmanager.utils.Future;
import ru.yolta.customitemmanager.utils.Messenger;

public final class RemoveItem implements SubCommand {

    private static final Set<String> ALIASES = Set.of("remove");
    private static final Permission PERMISSION = new Permission("customitemmanager.command.remove");
    private final MessageConfig messages;

    public RemoveItem(@NotNull MessageConfig messages) {
        this.messages = messages;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length != 2) {
            Messenger.sendMessage(sender, messages.invalidArguments(), Map.of("USAGE", "/cim remove <name>"));
            return;
        }

        final String itemName = args[1];

        Future.runAsyncTask(() -> CustomItemManager.getApi().unregisterCustomItem(itemName), success -> {
            if (success) {
                Messenger.sendMessage(sender, messages.itemUnregistered(), Map.of("ITEM", itemName));
            } else {
                Messenger.sendMessage(sender, messages.itemNotFound(), Map.of("ITEM", itemName));
            }
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 2) return List.copyOf(CustomItemManager.getApi().getAllCustomItemIds());

        return List.of();
    }

    public static @NotNull @Unmodifiable Set<String> getAliases() {
        return ALIASES;
    }

    public static @NotNull Permission getPermission() {
        return PERMISSION;
    }
}
