package ua.renamer.app.core.mappers.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.ico.IcoDirectory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class IcoMapper extends ImageBaseMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Ico.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return IcoDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return IcoDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return IcoDirectory.TAG_IMAGE_HEIGHT;
    }

}
