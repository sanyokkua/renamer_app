package ua.renamer.app.core.v2.interfaces;

import ua.renamer.app.core.v2.model.FileModel;

import java.io.File;

public interface FileMapper {
    FileModel mapFrom(File file);
}
