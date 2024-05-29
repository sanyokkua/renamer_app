package ua.renamer.app.core.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class UtilsTest {

    static Stream<Arguments> findMinOrNullArguments() {
        var date20240529170510 = LocalDateTime.of(2024, 5, 29, 17, 5, 10);

        var date20240529170511 = LocalDateTime.of(2024, 5, 29, 17, 5, 11);
        var date20240529170509 = LocalDateTime.of(2024, 5, 29, 17, 5, 9);
        var date20200529170510 = LocalDateTime.of(2020, 5, 29, 17, 5, 10);
        var date20240529170512 = LocalDateTime.of(2024, 5, 29, 17, 5, 12);
        var date20240529170513 = LocalDateTime.of(2024, 5, 29, 17, 5, 13);
        var date20240529170514 = LocalDateTime.of(2024, 5, 29, 17, 5, 14);
        return Stream.of(
                arguments(null, null),
                arguments(null, new LocalDateTime[0]),
                arguments(date20240529170510, new LocalDateTime[]{date20240529170510}),
                arguments(date20240529170510, new LocalDateTime[]{date20240529170510, date20240529170511}),

                arguments(date20240529170509,
                          new LocalDateTime[]{date20240529170510, date20240529170509, date20240529170511}
                         ),

                arguments(date20240529170510,
                          new LocalDateTime[]{
                                  date20240529170510,
                                  date20240529170511,
                                  date20240529170512,
                                  date20240529170513,
                                  date20240529170514
                          }
                         ),
                arguments(date20240529170510,
                          new LocalDateTime[]{date20240529170510, null, date20240529170512, null, date20240529170514}
                         ),
                arguments(date20240529170510,
                          new LocalDateTime[]{date20240529170510, null, date20240529170512, null, null}
                         ),
                arguments(date20240529170510, new LocalDateTime[]{date20240529170510, null, null, null, null}),
                arguments(date20200529170510,
                          new LocalDateTime[]{
                                  date20240529170510,
                                  date20240529170511,
                                  date20240529170512,
                                  date20240529170513,
                                  date20240529170514,
                                  date20240529170510,
                                  date20200529170510
                          }
                         )
                        );
    }

    @ParameterizedTest
    @MethodSource("findMinOrNullArguments")
    void testFindMinOrNull(LocalDateTime expected, LocalDateTime[] actual) {
        var result = Utils.findMinOrNull(actual);
        assertEquals(expected, result);
    }

}