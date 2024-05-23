package ua.renamer.app.core.mappers.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.gif.GifImageDirectory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class GifMapper extends CommonImageMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Gif.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectory() {
        return GifImageDirectory.class;
    }

    @Override
    protected Integer getWidthTag() {
        return GifImageDirectory.TAG_WIDTH;
    }

    @Override
    protected Integer getHeightTag() {
        return GifImageDirectory.TAG_HEIGHT;
    }

}
