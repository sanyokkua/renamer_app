package ua.renamer.app.core.mappers.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.png.PngDirectory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class PngMapper extends CommonImageMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Png.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectory() {
        return PngDirectory.class;
    }

    @Override
    protected Integer getWidthTag() {
        return PngDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getHeightTag() {
        return PngDirectory.TAG_IMAGE_HEIGHT;
    }

}
