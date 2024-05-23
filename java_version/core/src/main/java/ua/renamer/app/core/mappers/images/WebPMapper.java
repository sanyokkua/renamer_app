package ua.renamer.app.core.mappers.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.webp.WebpDirectory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class WebPMapper extends CommonImageMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.WebP.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectory() {
        return WebpDirectory.class;
    }

    @Override
    protected Integer getWidthTag() {
        return WebpDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getHeightTag() {
        return WebpDirectory.TAG_IMAGE_HEIGHT;
    }

}
