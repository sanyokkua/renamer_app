package ua.renamer.app.ui.view;

import com.google.inject.Singleton;
import jakarta.inject.Inject;
import javafx.scene.Parent;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.api.model.TransformationMode;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Registry mapping each {@link TransformationMode} to a factory that loads
 * and returns the corresponding JavaFX {@link Parent} view.
 *
 * <p>Each migrated mode controller (or its DI provider) calls
 * {@link #register} during initialization. {@code ApplicationMainViewController}
 * calls {@link #getView} when a mode is selected.
 *
 * <p>This class replaces the 10 individual {@code @Provides @Named("ModeXxx")}
 * FXML-loader methods in {@code DIUIModule} and will be fully populated once
 * all 10 controllers are migrated (TASK-4.14).
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeViewRegistry {

    private final Map<TransformationMode, Supplier<Parent>> registry =
            new EnumMap<>(TransformationMode.class);

    /**
     * Registers a view factory for the given mode.
     * Overwrites any previously registered factory for the same mode.
     *
     * @param mode        the transformation mode; never null
     * @param viewFactory supplier that constructs/loads the view; never null
     */
    public void register(TransformationMode mode, Supplier<Parent> viewFactory) {
        registry.put(mode, viewFactory);
    }

    /**
     * Returns the view for {@code mode} by invoking its registered factory,
     * or {@link Optional#empty()} if no factory has been registered yet.
     *
     * @param mode the transformation mode to look up; never null
     * @return the loaded {@link Parent} wrapped in {@link Optional},
     * or empty if no factory has been registered for the given mode
     */
    public Optional<Parent> getView(TransformationMode mode) {
        return Optional.ofNullable(registry.get(mode)).map(Supplier::get);
    }
}
