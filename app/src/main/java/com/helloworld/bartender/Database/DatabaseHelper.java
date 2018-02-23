package com.helloworld.bartender.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.helloworld.bartender.Item.Item;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by 김현식 on 2018-02-05.
 */

public class DatabaseHelper extends SQLiteOpenHelper{
    private static String DB_NAME ="filter_list.db";    //sqlite
    private static final int DB_VERSION = 2 ;
    public static final String TABLE_NAME = "list";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FILTER_NAME = "name";
    public static final String COLUMN_FILTER_BLUR = "blur";
    public static final String COLUMN_FILTER_FOCUS = "focus";
    public static final String COLUMN_FILTER_ABERATION = "aberation";
    public static final String COLUMN_FILTER_NOISE_SIZE = "noise_size";
    public static final String COLUMN_FILTER_NOISE_INTENSITY = "noise_intensity";

    public DatabaseHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FILTER_NAME + " TEXT NOT NULL, " +
                COLUMN_FILTER_BLUR + " REAL NOT NULL, " +
                COLUMN_FILTER_ABERATION + " REAL NOT NULL, " +
                COLUMN_FILTER_FOCUS + " REAL NOT NULL, "+
                COLUMN_FILTER_NOISE_SIZE+" REAL NOT NULL, "+
                COLUMN_FILTER_NOISE_INTENSITY+" REAL NOT NULL);"
        );
        Log.e("x","sqldbcreated");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //you can implement here migration process
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        this.onCreate(db);
    }

    public void saveFilter(Item filter){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FILTER_NAME, filter.getFilter_name());
        values.put(COLUMN_FILTER_BLUR,filter.getBlur());
        values.put(COLUMN_FILTER_ABERATION,filter.getAberation());
        values.put(COLUMN_FILTER_FOCUS,filter.getFocus());
        values.put(COLUMN_FILTER_NOISE_SIZE,filter.getNoiseSize());
        values.put(COLUMN_FILTER_NOISE_INTENSITY,filter.getNoiseIntensity());

        db.insert(TABLE_NAME,null,values);
        db.close();
    }

    //정렬
    public List<Item> FilterList(String option){
        String query;
        if(option.equals("")) {
            query = "SELECT * FROM " + TABLE_NAME;
        }else{
            query = "SELECT * FROM "+ TABLE_NAME+" ORDER BY "+option;
        }
        List<Item> filterLinkedList = new LinkedList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        Item filter;
        if(cursor.moveToFirst()){
            int i=0;
            do{
                filter = new Item();
                filter.setId(cursor.getLong((cursor.getColumnIndex(COLUMN_ID))));
                filter.setFilter_name(cursor.getString(cursor.getColumnIndex(COLUMN_FILTER_NAME)));
                filter.setBlur(cursor.getFloat(cursor.getColumnIndex(COLUMN_FILTER_BLUR)));
                filter.setAberation(cursor.getFloat(cursor.getColumnIndex(COLUMN_FILTER_ABERATION)));
                filter.setFocus(cursor.getFloat(cursor.getColumnIndex(COLUMN_FILTER_FOCUS)));
                filter.setNoiseSize(cursor.getFloat(cursor.getColumnIndex(COLUMN_FILTER_NOISE_SIZE)));
                filter.setNoiseIntensity(cursor.getFloat(cursor.getColumnIndex(COLUMN_FILTER_NOISE_INTENSITY)));
                filterLinkedList.add(filter);
                i++;
            }while(cursor.moveToNext());
            Log.d("x",String.valueOf(i)+"repeated this");
        }
        return filterLinkedList;
    }

    public Item getFilter(long id){
        Item receivedFilter= new Item();
        return receivedFilter;
    }

    public void deleteFilterRecord(long id, Context context){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE _id='"+id+"'");
        Toast.makeText(context,"Deleted successfully",Toast.LENGTH_SHORT).show();
    }

    public void updateFilterRecord(long filterId, Context context,Item updatedFilter){
        SQLiteDatabase db = this.getWritableDatabase();
        //you can use the constants above instead of typing the column names
        db.execSQL("UPDATE "+TABLE_NAME+"SET name ='"+updatedFilter.getFilter_name()+"', blur ='"+updatedFilter.getBlur()+"', aberation ='"+updatedFilter.getAberation()+
                "', focus ='"+updatedFilter.getFocus()+"', noiseSize ='"+updatedFilter.getNoiseSize()+"', noiseIntensity ='"+updatedFilter.getNoiseIntensity() +"' WHERE _id='"+ filterId+"'");
        Toast.makeText(context,"Updated successfully",Toast.LENGTH_SHORT).show();
    }


}
