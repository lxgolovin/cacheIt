package com.lxgolovin.cache.core;

public class ErrorMessages {

    public static final String EMPTY_DIRECTORY_LOGGER = "Unable to empty directory {}: {}";
    public static final String EMPTY_DIRECTORY_TRACE = "Cannot create temporary directory ";

    public static final String CREATE_DIRECTORY_LOGGER = "Cannot create directory: {}";
    public static final String CREATE_DIRECTORY_TRACE = "Cannot create temporary directory ";

    public static final String CREATE_TEMP_DIRECTORY_LOGGER = "Cannot create temporary directory: {}";
    public static final String CREATE_TEMP_DIRECTORY_TRACE = "Cannot create temporary directory ";

    public static final String READ_FILE_FROM_STORAGE_LOGGER = "Cannot read file {} from storage: {}";
    public static final String READ_FILE_FROM_STORAGE_TRACE = "Cannot read from storage ";

    public static final String PUT_DATA_TO_STORAGE_LOGGER = "Cannot write data to file {}: {}";
    public static final String PUT_DATA_TO_STORAGE_TRACE = "Cannot write data to file ";

    public static final String CREATE_FILE_STORAGE_LOGGER = "Cannot create temp file in directory {}: {}";
    public static final String CREATE_FILE_STORAGE_TRACE = "Cannot create temp file in directory to save data";

    public static final String READ_ALL_FROM_STORAGE_LOGGER = "Cannot read file/directory {}: {}";
    public static final String READ_ALL_FROM_STORAGE_TRACE = "Contact admin. Cannot read directory ";

    public static final String INFO_FILE_DELETE_SUCCESS = "File {} deleted successfully";
    public static final String WARN_FILE_DELETE_FAIL = "File {} not deleted";
}
