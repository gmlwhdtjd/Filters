package com.helloworld.bartender.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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

        ContentValues valuesForType = new ContentValues();
        saveMain(filter.getFilter_name(),filter.getType());

        long id = FindIdWithName(filter.getFilter_name());
        SQLiteDatabase db = this.getWritableDatabase();
        valuesForType.put(COLUMN_ID,id);
        valuesForType.put(COLUMN_FILTER_BLUR,filter.getBlur());
        valuesForType.put(COLUMN_FILTER_ABERRATION,filter.getAberration());
        valuesForType.put(COLUMN_FILTER_FOCUS,filter.getFocus());
        valuesForType.put(COLUMN_FILTER_NOISE_SIZE,filter.getNoiseSize());
        valuesForType.put(COLUMN_FILTER_NOISE_INTENSITY,filter.getNoiseIntensity());

        db.insert(TYPE1_TABLE_NAME,null,valuesForType);
        db.close();

    }

    public void saveMain(String name, String type){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valuesForMain = new ContentValues();
        valuesForMain.put(COLUMN_FILTER_NAME, name);
        valuesForMain.put(COLUMN_FILTER_TYPE, type);
        db.insert(MAIN_TABLE_NAME,null,valuesForMain);
        db.close();
    }


    //Original Filter 정렬
    public List<OriginalFilter> FilterList(String option){
        String query;
        if(option.equals("")) {
            query = "SELECT * FROM " + MAIN_TABLE_NAME;
        }else{
            query = "SELECT * FROM "+ MAIN_TABLE_NAME+" ORDER BY "+option;
        }

        //For originalfilter
        List<OriginalFilter> filterLinkedList = new LinkedList<>();
        SQLiteDatabase Type1DB = this.getWritableDatabase();
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query,null);
        OriginalFilter filter;
        if(cursor.moveToFirst()){
            int i=0;
            do{
                filter = new OriginalFilter();
                long FilterID = cursor.getLong((cursor.getColumnIndex(COLUMN_ID)));
                filter.setId(FilterID);
                filter.setFilter_name(cursor.getString(cursor.getColumnIndex(COLUMN_FILTER_NAME)));
                Cursor Type1Cursor =Type1DB.rawQuery("SELECT * FROM "+TYPE1_TABLE_NAME+" WHERE _id='"+FilterID+"'",null);
                if(Type1Cursor.moveToFirst()) {
                    do {
                        filter.setBlur(Type1Cursor.getFloat(Type1Cursor.getColumnIndex(COLUMN_FILTER_BLUR)));
                        filter.setAberration(Type1Cursor.getFloat(Type1Cursor.getColumnIndex(COLUMN_FILTER_ABERRATION)));
                        filter.setFocus(Type1Cursor.getFloat(Type1Cursor.getColumnIndex(COLUMN_FILTER_FOCUS)));
                        filter.setNoiseSize(Type1Cursor.getFloat(Type1Cursor.getColumnIndex(COLUMN_FILTER_NOISE_SIZE)));
                        filter.setNoiseIntensity(Type1Cursor.getFloat(Type1Cursor.getColumnIndex(COLUMN_FILTER_NOISE_INTENSITY)));
                    }while (Type1Cursor.moveToNext());
                }
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

        db.execSQL("DELETE FROM "+MAIN_TABLE_NAME+" WHERE _id='"+id+"'");
        db.execSQL("DELETE FROM "+TYPE1_TABLE_NAME+" WHERE _id='"+id+"'");
        Toast.makeText(context,"Deleted successfully",Toast.LENGTH_SHORT).show();
    }
    public void updateFilterName(long filterId,Context context,String name){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE "+MAIN_TABLE_NAME+" SET name='"+name+"' WHERE _id='"+filterId+"'");
    }

    public void updateFilterRecord(long filterId, Context context,OriginalFilter updatedFilter){
        SQLiteDatabase db = this.getWritableDatabase();
        //you can use the constants above instead of typing the column names
        db.execSQL("UPDATE "+TYPE1_TABLE_NAME+" SET blur ='"+updatedFilter.getBlur()+"', aberration ='"+updatedFilter.getAberration()+
                "', focus ='"+updatedFilter.getFocus()+"', noiseSize ='"+updatedFilter.getNoiseSize()+"', noiseIntensity ='"+updatedFilter.getNoiseIntensity() +"' WHERE _id='"+ filterId+"'");
        Toast.makeText(context,"Updated successfully",Toast.LENGTH_SHORT).show();
    }

    public long FindIdWithName(String name) {
        SQLiteDatabase dsb = this.getWritableDatabase();

        Cursor cs = dsb.rawQuery("SELECT * FROM " + MAIN_TABLE_NAME + " WHERE name='" + name + "'", null);
        cs.moveToFirst();
        long FilterID = cs.getLong(0);
        dsb.close();
        return FilterID;
    }




}
