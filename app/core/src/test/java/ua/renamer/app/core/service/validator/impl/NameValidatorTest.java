package ua.renamer.app.core.service.validator.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for {@link NameValidator}.
 *
 * <p>The validator enforces three rule sets:
 * <ol>
 *   <li>Null / empty input → invalid
 *   <li>Common cross-platform restricted characters (":", "/")
 *   <li>Windows-specific restrictions (backslash, wildcards, reserved names)
 *       — only active when {@code os.name} contains "win"
 *   <li>Platform path-validity check via {@code FileSystems.getDefault().getPath()}
 * </ol>
 *
 * <p>Because the Windows-specific branches only execute on a Windows host, those
 * tests that rely on the OS check carry a comment explaining the expected outcome
 * per OS and do NOT use platform branching inside the test body itself.
 */
class NameValidatorTest {

    private final NameValidator validator = new NameValidator();

    // =========================================================================
    // Null and empty input
    // =========================================================================

    @Nested
    class NullAndEmptyInputTests {

        @ParameterizedTest
        @NullAndEmptySource
        void isValid_whenNullOrEmpty_returnsFalse(String input) {
            assertThat(validator.isValid(input)).isFalse();
        }
    }

    // =========================================================================
    // Common restricted characters (cross-platform: ":" and "/")
    // =========================================================================

    @Nested
    class CommonRestrictedCharacterTests {

        @ParameterizedTest
        @CsvSource({
                "file:name,  false, colon in name",
                "path/file,  false, slash in name",
                "file:,      false, trailing colon",
                ":file,      false, leading colon",
                "a/b/c,      false, multiple slashes",
        })
        void isValid_withRestrictedChar_returnsFalse(String input, boolean expectedValid, String reason) {
            assertThat(validator.isValid(input))
                    .as("'%s' should be %s (%s)", input, expectedValid ? "valid" : "invalid", reason)
                    .isEqualTo(expectedValid);
        }

        @Test
        void isValid_withOnlyColon_returnsFalse() {
            assertThat(validator.isValid(":")).isFalse();
        }

        @Test
        void isValid_withOnlySlash_returnsFalse() {
            assertThat(validator.isValid("/")).isFalse();
        }
    }

    // =========================================================================
    // Valid names (cross-platform)
    // =========================================================================

    @Nested
    class ValidNameTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "document",
                "photo.jpg",
                "my file",
                "file_with_underscores",
                "file-with-dashes",
                "file.with.dots",
                "File123",
                "UPPERCASE",
                "mixedCase",
                "a",
        })
        void isValid_withPlainName_returnsTrue(String name) {
            // On non-Windows systems these are always valid path components.
            // On Windows they must also pass the Windows-specific checks, which
            // these names all satisfy (no reserved names, no restricted chars).
            assertThat(validator.isValid(name)).isTrue();
        }

        @Test
        void isValid_withSingleCharacter_returnsTrue() {
            assertThat(validator.isValid("x")).isTrue();
        }

        @Test
        void isValid_withSpacesInMiddle_returnsTrue() {
            assertThat(validator.isValid("my document")).isTrue();
        }

        @Test
        void isValid_withUnicode_returnsTrue() {
            assertThat(validator.isValid("测试文件")).isTrue();
        }

        @Test
        void isValid_withEmojiName_doesNotThrow() {
            // Emoji in filenames are unusual but the validator must never throw;
            // outcome depends on whether the platform FileSystems accepts them.
            assertThatCode(() -> validator.isValid("photo📷")).doesNotThrowAnyException();
        }

        @Test
        void isValid_withLeadingDot_returnsTrue() {
            // Hidden files on Unix start with a dot; the validator must accept them.
            assertThat(validator.isValid(".hidden")).isTrue();
        }

        @Test
        void isValid_withTrailingDot_doesNotThrow() {
            // Trailing dots are rejected by Windows; on other platforms they may be valid.
            // The important contract is that the method never throws.
            assertThatCode(() -> validator.isValid("name.")).doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // No-throw contract
    // =========================================================================

    @Nested
    class NoThrowContractTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "normal",
                "a:b",
                "a/b",
                "\u0000control",   // null byte — triggers InvalidPathException on most platforms
        })
        void isValid_neverThrowsForAnyInput(String input) {
            assertThatCode(() -> validator.isValid(input)).doesNotThrowAnyException();
        }

        @Test
        void isValid_nullInput_neverThrows() {
            assertThatCode(() -> validator.isValid(null)).doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // InvalidPathException path — characters that break FileSystems.getPath()
    // =========================================================================

    @Nested
    class InvalidPathCharacterTests {

        @Test
        void isValid_withNullByteCharacter_returnsFalse() {
            // A null byte (\u0000) causes FileSystems.getDefault().getPath() to throw
            // InvalidPathException on all standard JVM implementations (Linux, macOS, Windows).
            // The validator must catch the exception and return false.
            assertThat(validator.isValid("file\u0000name")).isFalse();
        }
    }

    // =========================================================================
    // Windows-specific checks (active only when os.name contains "win")
    // These tests document the expected behaviour; actual branch execution only
    // occurs on a Windows host.  On all other platforms isValid() returns true
    // for these inputs because the Windows checks are skipped.
    // =========================================================================

    @Nested
    class WindowsSpecificTests {

        /**
         * On Windows these characters are restricted: \, *, ?, <, >, |.
         * On non-Windows platforms the validator does NOT check for them,
         * so the expected result is platform-dependent.
         * The test documents the contract without asserting a fixed value —
         * it only asserts that isValid() does not throw.
         */
        @ParameterizedTest
        @ValueSource(strings = {"file*name", "file?name", "file<name", "file>name", "file|name", "file\\name"})
        void isValid_withWindowsRestrictedChar_doesNotThrow(String name) {
            assertThatCode(() -> validator.isValid(name)).doesNotThrowAnyException();
        }

        /**
         * On Windows the reserved names (CON, PRN, AUX, NUL, COM1-9, LPT1-9) are invalid.
         * On non-Windows the check is skipped and the name is valid.
         * This test only verifies no exception is thrown (contract applies on all platforms).
         */
        @ParameterizedTest
        @ValueSource(strings = {"CON", "PRN", "AUX", "NUL", "COM1", "COM9", "LPT1", "LPT9"})
        void isValid_withWindowsReservedName_doesNotThrow(String name) {
            assertThatCode(() -> validator.isValid(name)).doesNotThrowAnyException();
        }

        /**
         * On Windows the reserved name check is case-insensitive.
         * This test documents the case-insensitivity contract without depending on
         * being run on Windows.
         */
        @ParameterizedTest
        @ValueSource(strings = {"con", "prn", "nul", "aux", "com1", "lpt1"})
        void isValid_withWindowsReservedNameLowercase_doesNotThrow(String name) {
            assertThatCode(() -> validator.isValid(name)).doesNotThrowAnyException();
        }
    }
}
