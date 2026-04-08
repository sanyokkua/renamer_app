package ua.renamer.app.backend.settings;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import ua.renamer.app.api.settings.AppDefaults;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the OS-dispatch logic inside
 * {@link SettingsServiceImpl#resolveAppDir()} (private, exercised via
 * {@link SettingsServiceImpl#getSettingsFilePath()}).
 *
 * <p>Strategy: manipulate {@code System.getProperty("os.name")} and the
 * relevant environment-variable substitutes. Because the JVM does not allow
 * setting environment variables at runtime, we subclass
 * {@link SettingsServiceImpl} to override the two env-var lookups that
 * {@code resolveAppDir} performs ({@code APPDATA} and {@code XDG_CONFIG_HOME}).
 *
 * <p>Each test saves and restores the original {@code os.name} and
 * {@code user.home} system properties so there is no inter-test leakage.
 */
class SettingsServiceResolveAppDirTest {

    // Saved system properties restored in @AfterEach
    private String savedOsName;
    private String savedUserHome;

    @BeforeEach
    void saveProperties() {
        savedOsName = System.getProperty("os.name");
        savedUserHome = System.getProperty("user.home");
    }

    @AfterEach
    void restoreProperties() {
        if (savedOsName != null) {
            System.setProperty("os.name", savedOsName);
        }
        if (savedUserHome != null) {
            System.setProperty("user.home", savedUserHome);
        }
    }

    // -------------------------------------------------------------------------
    // Testable subclass — lets us inject fake env values without touching the JVM
    // -------------------------------------------------------------------------

    /**
     * Subclass of {@link SettingsServiceImpl} that overrides
     * {@link #getSettingsFilePath()} to expose the real {@code resolveAppDir()}
     * logic (no path override), but replaces the two env-variable reads with
     * constructor-supplied values.
     */
    static final class EnvOverrideSettingsService extends SettingsServiceImpl {

        private final String fakeAppData;
        private final String fakeXdgConfigHome;

        /**
         * @param fakeAppData       value to return for {@code APPDATA}; {@code null} simulates unset
         * @param fakeXdgConfigHome value to return for {@code XDG_CONFIG_HOME}; {@code null} simulates unset
         */
        EnvOverrideSettingsService(String fakeAppData, String fakeXdgConfigHome) {
            super();
            this.fakeAppData = fakeAppData;
            this.fakeXdgConfigHome = fakeXdgConfigHome;
        }

        @Override
        protected String getEnv(String name) {
            return switch (name) {
                case "APPDATA" -> fakeAppData;
                case "XDG_CONFIG_HOME" -> fakeXdgConfigHome;
                default -> System.getenv(name);
            };
        }
    }

    // =========================================================================
    // macOS branch
    // =========================================================================

    @Nested
    @EnabledOnOs(OS.MAC) // path assertions use Unix separators (e.g. startsWith("/Users/..."))
    class MacOsBranch {

        @Test
        void getSettingsFilePath_onMac_containsLibraryApplicationSupport() {
            System.setProperty("os.name", "Mac OS X");
            System.setProperty("user.home", "/Users/testuser");

            SettingsServiceImpl service = new EnvOverrideSettingsService(null, null);
            Path path = service.getSettingsFilePath();

            assertThat(path.toString())
                    .contains("Library")
                    .contains("Application Support")
                    .contains(AppDefaults.APP_DIR_NAME)
                    .endsWith(AppDefaults.SETTINGS_FILE_NAME);
        }

        @Test
        void getSettingsFilePath_onMac_usesUserHome() {
            System.setProperty("os.name", "Mac OS X");
            System.setProperty("user.home", "/Users/testuser");

            SettingsServiceImpl service = new EnvOverrideSettingsService(null, null);
            Path path = service.getSettingsFilePath();

            assertThat(path.toString()).startsWith("/Users/testuser");
        }
    }

    // =========================================================================
    // Windows branch — APPDATA set
    // =========================================================================

    @Nested
    class WindowsBranchWithAppData {

        @Test
        void getSettingsFilePath_onWindows_withAppData_usesAppDataDir() {
            System.setProperty("os.name", "Windows 10");
            System.setProperty("user.home", "C:\\Users\\testuser");

            SettingsServiceImpl service =
                    new EnvOverrideSettingsService("C:\\Users\\testuser\\AppData\\Roaming", null);
            Path path = service.getSettingsFilePath();

            assertThat(path.toString())
                    .contains("AppData")
                    .contains(AppDefaults.APP_DIR_NAME)
                    .endsWith(AppDefaults.SETTINGS_FILE_NAME);
        }

        @Test
        void getSettingsFilePath_onWindows_withAppData_doesNotUseUserHome() {
            System.setProperty("os.name", "Windows 11");
            System.setProperty("user.home", "C:\\Users\\other");

            SettingsServiceImpl service =
                    new EnvOverrideSettingsService("C:\\AppData\\Custom", null);
            Path path = service.getSettingsFilePath();

            // Should start with the APPDATA value, not user.home
            assertThat(path.toString()).startsWith("C:\\AppData\\Custom");
        }
    }

    // =========================================================================
    // Windows branch — APPDATA null (fallback to user.home)
    // =========================================================================

    @Nested
    class WindowsBranchWithoutAppData {

        @Test
        void getSettingsFilePath_onWindows_withNullAppData_fallsBackToUserHome() {
            System.setProperty("os.name", "Windows 10");
            System.setProperty("user.home", "C:\\Users\\fallback");

            // APPDATA is null — implementation must fall back to user.home
            SettingsServiceImpl service = new EnvOverrideSettingsService(null, null);
            Path path = service.getSettingsFilePath();

            assertThat(path.toString())
                    .startsWith("C:\\Users\\fallback")
                    .contains(AppDefaults.APP_DIR_NAME)
                    .endsWith(AppDefaults.SETTINGS_FILE_NAME);
        }

        @Test
        void getSettingsFilePath_onWindows_withNullAppData_containsAppDirName() {
            System.setProperty("os.name", "Windows 10");
            System.setProperty("user.home", "C:\\Users\\fallback");

            SettingsServiceImpl service = new EnvOverrideSettingsService(null, null);
            Path path = service.getSettingsFilePath();

            assertThat(path.toString()).contains(AppDefaults.APP_DIR_NAME);
        }
    }

    // =========================================================================
    // Linux/other branch — XDG_CONFIG_HOME set
    // =========================================================================

    @Nested
    @EnabledOnOs(OS.LINUX) // path assertions use Unix separators (e.g. startsWith("/home/..."))
    class LinuxBranchWithXdg {

        @Test
        void getSettingsFilePath_onLinux_withXdgConfigHome_usesXdgDir() {
            System.setProperty("os.name", "Linux");
            System.setProperty("user.home", "/home/testuser");

            SettingsServiceImpl service =
                    new EnvOverrideSettingsService(null, "/home/testuser/.config-custom");
            Path path = service.getSettingsFilePath();

            assertThat(path.toString())
                    .startsWith("/home/testuser/.config-custom")
                    .contains(AppDefaults.APP_DIR_NAME.toLowerCase())
                    .endsWith(AppDefaults.SETTINGS_FILE_NAME);
        }

        @Test
        void getSettingsFilePath_onLinux_withXdgConfigHome_doesNotUseDefaultConfigDir() {
            System.setProperty("os.name", "Linux");
            System.setProperty("user.home", "/home/testuser");

            SettingsServiceImpl service =
                    new EnvOverrideSettingsService(null, "/custom/xdg");
            Path path = service.getSettingsFilePath();

            // Must NOT contain the ".config" default path segment
            assertThat(path.toString()).doesNotContain("/.config/");
        }
    }

    // =========================================================================
    // Linux/other branch — XDG_CONFIG_HOME null (fallback to ~/.config)
    // =========================================================================

    @Nested
    @EnabledOnOs(OS.LINUX) // path assertions use Unix separators (e.g. startsWith("/home/..."))
    class LinuxBranchWithoutXdg {

        @Test
        void getSettingsFilePath_onLinux_withNullXdg_fallsBackToDefaultConfig() {
            System.setProperty("os.name", "Linux");
            System.setProperty("user.home", "/home/testuser");

            SettingsServiceImpl service = new EnvOverrideSettingsService(null, null);
            Path path = service.getSettingsFilePath();

            assertThat(path.toString())
                    .contains("/.config/")
                    .contains(AppDefaults.APP_DIR_NAME.toLowerCase())
                    .endsWith(AppDefaults.SETTINGS_FILE_NAME);
        }

        @Test
        void getSettingsFilePath_onLinux_withNullXdg_usesUserHome() {
            System.setProperty("os.name", "Linux");
            System.setProperty("user.home", "/home/testuser");

            SettingsServiceImpl service = new EnvOverrideSettingsService(null, null);
            Path path = service.getSettingsFilePath();

            assertThat(path.toString()).startsWith("/home/testuser");
        }

        @Test
        void getSettingsFilePath_onLinux_appDirNameIsLowerCase() {
            System.setProperty("os.name", "Linux");
            System.setProperty("user.home", "/home/testuser");

            SettingsServiceImpl service = new EnvOverrideSettingsService(null, null);
            Path path = service.getSettingsFilePath();

            // Implementation uses APP_DIR_NAME.toLowerCase() on Linux/other
            assertThat(path.toString())
                    .contains(AppDefaults.APP_DIR_NAME.toLowerCase());
        }
    }

    // =========================================================================
    // No-throw contract — resolveAppDir must never throw regardless of OS
    // =========================================================================

    @Nested
    class NoThrowContract {

        @Test
        void getSettingsFilePath_onMac_neverThrows() {
            System.setProperty("os.name", "Mac OS X");
            SettingsServiceImpl service = new EnvOverrideSettingsService(null, null);
            assertThat(service.getSettingsFilePath()).isNotNull();
        }

        @Test
        void getSettingsFilePath_onWindows_withAppData_neverThrows() {
            System.setProperty("os.name", "Windows 10");
            SettingsServiceImpl service = new EnvOverrideSettingsService("C:\\AppData", null);
            assertThat(service.getSettingsFilePath()).isNotNull();
        }

        @Test
        void getSettingsFilePath_onWindows_withoutAppData_neverThrows() {
            System.setProperty("os.name", "Windows 10");
            SettingsServiceImpl service = new EnvOverrideSettingsService(null, null);
            assertThat(service.getSettingsFilePath()).isNotNull();
        }

        @Test
        void getSettingsFilePath_onLinux_withXdg_neverThrows() {
            System.setProperty("os.name", "Linux");
            SettingsServiceImpl service = new EnvOverrideSettingsService(null, "/xdg");
            assertThat(service.getSettingsFilePath()).isNotNull();
        }

        @Test
        void getSettingsFilePath_onLinux_withoutXdg_neverThrows() {
            System.setProperty("os.name", "Linux");
            SettingsServiceImpl service = new EnvOverrideSettingsService(null, null);
            assertThat(service.getSettingsFilePath()).isNotNull();
        }
    }
}
