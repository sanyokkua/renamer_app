/**
 * File-path manipulation utilities for the Renamer application.
 *
 * <p>Stateless helper classes for extracting and normalising components of
 * file-system paths. All classes expose only static methods and cannot be
 * instantiated.
 *
 * <p>Contains {@link ua.renamer.app.utils.file.FileUtils} for extracting the
 * base name, file extension, absolute path string, and ordered list of parent
 * folder names. Correctly handles hidden files (names starting with {@code '.'})
 * and normalises Windows-style backslash separators.
 */
package ua.renamer.app.utils.file;
