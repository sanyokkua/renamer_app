package ua.renamer.app.core.service.command.impl.preparation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.core.TestUtilities;
import ua.renamer.app.core.enums.*;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.command.Command;
import ua.renamer.app.core.service.helper.DateTimeOperations;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static ua.renamer.app.core.TestUtilities.*;

@ExtendWith(MockitoExtension.class)
class DateTimeRenamePrepareInformationCommandTest extends BaseRenamePreparationCommandTest {

    @Spy
    DateTimeOperations dateTimeOperations;

    static Stream<Arguments> provideCommandArguments() {
        // TEST_FC_TIME_2005_10_12_12_00_05
        // TEST_FM_TIME_2010_09_12_09_00_00
        // TEST_CC_TIME_2005_09_12_12_00_00

        // @formatter:off
        LocalDateTime customDate = LocalDateTime.of(2024,6,1,11,11);
        return Stream.of(
                arguments("Name", "Name", ItemPositionWithReplacement.BEGIN, true, DateFormat.DO_NOT_USE_DATE, TimeFormat.DO_NOT_USE_TIME, DateTimeFormat.DATE_TIME_TOGETHER, DateTimeSource.FILE_CREATION_DATE, true, customDate, "_", false, false),
                arguments("Name", "20051012_Name", ItemPositionWithReplacement.BEGIN, true, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.DO_NOT_USE_TIME, DateTimeFormat.DATE_TIME_TOGETHER, DateTimeSource.FILE_CREATION_DATE, true, customDate, "_", false, false),
                arguments("Name", "120005_Name", ItemPositionWithReplacement.BEGIN, true, DateFormat.DO_NOT_USE_DATE, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_TOGETHER, DateTimeSource.FILE_CREATION_DATE, true, customDate, "_", false, false),
                arguments("Name", "20051012120005_Name", ItemPositionWithReplacement.BEGIN, true, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_TOGETHER, DateTimeSource.FILE_CREATION_DATE, true, customDate, "_", false, false),
                arguments("Name", "20051012_120005_Name", ItemPositionWithReplacement.BEGIN, true, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, true, customDate, "_", false, false),
                arguments("Name", "Name_20051012_120005", ItemPositionWithReplacement.END, true, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, true, customDate, "_", false, false),
                arguments("Name", "20051012_120005", ItemPositionWithReplacement.REPLACE, true, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, true, customDate, "_", false, false),
                arguments("Name", "20051012_120005", ItemPositionWithReplacement.REPLACE, false, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, true, customDate, "_", false, false),
                arguments("Name", "20100912_090000", ItemPositionWithReplacement.REPLACE, true, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_MODIFICATION_DATE, true, customDate, "_", false, false),
                arguments("Name", "20050912_120000", ItemPositionWithReplacement.REPLACE, true, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.CONTENT_CREATION_DATE, true, customDate, "_", false, false),
                arguments("Name", "20050912_120000", ItemPositionWithReplacement.REPLACE, true, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.CONTENT_CREATION_DATE, true, customDate, "x", false, false),
                arguments("Name", "20050912_120000", ItemPositionWithReplacement.REPLACE, true, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.CONTENT_CREATION_DATE, true, customDate, "", false, false),
                arguments("Name", "20050912_120000xName", ItemPositionWithReplacement.BEGIN, true, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.CONTENT_CREATION_DATE, true, customDate, "x", false, false),
                arguments("Name", "20050912_120000Name", ItemPositionWithReplacement.BEGIN, true, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.CONTENT_CREATION_DATE, true, customDate, "", false, false),
                arguments("Name", "20240601_111100_Name", ItemPositionWithReplacement.BEGIN, true, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.CUSTOM_DATE, true, customDate, "_", false, false)
                        );
        // @formatter:on
    }

