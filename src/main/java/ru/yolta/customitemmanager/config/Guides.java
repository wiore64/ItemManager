package ru.yolta.customitemmanager.config;

import org.jetbrains.annotations.NotNull;
import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.utils.Logger;

import java.io.File;

final class Guides {

    private static final String GUIDES_FOLDER_NAME = "guides";
    private static final String ITEMS_GUIDE_FILE_NAME = GUIDES_FOLDER_NAME + "/items.md";
    private static final String PLACEHOLDERS_GUIDE_FILE_NAME = GUIDES_FOLDER_NAME + "/placeholders.md";

    private Guides() {}

    static void overwriteGuides(@NotNull CustomItemManager plugin, @NotNull ConfigProvider provider) {
        final File folder = new File(plugin.getDataFolder(), GUIDES_FOLDER_NAME);

        if (!folder.exists()) {
            final boolean result = folder.mkdirs();

            if (!result) {
                Logger.error(Guides.class, "Failed to create a folder for guides.");
                return;
            }
        }

        plugin.saveResource(ITEMS_GUIDE_FILE_NAME, true);
        plugin.saveResource(PLACEHOLDERS_GUIDE_FILE_NAME, true);
    }
}
