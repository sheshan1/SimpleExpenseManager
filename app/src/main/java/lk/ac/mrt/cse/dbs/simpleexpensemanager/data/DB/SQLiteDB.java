package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class SQLiteDB extends SQLiteOpenHelper {
    private static final String DB = "200483J.db";

    private static final String AccountDetails = "account";
    private static final String TransactionDetails = "transactions";

    private static final int DEFAULT_LIMIT = 0;

    public SQLiteDB(@Nullable Context context) {
        super(context, DB, null, 2);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ AccountDetails +
                " (accountno TEXT PRIMARY KEY ,"+
                "bankname TEXT  PRIMARY KEY,"+
                "accountHolderName TEXT, "+
                "balance REAL"
                +")");

        db.execSQL("CREATE TABLE "+ TransactionDetails +
                " (transaction_no INTEGER  PRIMARY KEY AUTOINCREMENT,"+
                "accountno TEXT  ,"+
                "date TEXT, "+
                "expenseType TEXT ,"+
                "amount REAL"
                +")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ AccountDetails);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TransactionDetails);
        onCreate(sqLiteDatabase);
    }


    public boolean insertData(String table,ContentValues content){
        SQLiteDatabase db = this.getWritableDatabase();
        long result;

        try{
            result = db.insertOrThrow(table, null,content);
        }catch(Exception e){
            System.out.println("Error in Data Insert");
            result = -1;
        }

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public boolean updateData(String table,ContentValues content, String[] conditions){
        SQLiteDatabase db = this.getWritableDatabase();
        String condition = conditions[0] + " " + conditions[1] + " ? ";
        String[] args = {conditions[2]};

        long result;
        try{
            result = db.update(table, content,condition,args);
        }catch (Exception e){
            result = -1;
        }

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Integer deleteData(String table, String col, String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(table, col+" = ?", new String[] {id});
    }

    public Cursor getDataWithLimit(String table, String [] columns, String [][] conditions,int limit){
        SQLiteDatabase db = this.getWritableDatabase();

        String cols = "";
        if (columns.length != 0){
            for (int i = 0;i < columns.length ;i++){
                cols += columns[i]+" , ";
            }
            cols = cols.substring(0,cols.length()-2);
        }
        String condition = "";
        String[] args = null;
        if(conditions.length != 0){
            args = new String[conditions.length];
            condition += " WHERE ";
            for (int i = 0;i < conditions.length ;i++){
                if(conditions[i].length == 3){
                    String[] temp = conditions[i];
                    condition += temp[0] + " "+temp[1]+" ? AND ";
                    args[i] = temp[2];
                }

            }
            condition = condition.substring(0,condition.length()-4);
        }else{
            condition = "";
        }
        String lim = "";
        if(limit != 0){
            lim = " LIMIT "+String.valueOf(limit);
        }

        String sql = "SELECT "+cols+" FROM "+table+condition+lim;
        Cursor result = db.rawQuery(sql,args);
        return result;
    }

    public Cursor getData(String table, String [] columns, String [][] conditions){
        return getDataWithLimit(table, columns, conditions,DEFAULT_LIMIT);
    }

    public void deleteTableContent(String table_name){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+table_name);
    }
}
