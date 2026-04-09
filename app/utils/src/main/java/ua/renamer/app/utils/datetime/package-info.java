/**
 * Date-time parsing and formatting utilities for the Renamer application.
 *
 * <p>Stateless helper classes that handle the wide variety of date-time string
 * formats found in file metadata (EXIF, ID3, filesystem attributes). All
 * classes expose only static methods and cannot be instantiated.
 *
 * <p>Contains {@link ua.renamer.app.utils.datetime.DateTimeUtils} for parsing
 * date-time strings across multiple formats and locales, converting
 * {@code FileTime} to {@code LocalDateTime}, finding the earliest of a set of
 * timestamps, and formatting values to a canonical string representation.
 */
package ua.renamer.app.utils.datetime;
