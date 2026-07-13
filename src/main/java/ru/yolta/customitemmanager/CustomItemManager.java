package ru.yolta.customitemmanager;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import ru.yolta.customitemmanager.api.CustomItemManagerApi;
import ru.yolta.customitemmanager.command.CommandService;
import ru.yolta.customitemmanager.command.CustomItemManagerCommand;
import ru.yolta.customitemmanager.config.ConfigManager;
import ru.yolta.customitemmanager.storage.CustomItemBuilder;
import ru.yolta.customitemmanager.storage.CustomItemStorage;
import ru.yolta.customitemmanager.utils.Future;
import ru.yolta.customitemmanager.utils.Logger;
import ru.yolta.customitemmanager.utils.Messenger;
import ru.yolta.customitemmanager.utils.UpdateChecker;

public final class CustomItemManager extends JavaPlugin {

    public static final String PLUGIN_NAME = "CustomItemManager";
    private static CustomItemManagerApi api;
    
    @Override
    public void onEnable() {
        Logger.info(PLUGIN_NAME, "Hello! Loading up...");

        final var configManager = new ConfigManager(this);

        Future.setPlugin(this);
        Messenger.setPrefix(configManager.getMessageConfig().prefix());

        final var itemStorage = new CustomItemStorage(this);
        final var itemBuilder = new CustomItemBuilder(itemStorage);

        api = new CustomItemManagerApi(this, itemStorage, itemBuilder);

        final var commandService = new CommandService(configManager.getMessageConfig());
        final var commandHandler = new CustomItemManagerCommand(commandService, configManager.getMessageConfig());

        final PluginCommand command = this.getCommand("customitemmanager");
        if (command == null) throw new IllegalStateException("Failed to find command in 'plugin.yml': customitemmanager");

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

        final PluginCommand command = this.getCommand("customitemmanager");
        if (command == null) throw new IllegalStateException("Failed to find command in 'plugin.yml': customitemmanager");

        command.setExecutor(null);
        command.setTabCompleter(null);

        Messenger.setPrefix("");
        Future.setPlugin(null);

        api = null;

        Logger.info(PLUGIN_NAME, "Shut down successfully. Bye!");
    }

    public static @NotNull CustomItemManagerApi getApi() {
        if (api == null) 
            throw new IllegalStateException("Accessed API before it was initialized.");
        return api;
    }
}
