package ua.renamer.app.core.mappers.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class JpegMapper extends CommonImageMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Jpeg.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectory() {
        return JpegDirectory.class;
    }

    @Override
    protected Integer getWidthTag() {
        return JpegDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getHeightTag() {
        return JpegDirectory.TAG_IMAGE_HEIGHT;
    }

}
