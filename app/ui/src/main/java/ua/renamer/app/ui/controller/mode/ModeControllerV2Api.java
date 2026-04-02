package ua.renamer.app.ui.controller.mode;

import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.ModeParameters;

/**
 * Marker and behaviour interface for V2-migrated mode controllers.
 *
 * <p>A controller implementing this interface is detected at runtime by
 * {@code ApplicationMainViewController} and routed through the V2 path
 * (via {@link ModeApi}) instead of the legacy V1 command path.
 *
 * @param <P> the concrete {@link ModeParameters} subtype this controller manages
 */
public interface ModeControllerV2Api<P extends ModeParameters> {

    /**
     * Called by {@code ApplicationMainViewController} immediately after the backend
     * returns a {@link ModeApi} for the selected mode.
     *
     * <p>Implementations must attach listeners to their FXML controls here.
     * Each listener calls {@code modeApi.updateParameters(p -> p.withField(newValue))}
     * when a control value changes.
     *
     * @param modeApi the live API handle for this mode's parameters; never null
     */
    void bind(ModeApi<P> modeApi);

    /**
     * Returns the {@link TransformationMode} this controller handles.
     * Used by the registry and the dual-path router for look-up.
     *
     * @return the transformation mode supported by this controller; never null
     */
    TransformationMode supportedMode();
}
