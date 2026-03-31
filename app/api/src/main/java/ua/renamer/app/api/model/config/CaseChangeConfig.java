package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.TextCaseOptions;

/**
 * Configuration for changing the case of filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class CaseChangeConfig implements TransformationConfig {
    /** Case transformation option (CAMEL_CASE, SNAKE_CASE, etc.). */
    TextCaseOptions caseOption;

    /** Whether to capitalize the first letter after transformation. */
    boolean capitalizeFirstLetter;
}
