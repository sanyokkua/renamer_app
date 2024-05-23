package ua.renamer.app.core.mappers.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.photoshop.PsdHeaderDirectory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class PsdMapper extends CommonImageMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Psd.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectory() {
        return PsdHeaderDirectory.class;
    }

    @Override
    protected Integer getWidthTag() {
        return PsdHeaderDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getHeightTag() {
        return PsdHeaderDirectory.TAG_IMAGE_HEIGHT;
    }

}
