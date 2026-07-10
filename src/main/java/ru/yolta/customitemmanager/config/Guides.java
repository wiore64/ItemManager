package ru.yolta.customitemmanager.config;

import org.jetbrains.annotations.NotNull;
import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.utils.Logger;

import java.io.File;

final class Guides {

    private static final String GUIDES_FOLDER_NAME = "guides";
    private static final String ITEMS_GUIDE_FILE_NAME = "guides/items.md";
    private static final String PLACEHOLDERS_GUIDE_FILE_NAME = "guides/placeholders.md";

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

        final File itemsGuideFile = new File(folder, ITEMS_GUIDE_FILE_NAME);
        plugin.saveResource(itemsGuideFile.getPath(), true);

        final File placeholdersGuideFile = new File(folder, PLACEHOLDERS_GUIDE_FILE_NAME);
        plugin.saveResource(placeholdersGuideFile.getPath(), true);
    }
}
