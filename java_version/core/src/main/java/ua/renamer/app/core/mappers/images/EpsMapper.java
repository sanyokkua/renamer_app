package ua.renamer.app.core.mappers.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.eps.EpsDirectory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class EpsMapper extends ImageBaseMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Eps.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return EpsDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return EpsDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return EpsDirectory.TAG_IMAGE_HEIGHT;
    }

}
