package ua.renamer.app.core.commands.preparation;

import lombok.*;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.SortSource;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;

import java.util.Comparator;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SequencePrepareInformationCommand extends FileInformationCommand {

    @NonNull
    @Builder.Default
    private final SortSource sortSource = SortSource.FILE_NAME;
    @NonNull
    @Builder.Default
    private final int startNumber = 0;
    @NonNull
    @Builder.Default
    private final int stepValue = 1;
    @NonNull
    @Builder.Default
    private final int padding = 0;
    private int nextNumber;

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

    @Override
    public FileInformation processItem(FileInformation item) {
        nextNumber = nextNumber + stepValue;

        if (padding == 0) {
            item.setNewName(String.valueOf(nextNumber));
            return item;
        }

        String formattedString = String.format("%0" + padding + "d", nextNumber);
        item.setNewName(formattedString);
        return item;
    }

}
