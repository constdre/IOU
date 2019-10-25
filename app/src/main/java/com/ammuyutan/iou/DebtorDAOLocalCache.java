package com.ammuyutan.iou;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ammuyutan.iou.Models.Debtor;
import com.ammuyutan.iou.Util.ActivityUtil;
import com.ammuyutan.iou.Util.Statics;

import java.util.ArrayList;
import java.util.List;

public class DebtorDAOLocalCache implements DebtorDAO{

    /**
     *DetorDAOLocalCache - local db operations implementation
     */

    private SQLiteHelperClass dbHelper;
    private SQLiteDatabase sqlDB;
    private String columns[] = {DatabaseMetadata.dbDebtor.COLUMN_ID, DatabaseMetadata.dbDebtor.COLUMN_NAME, DatabaseMetadata.dbDebtor.COLUMN_BALANCE, DatabaseMetadata.dbDebtor.COLUMN_DEADLINE, DatabaseMetadata.dbDebtor.COLUMN_PHONENO, DatabaseMetadata.dbDebtor.COLUMN_EMAIL, DatabaseMetadata.dbDebtor.COLUMN_IMAGE};

    public DebtorDAOLocalCache(Context context){
        dbHelper = SQLiteHelperClass.getInstance(context);
    }


    @Override
    public void addDebtor(Debtor debtor) {
        sqlDB = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseMetadata.dbDebtor.COLUMN_NAME, debtor.getName());
        values.put(DatabaseMetadata.dbDebtor.COLUMN_BALANCE, debtor.getBalance());
        values.put(DatabaseMetadata.dbDebtor.COLUMN_DEADLINE, debtor.getDeadline());
        values.put(DatabaseMetadata.dbDebtor.COLUMN_PHONENO, debtor.getPhoneNo());
        values.put(DatabaseMetadata.dbDebtor.COLUMN_EMAIL, debtor.getEmail());
        values.put(DatabaseMetadata.dbDebtor.COLUMN_IMAGE, debtor.getImage());
        sqlDB.insert(DatabaseMetadata.dbDebtor.TABLE_NAME, null, values);
    }
    @Override
    public void updateDebtor(Debtor debtor) {
        //like update everything? is this efficient?
        sqlDB = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseMetadata.dbDebtor.COLUMN_NAME, debtor.getName());
        values.put(DatabaseMetadata.dbDebtor.COLUMN_DEADLINE, debtor.getDeadline());
        values.put(DatabaseMetadata.dbDebtor.COLUMN_BALANCE, debtor.getBalance());
        values.put(DatabaseMetadata.dbDebtor.COLUMN_PHONENO, debtor.getPhoneNo());
        values.put(DatabaseMetadata.dbDebtor.COLUMN_EMAIL, debtor.getEmail());
        values.put(DatabaseMetadata.dbDebtor.COLUMN_IMAGE, debtor.getImage());

        String whereClause = DatabaseMetadata.dbDebtor.COLUMN_ID + "= ?";
        String [] whereArgs = {String.valueOf(debtor.getId())};

        int rowsUpdated = sqlDB.update(DatabaseMetadata.dbDebtor.TABLE_NAME,values, whereClause,whereArgs);
        Log.i(Statics.LOG_TAG_MAIN, "Updated debtor id"+debtor.getId()+", rows affected: "+ rowsUpdated);
    }

    public void updateDebtor1(String oldPhoneNo, String name, String deadline, String phoneNo, String email, byte[]image) {
        sqlDB = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseMetadata.dbDebtor.COLUMN_NAME, name);
        values.put(DatabaseMetadata.dbDebtor.COLUMN_DEADLINE, deadline);
        values.put(DatabaseMetadata.dbDebtor.COLUMN_PHONENO, phoneNo);
        values.put(DatabaseMetadata.dbDebtor.COLUMN_EMAIL, email);
        values.put(DatabaseMetadata.dbDebtor.COLUMN_IMAGE, image);
        String whereClause = DatabaseMetadata.dbDebtor.COLUMN_PHONENO + "= ?";
        String [] whereArgs = {oldPhoneNo};
        sqlDB.update(DatabaseMetadata.dbDebtor.TABLE_NAME,values, whereClause,whereArgs);
    }



    @Override
    public int updateBalance(String balance, int id) {
        sqlDB = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseMetadata.dbDebtor.COLUMN_BALANCE,balance);
        String whereClause = DatabaseMetadata.dbDebtor.COLUMN_ID + "= ?";
        String [] whereArgs = {String.valueOf(id)};
        int rows = sqlDB.update(DatabaseMetadata.dbDebtor.TABLE_NAME,values,whereClause,whereArgs);
        return rows;
    }


    @Override
    public Debtor getDebtorByPhoneNo(String phoneNo) {
        sqlDB = dbHelper.getWritableDatabase();
        String selection = DatabaseMetadata.dbDebtor.COLUMN_PHONENO+"= ?";
        String[] selectionArgs = {String.valueOf(phoneNo)};
        Cursor cursor = sqlDB.query(DatabaseMetadata.dbDebtor.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        while(cursor.moveToNext()){
            return ActivityUtil.buildDebtor(cursor);
        }
        return null;
    }

    @Override
    public Debtor getDebtorById(int id) {
        sqlDB = dbHelper.getReadableDatabase();
        String selection = "id=?";
        String[] strArgs = {String.valueOf(id)};

        Cursor cursor = sqlDB.query(DatabaseMetadata.dbDebtor.TABLE_NAME,columns,selection,strArgs,null,null,null);
        //supposed to return only 1 entity:
        while(cursor.moveToNext()){
            return ActivityUtil.buildDebtor(cursor);
        }
        return null;

    }

    @Override
    public void deleteDebtor(int id) {
        sqlDB = dbHelper.getWritableDatabase();
        sqlDB.delete(DatabaseMetadata.dbDebtor.TABLE_NAME, "id=?", new String[]{String.valueOf(id)});
        //sqlDB.execSQL("DELETE FROM " + DatabaseMetadata.dbDebtor.TABLE_NAME + " WHERE phoneNo = '"+ phoneNo +"'");
    }



    @Override
    public List<Debtor> getDebtors() {
        List<Debtor> debtors = new ArrayList<>();
        sqlDB = dbHelper.getReadableDatabase();
        int id;
        String balance;
        String name, email, phoneNo, deadline;
        byte[] image;
        Cursor cursor = sqlDB.query(DatabaseMetadata.dbDebtor.TABLE_NAME, columns, null, null, null, null, null);

        while(cursor.moveToNext()){

            id = cursor.getInt(cursor.getColumnIndex("id"));
            balance = cursor.getString(cursor.getColumnIndex("balance"));
            name = cursor.getString(cursor.getColumnIndex("name"));
            email = cursor.getString(cursor.getColumnIndex("email"));
            phoneNo = cursor.getString(cursor.getColumnIndex("phoneNo"));
            deadline = cursor.getString(cursor.getColumnIndex("deadline"));
            image = cursor.getBlob(cursor.getColumnIndex("image"));

//            debtors.add(new Debtor(cursor.getString(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getBlob(5)));
            debtors.add(new Debtor(id,name,balance,deadline,phoneNo,email,image));
        }
        return debtors;
    }

    @Override
    public List<Debtor> searchDebtors(String param){
        List<Debtor> searchResults = new ArrayList<>();
        sqlDB = dbHelper.getReadableDatabase();
        StringBuilder sb = new StringBuilder();

        String whereClause = sb.append(DatabaseMetadata.dbDebtor.COLUMN_ID)
                .append(" LIKE ? OR ")
                .append(DatabaseMetadata.dbDebtor.COLUMN_NAME)
                .append(" LIKE ? OR ")
                .append(DatabaseMetadata.dbDebtor.COLUMN_BALANCE)
                //.append(" LIKE '%" + param + "%' OR")
                .append(" LIKE ? OR ")
                .append(DatabaseMetadata.dbDebtor.COLUMN_EMAIL)
                .append(" LIKE ? OR ")
                .append(DatabaseMetadata.dbDebtor.COLUMN_PHONENO)
                .append(" LIKE ?").toString();
        String pattern = "%"+param+"%"; //"containing the substring" filter
        String[] whereArgs = {pattern,pattern,pattern,pattern};
        Cursor c = sqlDB.query(DatabaseMetadata.dbDebtor.TABLE_NAME, columns, whereClause,whereArgs,null,null,null);

        while(c.moveToNext()){
            searchResults.add(ActivityUtil.buildDebtor(c));
        }
        return searchResults;
    }


}
