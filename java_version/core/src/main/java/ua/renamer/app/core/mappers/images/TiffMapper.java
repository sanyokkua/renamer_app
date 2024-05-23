package ua.renamer.app.core.mappers.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformationMetadata;

import java.io.File;
import java.util.Set;

@Slf4j
public class TiffMapper extends CommonImageMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Tiff.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectory() {
        return null;
    }

    @Override
    protected Integer getWidthTag() {
        return null;
    }

    @Override
    protected Integer getHeightTag() {
        return null;
    }

    @Override
    public FileInformationMetadata process(File input) {
        var height = getHeight(input);
        var width = getWidth(input);
        var dateTime = getContentCreationDateTimeFromExif(input);

        return FileInformationMetadata.builder()
                                      .creationDate(dateTime)
                                      .imgVidWidth(width)
                                      .imgVidHeight(height)
                                      .build();
    }

}
