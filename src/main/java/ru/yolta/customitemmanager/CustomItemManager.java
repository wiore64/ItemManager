package ru.yolta.customitemmanager;

import org.apache.logging.log4j.Level;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import ru.yolta.customitemmanager.api.CustomItemManagerApi;
import ru.yolta.customitemmanager.command.CommandService;
import ru.yolta.customitemmanager.command.CustomItemManagerCommand;
import ru.yolta.customitemmanager.config.ConfigProvider;
import ru.yolta.customitemmanager.storage.CustomItemBuilder;
import ru.yolta.customitemmanager.storage.CustomItemStorage;
import ru.yolta.customitemmanager.utils.Logger;
import ru.yolta.customitemmanager.utils.Messenger;
import ru.yolta.customitemmanager.utils.UpdateChecker;

public class CustomItemManager extends JavaPlugin {

    public static final String PLUGIN_NAME = "CustomItemManager";
    public static final String PLUGIN_PREFIX = "CIM";
    private static CustomItemManagerApi api;
    
    @Override
    public void onEnable() {
        Logger.info(PLUGIN_NAME, "Loading up...");

        final ConfigProvider configProvider = new ConfigProvider(this);

        Logger.setLevel(configProvider.getGeneralConfig().loggingLevel());
        Messenger.setPrefix(configProvider.getMessageConfig().prefix());

        final CustomItemStorage itemStorage = new CustomItemStorage(this);
        final CustomItemBuilder itemBuilder = new CustomItemBuilder(itemStorage);

        api = new CustomItemManagerApi(this, itemStorage, itemBuilder);

        final CommandService commandService = new CommandService(configProvider.getMessageConfig());
        final CustomItemManagerCommand commandHandler = new CustomItemManagerCommand(commandService, configProvider.getMessageConfig());

        final PluginCommand command = this.getCommand("customitemmanager");
        if (command == null) throw new IllegalStateException("Command '%s' not found in plugin.yml".formatted("customitemmanager"));

        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);

        checkUpdates();

        Logger.info(PLUGIN_NAME, "Loaded successfully.");
    }

    private void checkUpdates() {
        final UpdateChecker.UpdateCheckResult result = UpdateChecker.checkUpdates(getPluginMeta().getVersion());

        if (!result.hasUpdate()) {
            Logger.info(PLUGIN_NAME, "You're up to date!");
            return;
        }

        switch (result.type()) {
            case MAJOR -> Logger.info(PLUGIN_NAME,
                    "You're missing a major update! Your version: {}; latest version: {}. Download link: {}",
                    result.currVer(), result.lastVer(), getPluginMeta().getWebsite());

            case MINOR -> Logger.info(PLUGIN_NAME,
                    "You're missing a minor update! Your version: {}; latest version: {}. Download link: {}",
                    result.currVer(), result.lastVer(), getPluginMeta().getWebsite());

            case PATCH -> Logger.info(PLUGIN_NAME,
                    "You're missing a patch! Your version: {}; Latest version: {}. Download link: {}",
                    result.currVer(), result.lastVer(), getPluginMeta().getWebsite());

            default -> throw new IllegalArgumentException("Invalid update type: " + result.type());
        }
    }

    public void onReload() {
        onDisable();
        onEnable();
    }

    @Override
    public void onDisable() {
        Logger.info(PLUGIN_NAME, "Shutting down...");

        Messenger.setPrefix("");

        api = null;

        Logger.info(PLUGIN_NAME, "Shut down successfully.");

        Logger.setLevel(Level.INFO);
    }

    public static @NotNull CustomItemManagerApi getApi() {
        if (api == null) 
            throw new IllegalStateException("Accessed API before it was initialized.");
        return api;
    }
}
