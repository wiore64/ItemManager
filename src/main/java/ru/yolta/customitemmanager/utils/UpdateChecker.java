package ru.yolta.customitemmanager.utils;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Optional;

public final class UpdateChecker {

    private static final String LOG_NAME = "UpdateChecker";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/wiore64/CustomItemManager/releases/latest";

    public enum UpdateType {
        MAJOR,
        MINOR,
        PATCH,
        NONE
    }

    private UpdateChecker() {}

    public static UpdateCheckResult checkUpdates(@NotNull String currStrVer) {
        final Optional<String> optionalRelease = fetchLatestRelease();
        if (optionalRelease.isEmpty()) return UpdateCheckResult.updateNotFound();

        final Optional<SemVer> optionalLastSemVer = extractReleaseVersion(optionalRelease.get());
        if (optionalLastSemVer.isEmpty()) return UpdateCheckResult.updateNotFound();

        final Optional<SemVer> optionalCurrSemVer = SemVer.fromString(currStrVer);
        if (optionalCurrSemVer.isEmpty()) return UpdateCheckResult.updateNotFound();

        return compareVersions(optionalCurrSemVer.get(), optionalLastSemVer.get());
    }

    private static Optional<String> fetchLatestRelease() {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GITHUB_API_URL))
                .header("X-GitHub-Api-Version", "2026-03-10")
                .GET()
                .build();

        try (final HttpClient client = HttpClient.newHttpClient()) {
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                Logger.error(LOG_NAME, "Failed to fetch latest release. Status code: {}.", response.statusCode());
                return Optional.empty();
            }

            return Optional.of(response.body());
        } catch (IOException | InterruptedException e) {
            Logger.error(LOG_NAME, "Failed to fetch latest release:", e);
            return Optional.empty();
        }
    }

    private static Optional<SemVer> extractReleaseVersion(String release) {
        final int tagKeyIndex = release.indexOf("\"tag_name\":");

        if (tagKeyIndex == -1) {
            Logger.error(LOG_NAME, "Failed to extract tag name from latest release: No tag key found.");
            return Optional.empty();
        }

        final int tagFirstIndex = release.indexOf("v", tagKeyIndex + "\"tag_name\":".length() + 1) + 1;
        final int tagLastIndex = release.indexOf(",", tagKeyIndex) - 1;

        return SemVer.fromString(release.substring(tagFirstIndex, tagLastIndex));
    }

    private static UpdateCheckResult compareVersions(SemVer currVer, SemVer lastVer) {
        if (currVer.currMajVer() < lastVer.currMajVer())
            return UpdateCheckResult.updateAvailable(currVer, lastVer, UpdateType.MAJOR);

        if (currVer.currMinVer() < lastVer.currMinVer())
            return UpdateCheckResult.updateAvailable(currVer, lastVer, UpdateType.MINOR);

        if (currVer.currPatch() < lastVer.currPatch())
            return UpdateCheckResult.updateAvailable(currVer, lastVer, UpdateType.PATCH);

        return UpdateCheckResult.updateNotFound();
    }

    public record SemVer(int currMajVer, int currMinVer, int currPatch) {

        private static Optional<SemVer> fromString(String strNum) {
            final String[] splitStrNum = strNum.split("\\.");

            if (splitStrNum.length != 3) {
                Logger.error(LOG_NAME, "Failed to parse '{}' to SemVer: Invalid format.", strNum);
                return Optional.empty();
            }

            return fromArray(new int[] {
                    parseInteger(splitStrNum[0]),
                    parseInteger(splitStrNum[1]),
                    parseInteger(splitStrNum[2])
            });
        }

        private static Optional<SemVer> fromArray(int[] arr) {
            if (arr.length != 3) {
                Logger.error(LOG_NAME, "Failed to parse '{}' to SemVer: Invalid format.", Arrays.toString(arr));
                return Optional.empty();
            }

            if (arr[0] == -1 || arr[1] == -1 || arr[2] == -1) {
                Logger.error(LOG_NAME, "Failed to parse '{}' to SemVer: One of identifiers is missing.", Arrays.toString(arr));
                return Optional.empty();
            }

            return Optional.of(new SemVer(arr[0], arr[1], arr[2]));
        }

        private static SemVer empty() {
            return new SemVer(0, 0, 0);
        }

        private static int parseInteger(String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                Logger.error(LOG_NAME, "Failed to parse '{}' to identifier.", s, e);
                return -1;
            }
        }

        @Override
        public @NonNull String toString() {
            return currMajVer + "." + currMinVer + "." + currPatch;
        }
    }

    public record UpdateCheckResult(
            boolean hasUpdate,
            @NotNull SemVer currVer,
            @NotNull SemVer lastVer,
            @NotNull UpdateType type
    ) {

        private static UpdateCheckResult updateNotFound() {
            return new UpdateCheckResult(false, SemVer.empty(), SemVer.empty(), UpdateType.NONE);
        }

        private static UpdateCheckResult updateAvailable(SemVer currVer, SemVer lastVer, UpdateType type) {
            return new UpdateCheckResult(true, currVer, lastVer, type);
        }
    }
}
