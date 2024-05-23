package ua.renamer.app.core.mappers.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.pcx.PcxDirectory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class PcxMapper extends CommonImageMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Pcx.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectory() {
        return PcxDirectory.class;
    }

    @Override
    protected Integer getWidthTag() {
        return PcxDirectory.TAG_HSCR_SIZE;
    }

    @Override
    protected Integer getHeightTag() {
        return PcxDirectory.TAG_VSCR_SIZE;
    }

}
