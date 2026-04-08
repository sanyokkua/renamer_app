package ua.renamer.app.backend.integration;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * IDE-friendly suite runner: executes all integration tests in this package.
 *
 * <p>From Maven, use:
 * <pre>
 *   # Integration tests only
 *   mvn test -pl app/backend -Dgroups=integration -Dai=true
 *
 *   # All backend tests (unit + integration)
 *   mvn test -pl app/backend -Dai=true
 * </pre>
 */
@Suite
@SelectPackages("ua.renamer.app.backend.integration")
@IncludeTags("integration")
class IntegrationTestSuite {
}
