package ru.yolta.customitemmanager;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import ru.yolta.customitemmanager.api.ItemManagerApi;
import ru.yolta.customitemmanager.command.CommandService;
import ru.yolta.customitemmanager.command.CustomItemManagerCommand;
import ru.yolta.customitemmanager.config.ConfigManager;
import ru.yolta.customitemmanager.storage.CustomItemBuilder;
import ru.yolta.customitemmanager.storage.CustomItemStorage;
import ru.yolta.customitemmanager.utils.Logger;
import ru.yolta.customitemmanager.utils.UpdateChecker;

public class CustomItemManager extends JavaPlugin {

    private static final String PLUGIN_FULL_NAME = "CustomItemManager";
    private static final String PLUGIN_SHORT_NAME = "CIM";
    private static final String MAIN_COMMAND_NAME = "customitemmanager";
    private static final String PLUGIN_DOWNLOAD_LINK = "https://hangar.papermc.io/randomlychosenname/ItemManager/versions";
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
        final CustomItemManagerCommand commandHandler = new CustomItemManagerCommand(commandService, configManager.getMessageConfig());

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

    public static @NotNull String getPrefix()   {
        return PLUGIN_SHORT_NAME;
    }

    public static @NotNull String getPluginFullName() {
        return PLUGIN_FULL_NAME;
    }

    public static @NotNull ItemManagerApi getApi() {
        if (api == null) 
            throw new IllegalStateException("Accessed API before it was initialized.");
        return api;
    }
}
