package com.ammuyutan.iou;

import android.provider.BaseColumns;

/**
 * Houses DB Column names
 */
public class DatabaseMetadata {
    public static abstract class dbDebtor implements BaseColumns
    {
        public static final String TABLE_NAME = "Debtor";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BALANCE = "balance";
        public static final String COLUMN_DEADLINE = "deadline";
        public static final String COLUMN_PHONENO = "phoneNo";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_IMAGE = "image";
    }
}
