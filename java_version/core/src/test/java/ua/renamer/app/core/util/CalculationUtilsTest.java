package ua.renamer.app.core.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CalculationUtilsTest {

    static Stream<Arguments> toKilobytesArguments() {
        return Stream.of(arguments(-10, -0), arguments(-100, -0), arguments(-1000, -0), arguments(-1024, -1), arguments(-1500, -1), arguments(-2000, -1), arguments(-2048, -2), arguments(0, 0), arguments(10, 0), arguments(100, 0), arguments(1000, 0), arguments(1024, 1), arguments(1500, 1), arguments(2000, 1), arguments(2048, 2));
    }

    static Stream<Arguments> toMegabytesArguments() {
        return Stream.of(arguments(-10, -0), arguments(-100, -0), arguments(-1000, 0), arguments(-1024, 0), arguments(-1500, 0), arguments(-2000, 0), arguments(-2048, 0), arguments(-1048576, -1), arguments(-2097152, -2), arguments(0, 0), arguments(10, 0), arguments(100, 0), arguments(1000, 0), arguments(1024, 0), arguments(1500, 0), arguments(2000, 0), arguments(2048, 0), arguments(2097152, 2));
    }

    @ParameterizedTest
    @MethodSource("toKilobytesArguments")
    void toKilobytes(long actual, long expected) {
        var result = CalculationUtils.toKilobytes(actual);

        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("toMegabytesArguments")
    void toMegabytes(long actual, long expected) {
        var result = CalculationUtils.toMegabytes(actual);

        assertEquals(expected, result);
    }

}