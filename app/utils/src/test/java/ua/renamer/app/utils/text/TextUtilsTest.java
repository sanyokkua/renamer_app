package ua.renamer.app.utils.text;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TextUtilsTest {

    static Stream<Arguments> capitalizeArguments() {
        return Stream.of(
                arguments("", ""),
                arguments("  ", "  "),
                arguments("hello", "Hello"),
                arguments("java", "Java"),
                arguments("JUnit", "Junit"),
                arguments("parameterized", "Parameterized"),
                arguments("pARAMETERIZED", "Parameterized")
        );
    }

    static Stream<Arguments> getFileNewFullNameArguments() {
        return Stream.of(
                arguments("file", ".txt", "file.txt"),
                arguments("file", "txt", "file.txt"),
                arguments("file", "", "file"),
                arguments("file", null, "file"),
                arguments("file", "  ", "file"),
                arguments("file", ".jpg", "file.jpg"),
                arguments("file", "jpg", "file.jpg"),
                arguments("document", ".pdf", "document.pdf"),
                arguments("", ".txt", ".txt"),
                arguments("file", ".tar.gz", "file.tar.gz")
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\n", "\t"})
    void testIsEmptyWithNullAndWhitespaceReturnsTrue(String value) {
        assertTrue(TextUtils.isEmpty(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "test"})
    void testIsEmptyWithNonEmptyStringsReturnsFalse(String value) {
        assertFalse(TextUtils.isEmpty(value));
    }

    @ParameterizedTest
    @MethodSource("capitalizeArguments")
    void testCapitalize(String actualValue, String expectedValue) {
        var result = TextUtils.capitalize(actualValue);

        assertEquals(expectedValue, result);
    }

    @Test
    void testCapitalizeWithNull() {
        assertNull(TextUtils.capitalize(null));
    }

    @ParameterizedTest
    @MethodSource("getFileNewFullNameArguments")
    void testGetFileNewFullName(String name, String ext, String expected) {
        var result = TextUtils.getFileNewFullName(name, ext);

        assertEquals(expected, result);
    }

}
