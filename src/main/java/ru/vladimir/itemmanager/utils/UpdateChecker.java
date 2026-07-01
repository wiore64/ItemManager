package ru.vladimir.itemmanager.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class UpdateChecker {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/randomlychosenname/ItemManager/releases/latest";
    private static final String TAG_NAME_KEY = "\"tag_name\":";

    private UpdateChecker() {}

    public static boolean isUpToDate(@NotNull String currentVersionString) {
        final String release = fetchLatestRelease();
        if (release == null) return false;

        final int currentVersion;

        try {
           currentVersion = Integer.parseInt(currentVersionString.replace(".", ""));
        } catch (NumberFormatException e) {
            Logger.getInstance().error(UpdateChecker.class, "Failed to parse current version: %s".formatted(currentVersionString));
            return false;
        }

        Logger.getInstance().info(UpdateChecker.class, "%d and %d".formatted(currentVersion, extractLatestVersion(release)));

        return currentVersion == extractLatestVersion(release);
    }

    private static String fetchLatestRelease() {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GITHUB_API_URL))
                .header("X-GitHub-Api-Version", "2026-03-10")
                .GET()
                .build();

        try (final HttpClient client = HttpClient.newHttpClient()) {
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                Logger.getInstance().error(UpdateChecker.class, "Failed to fetch latest release. Status code: %d.".formatted(response.statusCode()));
                return null;
            }

            return response.body();
        } catch (IOException | InterruptedException e) {
            Logger.getInstance().error(UpdateChecker.class, "Failed to fetch latest release.", e);
            return null;
        }
    }

    private static int extractLatestVersion(String release) {
        final int tagKeyIndex = release.indexOf(TAG_NAME_KEY);
        if (tagKeyIndex == -1) {
            Logger.getInstance().error(UpdateChecker.class, "Failed to extract tag name from latest release: No tag key found.");
            return -1;
        }

        final int tagFirstIndex = release.indexOf("v", tagKeyIndex + TAG_NAME_KEY.length() + 1) + 1;
        final int tagLastIndex = release.indexOf(",", tagKeyIndex) - 1;

        final String tagName = release.substring(tagFirstIndex, tagLastIndex).replace(".", "");

        try {
            return Integer.parseInt(tagName);
        } catch (NumberFormatException e) {
            Logger.getInstance().error(UpdateChecker.class, "Failed to parse tag name of latest release: %s.".formatted(tagName));
            return -1;
        }
    }
}