package com.ammuyutan.iou;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import android.database.SQLException;
import java.util.ArrayList;

/**
 * Created by YeahBaby on 8/31/2016.
 */
public class SQLiteHelperClass extends SQLiteOpenHelper {

    private static SQLiteHelperClass instance;
    private static final String SQL_CREATE_DB =
            "CREATE TABLE " + DatabaseMetadata.dbDebtor.TABLE_NAME + " (" +
                    DatabaseMetadata.dbDebtor.COLUMN_ID+" INTEGER PRIMARY KEY,"+ //The "ROWID" column of sqlite which is auto-increment
                    DatabaseMetadata.dbDebtor.COLUMN_NAME + " TEXT NOT NULL," +
                    DatabaseMetadata.dbDebtor.COLUMN_BALANCE + "   TEXT NOT NULL" + "," +
                    DatabaseMetadata.dbDebtor.COLUMN_DEADLINE + " TEXT NOT NULL" + "," +
                    DatabaseMetadata.dbDebtor.COLUMN_PHONENO + " TEXT NOT NULL," +
                    DatabaseMetadata.dbDebtor.COLUMN_EMAIL + " TEXT" + "," +
                    DatabaseMetadata.dbDebtor.COLUMN_IMAGE + " BLOB" + ")";

    private static final String SQL_DROP_DB = "DROP TABLE IF EXISTS " + DatabaseMetadata.dbDebtor.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME ="dbIOU.db";


    private SQLiteHelperClass(Context context) {
        //database is created here
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public static SQLiteHelperClass getInstance(Context context){
        if(instance == null){
            instance = new SQLiteHelperClass(context);
        }
        return instance;
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DB);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DROP_DB);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    //---------For Android DatabaseMetadata Manager:

    public ArrayList<Cursor> getData(String Query) {
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[]{"mesage"};
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try {
            String maxQuery = Query;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});

            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0, c);
                c.moveToFirst();

                return alc;
            }
            return alc;
        } catch (SQLException sqlEx) {
            Log.i("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        } catch (Exception ex) {

            Log.i("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }

    }
}