    static Stream<Arguments> testCommandExecutionSeparateCasesArguments() {
        var amPm = TestUtilities.getLocaleAmPm();
        // @formatter:off
        Supplier<FileInformation> fileWithOnlyFC = () -> createTestFileInformation(TEST_FC_TIME_2005_10_12_12_00_05, null, null);
        Supplier<FileInformation> fileWithOnlyFM = () -> createTestFileInformation(null, TEST_FM_TIME_2010_09_12_09_00_00, null);
        Supplier<FileInformation> fileWithOnlyCC = () -> createTestFileInformation(null, null, TEST_CC_TIME_2005_09_12_12_00_00);
        Supplier<FileInformation> fileWithFC_FM = () -> createTestFileInformation(TEST_FC_TIME_2005_10_12_12_00_05, TEST_FM_TIME_2010_09_12_09_00_00, null);
        Supplier<FileInformation> fileWithFM_CC = () -> createTestFileInformation(null, TEST_FM_TIME_2010_09_12_09_00_00, TEST_CC_TIME_2005_09_12_12_00_00);

        // TEST_FC_TIME_2005_10_12_12_00_05
        // TEST_FM_TIME_2010_09_12_09_00_00
        // TEST_CC_TIME_2005_09_12_12_00_00

        LocalDateTime customDate = LocalDateTime.of(2024,6,1,11,11);
        return Stream.of(
                arguments("20051012_120005" + amPm.PM().toUpperCase(), fileWithOnlyFC.get(), DateTimeSource.FILE_CREATION_DATE, customDate, true, false, false),
                arguments("20100912_090000" + amPm.AM().toUpperCase(), fileWithOnlyFM.get(), DateTimeSource.FILE_MODIFICATION_DATE, customDate, true, false, false),
                arguments("20050912_120000" + amPm.PM().toUpperCase(), fileWithOnlyCC.get(), DateTimeSource.CONTENT_CREATION_DATE, customDate, true, false, false),
                arguments(TEST_FILE_NAME, fileWithOnlyFC.get(), DateTimeSource.CONTENT_CREATION_DATE, customDate, true, false, false),
                arguments(TEST_FILE_NAME, fileWithOnlyFC.get(), DateTimeSource.FILE_MODIFICATION_DATE, customDate, true, false, false),
                arguments(TEST_FILE_NAME, fileWithOnlyFM.get(), DateTimeSource.FILE_CREATION_DATE, customDate, true, false, false),
                arguments(TEST_FILE_NAME, fileWithOnlyFM.get(), DateTimeSource.CONTENT_CREATION_DATE, customDate, true, false, false),
                arguments(TEST_FILE_NAME, fileWithOnlyCC.get(), DateTimeSource.FILE_CREATION_DATE, customDate, true, false, false),
                arguments(TEST_FILE_NAME, fileWithOnlyCC.get(), DateTimeSource.FILE_MODIFICATION_DATE, customDate, true, false, false),
                arguments(TEST_FILE_NAME, fileWithFC_FM.get(), DateTimeSource.CONTENT_CREATION_DATE, customDate, true, false, false),
                arguments("20051012_120005" + amPm.PM().toLowerCase(), fileWithFC_FM.get(), DateTimeSource.CONTENT_CREATION_DATE, customDate, false, true, false),
                arguments("20051012_120005" + amPm.PM().toLowerCase(), fileWithOnlyFC.get(), DateTimeSource.CONTENT_CREATION_DATE, customDate, false, true, false),
                arguments("20051012_120005" + amPm.PM().toLowerCase(), fileWithOnlyFC.get(), DateTimeSource.FILE_MODIFICATION_DATE, customDate, false, true, false),
                arguments("20100912_090000" + amPm.AM().toLowerCase(), fileWithOnlyFM.get(), DateTimeSource.FILE_CREATION_DATE, customDate, false, true, false),
                arguments("20100912_090000" + amPm.AM().toLowerCase(), fileWithOnlyFM.get(), DateTimeSource.CONTENT_CREATION_DATE, customDate, false, true, false),
                arguments("20240601_111100" + amPm.AM().toLowerCase(), fileWithOnlyFM.get(), DateTimeSource.CONTENT_CREATION_DATE, customDate, false, true, true),
                arguments("20240601_111100" + amPm.AM().toLowerCase(), fileWithOnlyFC.get(), DateTimeSource.CONTENT_CREATION_DATE, customDate, false, true, true),
                arguments("20240601_111100" + amPm.AM().toLowerCase(), fileWithFC_FM.get(), DateTimeSource.CONTENT_CREATION_DATE, customDate, false, true, true),
                arguments("20240601_111100" + amPm.AM().toLowerCase(), fileWithFM_CC.get(), DateTimeSource.FILE_CREATION_DATE, customDate, false, true, true)
                        );
        // @formatter:on
    }

