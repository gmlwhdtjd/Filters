package com.helloworld.bartender.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by 김현식 on 2018-02-05.
 */

public class DatabaseHelper extends SQLiteOpenHelper{
    private static String DB_NAME ="filter_list.db";    //sqlite
    private static final int DB_VERSION = 1 ;
    public static final String MAIN_TABLE_NAME = "list";
    public static final String TYPE1_TABLE_NAME = "originalType";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FILTER_TYPE = "type";
    public static final String COLUMN_FILTER_NAME = "name";
    public static final String COLUMN_FILTER_BLUR = "blur";
    public static final String COLUMN_FILTER_FOCUS = "focus";
    public static final String COLUMN_FILTER_ABERRATION = "aberration";
    public static final String COLUMN_FILTER_NOISE_SIZE = "noise_size";
    public static final String COLUMN_FILTER_NOISE_INTENSITY = "noise_intensity";

    public DatabaseHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" CREATE TABLE " + MAIN_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FILTER_TYPE+" TEXT NOT NULL, "+
                COLUMN_FILTER_NAME+" TEXT NOT NULL);"
        );

        db.execSQL(" CREATE TABLE " + TYPE1_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER NOT NULL, " +
                COLUMN_FILTER_BLUR + " REAL NOT NULL, " +
                COLUMN_FILTER_ABERRATION + " REAL NOT NULL, " +
                COLUMN_FILTER_FOCUS + " REAL NOT NULL, "+
                COLUMN_FILTER_NOISE_SIZE+" REAL NOT NULL, "+
                COLUMN_FILTER_NOISE_INTENSITY+" REAL NOT NULL);"
        );
        Log.e("x","sqldbcreated");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //you can implement here migration process
        db.execSQL("DROP TABLE IF EXISTS "+MAIN_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+TYPE1_TABLE_NAME);
        this.onCreate(db);
    }

    public void saveFilter(OriginalFilter filter){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valuesForType = new ContentValues();
        ContentValues valuesForMain = new ContentValues();

        valuesForMain.put(COLUMN_FILTER_NAME, filter.getFilter_name());
        valuesForMain.put(COLUMN_FILTER_TYPE, filter.getType());


        db.insert(MAIN_TABLE_NAME,null,valuesForMain);

        int id = FindId(filter.getFilter_name());

        valuesForType.put(COLUMN_ID,id);
        valuesForType.put(COLUMN_FILTER_BLUR,filter.getBlur());
        valuesForType.put(COLUMN_FILTER_ABERRATION,filter.getAberration());
        valuesForType.put(COLUMN_FILTER_FOCUS,filter.getFocus());
        valuesForType.put(COLUMN_FILTER_NOISE_SIZE,filter.getNoiseSize());
        valuesForType.put(COLUMN_FILTER_NOISE_INTENSITY,filter.getNoiseIntensity());

        db.insert(TYPE1_TABLE_NAME,null,valuesForType);
        db.close();
    }

    //정렬
    public List<OriginalFilter> FilterList(String option){
        String query;
        if(option.equals("")) {
            query = "SELECT * FROM " + MAIN_TABLE_NAME;
        }else{
            query = "SELECT * FROM "+ MAIN_TABLE_NAME+" ORDER BY "+option;
        }

        //For originalfilter
        List<OriginalFilter> filterLinkedList = new LinkedList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        OriginalFilter filter;
        if(cursor.moveToFirst()){
            int i=0;
            do{
                filter = new OriginalFilter();
                filter.setId(cursor.getLong((cursor.getColumnIndex(COLUMN_ID))));
                filter.setFilter_name(cursor.getString(cursor.getColumnIndex(COLUMN_FILTER_NAME)));
                filter.setBlur(cursor.getFloat(cursor.getColumnIndex(COLUMN_FILTER_BLUR)));
                filter.setAberration(cursor.getFloat(cursor.getColumnIndex(COLUMN_FILTER_ABERRATION)));
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

    public OriginalFilter getFilter(long id){
        OriginalFilter receivedFilter= new OriginalFilter();
        return receivedFilter;
    }

    public void deleteFilterRecord(long id, Context context){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE _id='"+id+"'");
        Toast.makeText(context,"Deleted successfully",Toast.LENGTH_SHORT).show();
    }

    public void updateFilterRecord(long filterId, Context context,OriginalFilter updatedFilter){
        SQLiteDatabase db = this.getWritableDatabase();
        //you can use the constants above instead of typing the column names
        db.execSQL("UPDATE "+TABLE_NAME+"SET name ='"+updatedFilter.getFilter_name()+"', blur ='"+updatedFilter.getBlur()+"', aberration ='"+updatedFilter.getAberration()+
                "', focus ='"+updatedFilter.getFocus()+"', noiseSize ='"+updatedFilter.getNoiseSize()+"', noiseIntensity ='"+updatedFilter.getNoiseIntensity() +"' WHERE _id='"+ filterId+"'");
        Toast.makeText(context,"Updated successfully",Toast.LENGTH_SHORT).show();
    }

    public int FindId(String name){
        SQLiteDatabase dsb = this.getReadableDatabase();
        Cursor cs = dsb.rawQuery("SELECT * FROM list WHERE name="+name+";",null);
        int id = cs.getInt(0);
        dsb.close();
        return id;
    }



}
