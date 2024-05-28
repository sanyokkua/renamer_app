package ua.renamer.app.core.mappers.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.png.PngDirectory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class PngMapper extends ImageBaseMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Png.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return PngDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return PngDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return PngDirectory.TAG_IMAGE_HEIGHT;
    }

}
