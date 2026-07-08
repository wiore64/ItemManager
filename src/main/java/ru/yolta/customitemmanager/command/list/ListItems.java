package ru.yolta.customitemmanager.command.list;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.Unmodifiable;
import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.command.SubCommand;
import ru.yolta.customitemmanager.config.MessageConfig;
import ru.yolta.customitemmanager.utils.Messenger;

public final class ListItems implements SubCommand {

    private static final Set<String> ALIASES = Set.of("list");
    private static final Permission PERMISSION = new Permission("customitemmanager.command.list");
    private final MessageConfig messages;

    public ListItems(@NotNull MessageConfig messages) {
        this.messages = messages;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length != 1) {
            Messenger.sendMessage(sender, messages.invalidArguments(), Map.of("USAGE", "/cim list"));
            return;
        }

        final Set<String> itemIds = CustomItemManager.getApi().getAllCustomItemIds();

        if (itemIds.isEmpty()) {
            Messenger.sendMessage(sender, messages.itemList(), Map.of("ITEMS", "No custom items registered."));
            return;
        }

        final String itemList = String.join(", ", itemIds);
        
        Messenger.sendMessage(sender, messages.itemList(), Map.of("ITEMS", itemList));
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