    private static FileInformation createTestFileInformation(LocalDateTime fc, LocalDateTime fm, LocalDateTime cc) {
        var metadata = FileInformationMetadata.builder().creationDate(cc).build();
        return FileInformation.builder()
                              .originalFile(new File(TEST_DEFAULT_FILE_PATH))
                              .fileAbsolutePath(TEST_ABSOLUTE_PATH)
                              .isFile(TEST_IS_FILE)
                              .fileName(TEST_FILE_NAME)
                              .newName(TEST_FILE_NAME)
                              .fileExtension(TEST_FILE_EXTENSION)
                              .newExtension(TEST_FILE_EXTENSION)
                              .fileSize(TEST_FILE_SIZE)
                              .fsCreationDate(fc)
                              .fsModificationDate(fm)
                              .metadata(metadata)
                              .build();
    }

    @ParameterizedTest
    @MethodSource("provideCommandArguments")
    void commandExecution_ShouldRenameFileToDateTime(String originalName, String expectedName, ItemPositionWithReplacement itemPositionWithReplacement, boolean isFile, DateFormat dateFormat, TimeFormat timeFormat, DateTimeFormat dateTimeFormat, DateTimeSource dateTimeSource, boolean useUppercaseForAmPm, LocalDateTime customDateTime, String dateTimeAndNameSeparator, boolean useFallbackDateTime, boolean useCustomDateTimeAsFallback) {
        // Prepare Test Data
        var fileInfoMeta = FileInformationMetadata.builder().creationDate(TEST_CC_TIME_2005_09_12_12_00_00).build();
        var fileInfo = FileInformation.builder()
                                      .originalFile(new File(TEST_DEFAULT_FILE_PATH))
                                      .fileAbsolutePath(TEST_ABSOLUTE_PATH)
                                      .isFile(isFile)
                                      .fileName(originalName)
                                      .newName(originalName)
                                      .fileExtension(TEST_FILE_EXTENSION)
                                      .newExtension(TEST_FILE_EXTENSION)
                                      .fileSize(TEST_FILE_SIZE)
                                      .fsCreationDate(TEST_FC_TIME_2005_10_12_12_00_05)
                                      .fsModificationDate(TEST_FM_TIME_2010_09_12_09_00_00)
                                      .metadata(fileInfoMeta)
                                      .build();

        List<FileInformation> inputList = List.of(fileInfo);

        // Build the command for test

        var builder = DateTimeRenamePrepareInformationCommand.builder();
        builder.dateTimeOperations(dateTimeOperations);
        if (Objects.nonNull(itemPositionWithReplacement)) {
            builder.dateTimePositionInTheName(itemPositionWithReplacement);
        }
        if (Objects.nonNull(dateFormat)) {
            builder.dateFormat(dateFormat);
        }
        if (Objects.nonNull(timeFormat)) {
            builder.timeFormat(timeFormat);
        }
        if (Objects.nonNull(dateTimeFormat)) {
            builder.dateTimeFormat(dateTimeFormat);
        }
        if (Objects.nonNull(dateTimeSource)) {
            builder.dateTimeSource(dateTimeSource);
        }

        builder.useUppercaseForAmPm(useUppercaseForAmPm);

        if (Objects.nonNull(customDateTime)) {
            builder.customDateTime(customDateTime);
        }
        if (Objects.nonNull(dateTimeAndNameSeparator)) {
            builder.dateTimeAndNameSeparator(dateTimeAndNameSeparator);
        }

        builder.useFallbackDateTime(useFallbackDateTime);
        builder.useCustomDateTimeAsFallback(useCustomDateTimeAsFallback);

        Command<List<FileInformation>, List<FileInformation>> command = builder.build();

        testCommandWithItemChanged(command, inputList, expectedName, TEST_FILE_EXTENSION);
    }

    @ParameterizedTest
    @MethodSource("testCommandExecutionSeparateCasesArguments")
    void testCommandExecutionForSeparateCases(String expectedName, FileInformation fileInformation, DateTimeSource source, LocalDateTime customDateTime, boolean useUppercaseForAmPm, boolean useFallbackDateTime, boolean useCustomDateTimeAsFallback) {
        List<FileInformation> inputList = List.of(fileInformation);

        // Build the command for test

        var builder = DateTimeRenamePrepareInformationCommand.builder();
        builder.dateTimeOperations(dateTimeOperations);
        builder.dateTimePositionInTheName(ItemPositionWithReplacement.REPLACE);
        builder.dateFormat(DateFormat.YYYY_MM_DD_TOGETHER);
        builder.timeFormat(TimeFormat.HH_MM_SS_AM_PM_TOGETHER);
        builder.dateTimeFormat(DateTimeFormat.DATE_TIME_UNDERSCORED);
        builder.useUppercaseForAmPm(useUppercaseForAmPm);
        builder.useFallbackDateTime(useFallbackDateTime);
        builder.useCustomDateTimeAsFallback(useCustomDateTimeAsFallback);
        if (Objects.nonNull(source)) {
            builder.dateTimeSource(source);
        }
        if (Objects.nonNull(customDateTime)) {
            builder.customDateTime(customDateTime);
        }

        Command<List<FileInformation>, List<FileInformation>> command = builder.build();

        testCommandWithItemChanged(command, inputList, expectedName, TEST_FILE_EXTENSION);
    }

