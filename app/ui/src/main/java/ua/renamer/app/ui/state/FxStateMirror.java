package ua.renamer.app.ui.state;

import com.google.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeParameters;
import ua.renamer.app.api.session.RenameCandidate;
import ua.renamer.app.api.session.RenamePreview;
import ua.renamer.app.api.session.RenameSessionResult;
import ua.renamer.app.api.session.SessionStatus;
import ua.renamer.app.api.session.StatePublisher;

import java.util.List;

/**
 * FX-thread-safe bridge between backend state and JavaFX observable properties.
 *
 * <p>All {@code publishX()} methods are called from the backend state thread.
 * Each wraps its body in {@link Platform#runLater(Runnable)} so that mutations
 * to observable properties always occur on the JavaFX Application Thread.
 *
 * <p>UI controllers obtain read-only views via the accessor methods and bind
 * directly — they never call the {@code publishX()} methods themselves.
 */
@Singleton
public class FxStateMirror implements StatePublisher {

    // Mutable backing lists (mutated only on FX thread via Platform.runLater)
    private final ObservableList<RenameCandidate> filesList =
            FXCollections.observableArrayList();
    private final ObservableList<RenamePreview> previewList =
            FXCollections.observableArrayList();
    private final ObservableList<RenameSessionResult> renameResultsList =
            FXCollections.observableArrayList();

    private final SimpleObjectProperty<SessionStatus> statusProp =
            new SimpleObjectProperty<>(SessionStatus.EMPTY);
    private final SimpleObjectProperty<TransformationMode> modeProp =
            new SimpleObjectProperty<>();
    private final SimpleObjectProperty<ModeParameters> paramsProp =
            new SimpleObjectProperty<>();

    // Cached read-only wrappers (created once — getReadOnlyProperty() returns same instance)
    private final ReadOnlyListWrapper<RenameCandidate> filesWrapper =
            new ReadOnlyListWrapper<>(filesList);
    private final ReadOnlyListWrapper<RenamePreview> previewWrapper =
            new ReadOnlyListWrapper<>(previewList);
    private final ReadOnlyListWrapper<RenameSessionResult> renameResultsWrapper =
            new ReadOnlyListWrapper<>(renameResultsList);

    // -------------------------------------------------------------------------
    // Read-only accessors for UI binding
    // -------------------------------------------------------------------------

    /**
     * Returns a read-only view of the current rename candidates list.
     *
     * @return read-only list property; never null
     */
    public ReadOnlyListProperty<RenameCandidate> files() {
        return filesWrapper.getReadOnlyProperty();
    }

    /**
     * Returns a read-only view of the current rename preview list.
     *
     * @return read-only list property; never null
     */
    public ReadOnlyListProperty<RenamePreview> preview() {
        return previewWrapper.getReadOnlyProperty();
    }

    /**
     * Returns a read-only view of the rename results from the last completed session.
     *
     * @return read-only list property; never null
     */
    public ReadOnlyListProperty<RenameSessionResult> renameResults() {
        return renameResultsWrapper.getReadOnlyProperty();
    }

    /**
     * Returns a read-only view of the current session status property.
     *
     * @return read-only object property; never null; initial value is {@link SessionStatus#EMPTY}
     */
    public ReadOnlyObjectProperty<SessionStatus> status() {
        return statusProp;
    }

    /**
     * Returns a read-only view of the currently active transformation mode property.
     *
     * @return read-only object property; never null; value is null until first mode change
     */
    public ReadOnlyObjectProperty<TransformationMode> activeMode() {
        return modeProp;
    }

    /**
     * Returns a read-only view of the current transformation mode parameters property.
     *
     * @return read-only object property; never null; value is null until first mode change
     */
    public ReadOnlyObjectProperty<ModeParameters> currentParameters() {
        return paramsProp;
    }

    // -------------------------------------------------------------------------
    // StatePublisher — called from backend state thread
    // -------------------------------------------------------------------------

    @Override
    public void publishFilesChanged(List<RenameCandidate> files, List<RenamePreview> preview) {
        Platform.runLater(() -> {
            filesList.setAll(files);
            previewList.setAll(preview);
            renameResultsList.clear();
        });
    }

    @Override
    public void publishPreviewChanged(List<RenamePreview> preview) {
        Platform.runLater(() -> previewList.setAll(preview));
    }

    @Override
    public void publishModeChanged(TransformationMode mode, ModeParameters params) {
        Platform.runLater(() -> {
            modeProp.set(mode);
            paramsProp.set(params);
        });
    }

    @Override
    public void publishRenameComplete(List<RenameSessionResult> results, SessionStatus status) {
        Platform.runLater(() -> {
            renameResultsList.setAll(results);  // set before status so listeners see updated list
            statusProp.set(status);
        });
    }

    @Override
    public void publishStatusChanged(SessionStatus status) {
        Platform.runLater(() -> statusProp.set(status));
    }
}
