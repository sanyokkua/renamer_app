package ua.renamer.app.core.mappers.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.heif.HeifDirectory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class HeifMapper extends ImageBaseMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Heif.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return HeifDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return HeifDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return HeifDirectory.TAG_IMAGE_HEIGHT;
    }

}
