package ua.renamer.app.core.lang;

import lombok.Getter;

/**
 * An enumeration of text keys used for localization.
 */
@Getter
public enum TextKeys {
    APP_HEADER("app_header"),
    APP_MODE_LABEL("app_mode_label"),
    CHECK_BOX_AUTO_PREVIEW("check_box_auto_preview"),
    BTN_PREVIEW("btn_preview"),
    BTN_RENAME("btn_rename"),
    BTN_CLEAR("btn_clear"),
    MODE_ADD_CUSTOM_TEXT("mode_add_custom_text"),
    MODE_CHANGE_CASE("mode_change_case"),
    MODE_DATETIME("mode_datetime"),
    MODE_IMG_VID_DIMENSIONS("mode_img_vid_dimensions"),
    MODE_PARENT_FOLDERS("mode_parent_folders"),
    MODE_REMOVE_TEXT("mode_remove_text"),
    MODE_REPLACE_TEXT("mode_replace_text"),
    MODE_USE_DIGITAL_SEQUENCE("mode_use_digital_sequence"),
    MODE_TRUNCATE("mode_truncate"),
    MODE_CHANGE_EXTENSION("mode_change_extension"),
    TABLE_COLUMN_NAME("table_column_name"),
    TABLE_COLUMN_TYPE("table_column_type"),
    TABLE_COLUMN_NEW_NAME("table_column_new_name"),
    RADIO_BTN_BEGIN("radio_btn_begin"),
    RADIO_BTN_END("radio_btn_end"),
    RADIO_BTN_REPLACE("radio_btn_replace"),
    RADIO_BTN_EVERYWHERE("radio_btn_everywhere"),
    RADIO_BTN_TRIM_EMPTY("radio_btn_trim_empty"),
    MODE_ADD_CUSTOM_TEXT_LABEL_POSITION("mode_add_custom_text_label_position"),
    MODE_ADD_CUSTOM_TEXT_LABEL_TEXT("mode_add_custom_text_label_text"),
    MODE_CHANGE_CASE_LABEL_TEXT_CASE("mode_change_case_label_text_case"),
    MODE_CHANGE_CASE_LABEL_CAPITALIZE("mode_change_case_label_capitalize"),
    MODE_DATETIME_LABEL_POSITION("mode_datetime_label_position"),
    MODE_DATETIME_LABEL_DATETIME_NAME_SEPARATOR("mode_datetime_label_datetime_name_separator"),
    MODE_DATETIME_LABEL_DATE_FORMAT("mode_datetime_label_date_format"),
    MODE_DATETIME_LABEL_TIME_FORMAT("mode_datetime_label_time_format"),
    MODE_DATETIME_LABEL_DATE_TIME_FORMAT("mode_datetime_label_date_time_format"),
    MODE_DATETIME_LABEL_TIME_SOURCE("mode_datetime_label_time_source"),
    MODE_DATETIME_LABEL_USE_FALLBACK("mode_datetime_label_use_fallback"),
    MODE_DATETIME_LABEL_USE_FALLBACK_CUSTOM_DATE("mode_datetime_label_use_fallback_custom_date"),
    MODE_IMG_VID_DIMENSIONS_LABEL_POSITION("mode_img_vid_dimensions_label_position"),
    MODE_IMG_VID_DIMENSIONS_LABEL_LEFT_SIDE("mode_img_vid_dimensions_label_left_side"),
    MODE_IMG_VID_DIMENSIONS_LABEL_RIGHT_SIDE("mode_img_vid_dimensions_label_right_side"),
    MODE_IMG_VID_DIMENSIONS_LABEL_SEPARATOR_BETWEEN("mode_img_vid_dimensions_label_separator_between"),
    MODE_IMG_VID_DIMENSIONS_LABEL_NAME_SEPARATOR("mode_img_vid_dimensions_label_name_separator"),
    MODE_PARENT_FOLDERS_LABEL_POSITION("mode_parent_folders_label_position"),
    MODE_PARENT_FOLDERS_LABEL_NUMBER("mode_parent_folders_label_number"),
    MODE_PARENT_FOLDERS_LABEL_SEPARATOR("mode_parent_folders_label_separator"),
    MODE_REMOVE_TEXT_LABEL_POSITION("mode_remove_text_label_position"),
    MODE_REMOVE_TEXT_LABEL_TEXT_TO_REMOVE("mode_remove_text_label_text_to_remove"),
    MODE_REPLACE_TEXT_LABEL_POSITION("mode_replace_text_label_position"),
    MODE_REPLACE_TEXT_LABEL_TEXT_TO_REPLACE("mode_replace_text_label_text_to_replace"),
    MODE_REPLACE_TEXT_LABEL_TEXT_TO_ADD("mode_replace_text_label_text_to_add"),
    MODE_USE_DIGITAL_SEQUENCE_LABEL_START_VALUE("mode_use_digital_sequence_label_start_value"),
    MODE_USE_DIGITAL_SEQUENCE_LABEL_STEP_VALUE("mode_use_digital_sequence_label_step_value"),
    MODE_USE_DIGITAL_SEQUENCE_LABEL_AMOUNT_OF_DIGITS("mode_use_digital_sequence_label_amount_of_digits"),
    MODE_USE_DIGITAL_SEQUENCE_LABEL_SORTING_SOURCE("mode_use_digital_sequence_label_sorting_source"),
    MODE_TRUNCATE_LABEL_POSITION("mode_truncate_label_position"),
    MODE_TRUNCATE_LABEL_AMOUNT_OF_SYMBOLS("mode_truncate_label_amount_of_symbols"),
    MODE_CHANGE_EXTENSION_LABEL_NEW_EXTENSION("mode_change_extension_label_new_extension"),
    DO_NOT_USE("do_not_use"),
    DATE_TIME_NUMBER_OF_SECONDS("date_time_number_of_seconds"),
    IMG_VID_DIMENSION_WIDTH("img_vid_dimension_width"),
    IMG_VID_DIMENSION_HEIGHT("img_vid_dimension_height"),
    DATE_TIME_SOURCE_FILE_CREATION_DATETIME("date_time_source_file_creation_datetime"),
    DATE_TIME_SOURCE_FILE_MODIFICATION_DATETIME("date_time_source_file_modification_datetime"),
    DATE_TIME_SOURCE_FILE_CONTENT_CREATION_DATETIME("date_time_source_file_content_creation_datetime"),
    DATE_TIME_SOURCE_CURRENT_DATETIME("date_time_source_current_datetime"),
    DATE_TIME_SOURCE_CUSTOM_DATETIME("date_time_source_custom_datetime"),
    FILE_SORTING_SOURCE_FILE_NAME("file_sorting_source_file_name"),
    FILE_SORTING_SOURCE_FILE_PATH("file_sorting_source_file_path"),
    FILE_SORTING_SOURCE_FILE_SIZE("file_sorting_source_file_size"),
    FILE_SORTING_SOURCE_FILE_CREATION_DATETIME("file_sorting_source_file_creation_datetime"),
    FILE_SORTING_SOURCE_FILE_MODIFICATION_DATETIME("file_sorting_source_file_modification_datetime"),
    FILE_SORTING_SOURCE_FILE_CONTENT_CREATION_DATETIME("file_sorting_source_file_content_creation_datetime"),
    FILE_SORTING_SOURCE_IMG_VID_WIDTH("file_sorting_source_img_vid_width"),
    FILE_SORTING_SOURCE_IMG_VID_HEIGHT("file_sorting_source_img_vid_height");

    private final String keyString;

    TextKeys(String keyString) {
        this.keyString = keyString;
    }
}