    @Test
    void testCommandExecutionForCurrentTime() {
        List<FileInformation> inputList = List.of(createTestFileInformation(TEST_FC_TIME_2005_10_12_12_00_05, TEST_FM_TIME_2010_09_12_09_00_00, TEST_FC_TIME_2005_10_12_12_00_05));

        // Build the command for test

        var builder = DateTimeRenamePrepareInformationCommand.builder();
        builder.dateTimeOperations(dateTimeOperations);
        builder.dateTimePositionInTheName(ItemPositionWithReplacement.REPLACE);
        builder.dateFormat(DateFormat.YYYY_MM_DD_TOGETHER);
        builder.timeFormat(TimeFormat.HH_MM_SS_24_TOGETHER);
        builder.dateTimeFormat(DateTimeFormat.DATE_TIME_UNDERSCORED);
        builder.useUppercaseForAmPm(false);
        builder.useFallbackDateTime(false);
        builder.useCustomDateTimeAsFallback(false);
        builder.dateTimeSource(DateTimeSource.CURRENT_DATE);
        Command<List<FileInformation>, List<FileInformation>> command = builder.build();

        LocalDateTime fixedDateTime = LocalDateTime.of(2020, 1, 1, 0, 0);
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);

            LocalDateTime now = LocalDateTime.now();
            assertEquals(fixedDateTime, now);

            testCommandWithItemChanged(command, inputList, "20200101_000000", TEST_FILE_EXTENSION);
            mockedLocalDateTime.verify(LocalDateTime::now, times(2));
        }
    }

    @Test
    @Override
    void defaultCommandCreation_ShouldSetDefaultValues() {
        var command = DateTimeRenamePrepareInformationCommand.builder().dateTimeOperations(dateTimeOperations).build();

        assertNotNull(command);
        assertEquals(ItemPositionWithReplacement.BEGIN, command.getDateTimePositionInTheName());
        assertEquals(DateFormat.DO_NOT_USE_DATE, command.getDateFormat());
        assertEquals(TimeFormat.DO_NOT_USE_TIME, command.getTimeFormat());
        assertEquals(DateTimeFormat.DATE_TIME_TOGETHER, command.getDateTimeFormat());
        assertEquals(DateTimeSource.FILE_CREATION_DATE, command.getDateTimeSource());
        assertEquals("", command.getDateTimeAndNameSeparator());

        assertFalse(command.isUseUppercaseForAmPm());
        assertFalse(command.isUseFallbackDateTime());
        assertFalse(command.isUseCustomDateTimeAsFallback());

        var customDateTime = command.getCustomDateTime();
        assertNotNull(customDateTime);

        var currentDateTime = LocalDateTime.now();
        assertEquals(currentDateTime.getYear(), customDateTime.getYear());
        assertEquals(currentDateTime.getMonth(), customDateTime.getMonth());
        assertEquals(currentDateTime.getDayOfMonth(), customDateTime.getDayOfMonth());
        assertEquals(currentDateTime.getHour(), customDateTime.getHour());
        assertEquals(currentDateTime.getMinute(), customDateTime.getMinute());

    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return DateTimeRenamePrepareInformationCommand.builder()
                                                      .dateTimeOperations(dateTimeOperations)
                                                      .dateTimeFormat(DateTimeFormat.DATE_TIME_DASHED)
                                                      .dateFormat(DateFormat.YYYY_MM_DD_TOGETHER)
                                                      .timeFormat(TimeFormat.HH_MM_SS_24_TOGETHER)
                                                      .dateTimeSource(DateTimeSource.CURRENT_DATE)
                                                      .dateTimeFormat(DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970)
                                                      .build();
    }

    @Override
    boolean nameShouldBeChanged() {
        return true;
    }

    @Override
    boolean extensionShouldBeChanged() {
        return false;
    }

}