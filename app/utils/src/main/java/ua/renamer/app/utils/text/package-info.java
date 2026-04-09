/**
 * Text manipulation utilities for the Renamer application.
 *
 * <p>Stateless helper classes for string operations used when constructing
 * or transforming file names. All classes expose only static methods and
 * cannot be instantiated.
 *
 * <p>Contains:
 * <ul>
 *   <li>{@link ua.renamer.app.utils.text.TextUtils} — null/blank detection,
 *       capitalization, and assembly of a file name from base name and
 *       extension parts.</li>
 *   <li>{@link ua.renamer.app.utils.text.CaseUtils} — converts strings between
 *       naming conventions: camelCase, PascalCase, snake_case,
 *       SCREAMING_SNAKE_CASE, kebab-case, Title Case, uppercase, and lowercase.
 *       Handles mixed delimiters, camelCase boundaries, and alphanumeric
 *       transitions during word extraction.</li>
 * </ul>
 */
package ua.renamer.app.utils.text;
