package ua.renamer.app.core.service.command.impl.preparation;

import lombok.*;
import ua.renamer.app.core.enums.SortSource;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.command.FileInformationCommand;

import java.util.Comparator;
import java.util.List;

/**
 * Command for sequencing file names within {@link FileInformation} objects.
 * This class extends {@link FileInformationCommand} and provides functionality to sequence
 * file names based on specified options such as sort source, starting number, step value, and padding.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SequencePrepareInformationCommand extends FileInformationCommand {

    /**
     * The source used for sorting the file information before sequencing.
     */
    @NonNull
    @Builder.Default
    private final SortSource sortSource = SortSource.FILE_NAME;
    /**
     * The starting number for sequencing.
     */
    @Builder.Default
    private final int startNumber = 0;
    /**
     * The step value used for incrementing the sequence number.
     */
    @Builder.Default
    private final int stepValue = 1;
    /**
     * The padding size for the sequence number.
     */
    @Builder.Default
    private final int padding = 0;
    /**
     * The next number in the sequence.
     */
    private int nextNumber;

    /**
     * Sorts the input list of file information based on the specified sort source.
     *
     * @param input the input list of file information to be preprocessed.
     *
     * @return the preprocessed input list sorted based on the specified sort source.
     */
    @Override
    protected List<FileInformation> preprocessInput(List<FileInformation> input) {
        nextNumber = startNumber;

        switch (sortSource) {
            case FILE_NAME -> input.sort(Comparator.nullsFirst(Comparator.comparing(FileInformation::getFileName)));
            case FILE_PATH ->
                    input.sort(Comparator.nullsFirst(Comparator.comparing(FileInformation::getFileAbsolutePath)));
            case FILE_SIZE -> input.sort(Comparator.nullsFirst(Comparator.comparing(FileInformation::getFileSize)));
            case FILE_CREATION_DATETIME ->
                    input.sort(Comparator.nullsFirst(Comparator.comparing(fileInformation -> fileInformation.getFsCreationDate()
                                                                                                            .orElse(null))));
            case FILE_MODIFICATION_DATETIME ->
                    input.sort(Comparator.nullsFirst(Comparator.comparing(fileInformation -> fileInformation.getFsModificationDate()
                                                                                                            .orElse(null))));
            case FILE_CONTENT_CREATION_DATETIME ->
                    input.sort(Comparator.nullsFirst(Comparator.comparing(fileInformation -> fileInformation.getMetadata()
                                                                                                            .flatMap(
                                                                                                                    FileInformationMetadata::getCreationDate)
                                                                                                            .orElse(null))));
            case IMAGE_WIDTH ->
                    input.sort(Comparator.nullsFirst(Comparator.comparing(fileInformation -> fileInformation.getMetadata()
                                                                                                            .flatMap(
                                                                                                                    FileInformationMetadata::getImgVidWidth)
                                                                                                            .orElse(null))));
            case IMAGE_HEIGHT ->
                    input.sort(Comparator.nullsFirst(Comparator.comparing(fileInformation -> fileInformation.getMetadata()
                                                                                                            .flatMap(
                                                                                                                    FileInformationMetadata::getImgVidHeight)
                                                                                                            .orElse(null))));
        }

        return input;
    }

    /**
     * Processes a {@link FileInformation} item by sequencing the file name based on the next number in the sequence.
     *
     * @param item the {@link FileInformation} item to be processed.
     *
     * @return the processed {@link FileInformation} item with the new file name.
     */
    @Override
    public FileInformation processItem(FileInformation item) {
        String nextName;

        if (padding == 0) {
            nextName = String.valueOf(nextNumber);
        } else {
            nextName = String.format("%0" + padding + "d", nextNumber);
        }

        item.setNewName(nextName);

        nextNumber = nextNumber + stepValue;
        return item;
    }

}
