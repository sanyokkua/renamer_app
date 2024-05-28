package ua.renamer.app.core.mappers.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class TiffMapper extends ImageBaseMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Tiff.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return null;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return null;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return null;
    }

}
