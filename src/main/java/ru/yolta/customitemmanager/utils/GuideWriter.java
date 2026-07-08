package ru.yolta.customitemmanager.utils;

import org.jetbrains.annotations.NotNull;
import ru.yolta.customitemmanager.CustomItemManager;

import java.io.File;

public final class GuideWriter {

    private static final String ITEMS_GUIDE_FILE_NAME = "guides/items.md";
    private static final String PLACEHOLDERS_GUIDE_FILE_NAME = "guides/placeholders.md";
    private static File itemsGuideFile;
    private static File placeholdersGuideFile;

    private GuideWriter() {}

    public static void ensureGuidesExist(@NotNull CustomItemManager plugin) {
        initFiles(plugin);
        ensureFileExists(plugin, ITEMS_GUIDE_FILE_NAME, itemsGuideFile);
        ensureFileExists(plugin, PLACEHOLDERS_GUIDE_FILE_NAME, placeholdersGuideFile);
    }

    private static void initFiles(CustomItemManager plugin) {
        if (itemsGuideFile != null && placeholdersGuideFile != null) return;

        final File folder = new File(plugin.getDataFolder(), "guides");

        if (!folder.exists()) {
            boolean ignored = folder.mkdirs();
        }

        if (!folder.isDirectory()) {
            boolean ignored = folder.delete();
            boolean ignoredTwo = folder.mkdirs();
        }

        itemsGuideFile = new File(folder, ITEMS_GUIDE_FILE_NAME);
        placeholdersGuideFile = new File(folder, PLACEHOLDERS_GUIDE_FILE_NAME);
    }

    private static void ensureFileExists(CustomItemManager plugin, String fileName, File file) {
        if (!file.exists()) {
            plugin.saveResource(fileName, true);
        }
    }
}
