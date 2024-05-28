package ua.renamer.app.core.mappers.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class BmpMapper extends ImageBaseMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Bmp.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return BmpHeaderDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return BmpHeaderDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return BmpHeaderDirectory.TAG_IMAGE_HEIGHT;
    }

}
