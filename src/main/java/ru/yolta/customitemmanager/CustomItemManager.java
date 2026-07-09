package ru.yolta.customitemmanager;

import org.apache.logging.log4j.Level;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import ru.yolta.customitemmanager.api.CustomItemManagerApi;
import ru.yolta.customitemmanager.command.CommandService;
import ru.yolta.customitemmanager.command.CustomItemManagerCommand;
import ru.yolta.customitemmanager.config.ConfigManager;
import ru.yolta.customitemmanager.storage.CustomItemBuilder;
import ru.yolta.customitemmanager.storage.CustomItemStorage;
import ru.yolta.customitemmanager.utils.GuideWriter;
import ru.yolta.customitemmanager.utils.Logger;
import ru.yolta.customitemmanager.utils.Messenger;
import ru.yolta.customitemmanager.utils.UpdateChecker;

public class CustomItemManager extends JavaPlugin {

    public static final String PLUGIN_NAME = "CustomItemManager";
    public static final String PLUGIN_PREFIX = "CIM";
    private static CustomItemManagerApi api;
    
    @Override
    public void onEnable() {
        Logger.info(this, "Loading up...");

        final ConfigManager configManager = new ConfigManager(this);

        Logger.setLevel(configManager.getGeneralConfig().loggingLevel());
        Messenger.setPrefix(configManager.getMessageConfig().prefix());

        final CustomItemStorage itemStorage = new CustomItemStorage(this);
        final CustomItemBuilder itemBuilder = new CustomItemBuilder(itemStorage);

        api = new CustomItemManagerApi(this, itemStorage, itemBuilder);

        final CommandService commandService = new CommandService(configManager.getMessageConfig());
        final CustomItemManagerCommand commandHandler = new CustomItemManagerCommand(commandService, configManager.getMessageConfig());

        final PluginCommand command = this.getCommand("customitemmanager");
        if (command == null) throw new IllegalStateException("Command '%s' not found in plugin.yml".formatted("customitemmanager"));

        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);

        GuideWriter.ensureGuidesExist(this);

        checkUpdates();

        Logger.info(this, "Loaded successfully.");
    }

    private void checkUpdates() {
        final UpdateChecker.UpdateCheckResult result = UpdateChecker.checkUpdates(getPluginMeta().getVersion());
        if (!result.hasUpdate()) {
            // log all good
            return;
        }

        // Say you are not up-to-date.
    }

    public void onReload() {
        onDisable();
        onEnable();
    }

    @Override
    public void onDisable() {
        Logger.info(this, "Shutting down...");

        Messenger.setPrefix("");

        api = null;

        Logger.info(this, "Shut down successfully.");

        Logger.setLevel(Level.INFO);
    }

    public static @NotNull CustomItemManagerApi getApi() {
        if (api == null) 
            throw new IllegalStateException("Accessed API before it was initialized.");
        return api;
    }
}
