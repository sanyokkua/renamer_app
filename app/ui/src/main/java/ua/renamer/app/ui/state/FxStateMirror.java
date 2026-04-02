package ua.renamer.app.ui.state;

import com.google.inject.Singleton;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.*;

import java.util.List;

/**
 * FX-thread-safe bridge between backend state and JavaFX UI.
 * Stub implementation — observable properties added in TASK-4.1.
 */
@Singleton
public class FxStateMirror implements StatePublisher {

    @Override
    public void publishFilesChanged(List<RenameCandidate> files, List<RenamePreview> preview) {
    }

    @Override
    public void publishPreviewChanged(List<RenamePreview> preview) {
    }

    @Override
    public void publishModeChanged(TransformationMode mode, ModeParameters params) {
    }

    @Override
    public void publishRenameComplete(List<RenameSessionResult> results, SessionStatus status) {
    }

    @Override
    public void publishStatusChanged(SessionStatus status) {
    }
}
