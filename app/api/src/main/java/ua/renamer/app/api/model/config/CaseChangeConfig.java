package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.TextCaseOptions;

import java.util.Objects;

/**
 * Configuration for changing the case of filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class CaseChangeConfig implements TransformationConfig {
    /**
     * Case transformation option (CAMEL_CASE, SNAKE_CASE, etc.).
     */
    TextCaseOptions caseOption;

    /**
     * Whether to capitalize the first letter after transformation.
     */
    boolean capitalizeFirstLetter;

    // Partial Lombok builder — Lombok adds with* methods; we override build() for validation
    public static class CaseChangeConfigBuilder {
        /**
         * Builds the {@link CaseChangeConfig}, validating that required fields are non-null.
         *
         * @return a new {@link CaseChangeConfig} instance
         * @throws NullPointerException if caseOption is null
         */
        public CaseChangeConfig build() {
            Objects.requireNonNull(caseOption, "caseOption must not be null");
            return new CaseChangeConfig(caseOption, capitalizeFirstLetter);
        }
    }
}
