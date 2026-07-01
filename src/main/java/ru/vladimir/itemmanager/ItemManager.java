package ru.vladimir.itemmanager;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import ru.vladimir.itemmanager.api.ItemManagerApi;
import ru.vladimir.itemmanager.command.CommandService;
import ru.vladimir.itemmanager.command.ItemManagerCommand;
import ru.vladimir.itemmanager.config.ConfigManager;
import ru.vladimir.itemmanager.storage.CustomItemBuilder;
import ru.vladimir.itemmanager.storage.CustomItemStorage;
import ru.vladimir.itemmanager.utils.Logger;
import ru.vladimir.itemmanager.utils.UpdateChecker;

public final class ItemManager extends JavaPlugin {

    private static final String MAIN_COMMAND_NAME = "itemmanager";
    private static final String PLUGIN_DOWNLOAD_LINK = "https://github.com/randomlychosenname/ItemManager/releases/latest";
    private static ItemManagerApi api;
    
    @Override
    public void onEnable() {
        Logger.init(this.getComponentLogger());

        Logger.getInstance().info(this, "Loading up...");

        final ConfigManager configManager = new ConfigManager(this);

        Logger.getInstance().setLevel(configManager.getGeneralConfig().loggingLevel());

        final CustomItemStorage itemStorage = new CustomItemStorage(this);
        final CustomItemBuilder itemBuilder = new CustomItemBuilder(itemStorage);

        api = new ItemManagerApi(this, itemStorage, itemBuilder);

        final CommandService commandService = new CommandService(configManager.getMessageConfig());
        final ItemManagerCommand commandHandler = new ItemManagerCommand(commandService, configManager.getMessageConfig());

        final PluginCommand command = this.getCommand(MAIN_COMMAND_NAME);
        if (command == null) throw new IllegalStateException("Command '%s' not found in plugin.yml".formatted(MAIN_COMMAND_NAME));

        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);

        if (UpdateChecker.isUpToDate(this.getPluginMeta().getVersion())) {
            Logger.getInstance().info(this, "You're up to date!");
        } else {
            Logger.getInstance().warn(this, "A new version is available. Download it from here: %s".formatted(PLUGIN_DOWNLOAD_LINK));
        }

        Logger.getInstance().info(this, "Loaded successfully.");
    }

    public void onReload() {
        onDisable();
        onEnable();
    }

    @Override
    public void onDisable() {
        Logger.getInstance().info(this, "Shutting down...");

        api = null;

        Logger.getInstance().info(this, "Shut down successfully.");

        Logger.destroy();
    }

    public static @NotNull ItemManagerApi getApi() {
        if (api == null) 
            throw new IllegalStateException("Accessed API before it was initialized.");
        return api;
    }
}
