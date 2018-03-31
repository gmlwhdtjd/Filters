package com.helloworld.bartender.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.helloworld.bartender.FilterList.FilterListView;
import com.helloworld.bartender.FilterableCamera.FCameraCapture;
import com.helloworld.bartender.FilterableCamera.Filters.DefaultFilter;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;
import com.helloworld.bartender.MainActivity;
import com.helloworld.bartender.R;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by 김현식 on 2018-02-05.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_NAME = "filter_list.db";    //sqlite
    private static final int DB_VERSION = 1;
    public static final String TABLE_MAIN_NAME = "list";
    public static final String TYPE1_TABLE_NAME = "OriginalFilter";
    public static final String TYPE0_DEFAULT_NAME = "DefaultFilter";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FILTER_NAME = "name";
    public static final String COLUMN_FILTER_TYPE = "type";
    public static final String COLUMN_FILTER_POS = "position";
    public static final String COLUMN_FILTER_ICON = "filterIcon";
    public Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        if (!db.isReadOnly()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                String query = String.format("PRAGMA foreign_keys = %s", "ON");
                db.execSQL(query);
            } else {
                db.setForeignKeyConstraintsEnabled(true);
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" CREATE TABLE " + TABLE_MAIN_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FILTER_NAME + " TEXT NOT NULL, " +
                COLUMN_FILTER_TYPE + " TEXT NOT NULL, " +
                COLUMN_FILTER_ICON + " BLOB , " +
                COLUMN_FILTER_POS + " INT NOT NULL" +
                ");"
        );

        String query = " CREATE TABLE " + TYPE1_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, ";

        for (OriginalFilter.ValueType valueType : OriginalFilter.ValueType.values()) {
            query += valueType.toString() + " INTEGER NOT NULL, ";
        }
        query += "FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " + TABLE_MAIN_NAME + "(" + COLUMN_ID + ") ON DELETE CASCADE); ";

        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //you can implement here migration process

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAIN_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TYPE1_TABLE_NAME);
        this.onCreate(db);
    }

    public int saveFilter(FCameraFilter filter) {
        SQLiteDatabase db = this.getReadableDatabase();
        int position;
        //update
        if (filter.getId() != null) {
            Cursor cs = db.rawQuery("SELECT position FROM " + TABLE_MAIN_NAME + " WHERE " + COLUMN_ID + "='" + filter.getId() + "'", null);
            cs.moveToFirst();
            position = cs.getInt(cs.getColumnIndex(COLUMN_FILTER_POS));
        } else {
            //add
            FilterListView filterListView = ((MainActivity) mContext).findViewById(R.id.FilterListView);
            position = filterListView.getHorizontalAdapter().getItemCount() - 1;
        }
        return saveFilter(filter, position);
    }

    public int saveFilter(FCameraFilter filter, int position) {

        String query="";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        ContentValues valuesForMain = new ContentValues();

        //id가 존재하고 type이 다르면 기존 type테이블에 있는 튜플 삭제
        if (filter.getId() != null) {
            String type = findTypeWithId(db, filter.getId());
            if (!filter.getClass().getSimpleName().equals(type)) {
                String sql = "DELETE FROM " + type + " WHERE " + COLUMN_ID + "='" + filter.getId().toString() + "'";
                db.execSQL(sql);
            }
        }

        valuesForMain.put(COLUMN_ID, filter.getId());
        valuesForMain.put(COLUMN_FILTER_NAME, EncodeFilterName(filter.getName()));
        valuesForMain.put(COLUMN_FILTER_TYPE, filter.getClass().getSimpleName());
        valuesForMain.put(COLUMN_FILTER_POS, position);

        //id null이면 튜플 생성, id가 존재하면 튜플 update
        int lastInsertedId = (int) db.replace(TABLE_MAIN_NAME, null, valuesForMain);

        Log.d("lastinsertedid", String.valueOf(lastInsertedId));

        //type에 따라 테이블과 속성이 바뀜
        switch (filter.getClass().getSimpleName()) {
            case TYPE0_DEFAULT_NAME:
               BitmapDrawable defaultDrawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.default_image);
               Bitmap defaultFilterIconImage = defaultDrawable.getBitmap();
                byte[] defaultData = getByteArrayFromDrawable(defaultFilterIconImage);
              query = "UPDATE " + TABLE_MAIN_NAME + " SET " + COLUMN_FILTER_ICON + "=? WHERE " + COLUMN_ID + "=" + String.valueOf(lastInsertedId);
                SQLiteStatement p1 = db.compileStatement(query);
                p1.bindBlob(1, defaultData);
                p1.execute();
                defaultFilterIconImage.recycle();

                break;
            case TYPE1_TABLE_NAME:
                values.put(COLUMN_ID, lastInsertedId);
                for (OriginalFilter.ValueType valueType : OriginalFilter.ValueType.values()) {
                    values.put(valueType.toString(), filter.getValueWithType(valueType));
                }
                db.replace(TYPE1_TABLE_NAME, null, values);

                FCameraCapture cameraCapture = ((MainActivity) mContext).getFCameraCapture();
              BitmapDrawable  originalDrawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.sample_image2);
              Bitmap  originalFilterIconImage = cameraCapture.bitmapFiltering(filter, originalDrawable.getBitmap());
                byte[] OriginalData = getByteArrayFromDrawable(originalFilterIconImage);
              query = "UPDATE " + TABLE_MAIN_NAME + " SET " + COLUMN_FILTER_ICON + "=? WHERE " + COLUMN_ID + "=" + String.valueOf(lastInsertedId);
                SQLiteStatement p2 = db.compileStatement(query);
                p2.bindBlob(1, OriginalData);
                p2.execute();


                break;
            default:
                break;
        }

        db.close();

        return lastInsertedId;

    }

    public FCameraFilter pasteFilter(FCameraFilter receivedFilter, int position) {
        SQLiteDatabase db = this.getWritableDatabase();
        FCameraFilter newFilter = null;
        FCameraFilter pastedFilter = null;
        int pastedFilterId;

        //자리를 만들어준다.
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MAIN_NAME + " WHERE position > " + position + " ORDER BY position", null);
        if (cursor.moveToLast()) {
            do {
                int newPosition = cursor.getInt(cursor.getColumnIndex(COLUMN_FILTER_POS));
                db.execSQL("UPDATE " + TABLE_MAIN_NAME + " SET position ='" + String.valueOf(newPosition + 1) + "' WHERE position='" + newPosition + "'");
            } while (cursor.moveToPrevious());
        }

        switch (receivedFilter.getClass().getSimpleName()) {
            case TYPE1_TABLE_NAME:
                newFilter = new OriginalFilter(mContext, null);
                for (OriginalFilter.ValueType valueType : OriginalFilter.ValueType.values()) {
                    newFilter.setValueWithType(valueType, receivedFilter.getValueWithType(valueType));
                }
                newFilter.setName(receivedFilter.getName());

                pastedFilterId = saveFilter(newFilter, position + 1);
                pastedFilter = new OriginalFilter(mContext, pastedFilterId);
                for (OriginalFilter.ValueType valueType : OriginalFilter.ValueType.values()) {
                    pastedFilter.setValueWithType(valueType, newFilter.getValueWithType(valueType));
                }
                pastedFilter.setName(newFilter.getName());
                break;
            default:
                break;
        }
        return pastedFilter;
    }


    //정렬
    public List<FCameraFilter> getFilterList(String option) {
        String query;
        if (option.equals("")) {
            query = "SELECT * FROM " + TABLE_MAIN_NAME + " ORDER BY " + COLUMN_FILTER_POS;
        } else {
            query = "SELECT * FROM " + TABLE_MAIN_NAME + " ORDER BY " + option;
        }
        List<FCameraFilter> filterLinkedList = new LinkedList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            int i = 0;
            do {
                String type = cursor.getString(cursor.getColumnIndex(COLUMN_FILTER_TYPE));
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String name = DecodeFilterName(cursor.getString(cursor.getColumnIndex(COLUMN_FILTER_NAME)));
                switch (type) {
                    case TYPE1_TABLE_NAME:
                        FCameraFilter filter = new OriginalFilter(mContext, id);
                        filter.setName(name);
                        Cursor typeCursor = db.rawQuery("SELECT * FROM " + TYPE1_TABLE_NAME + " WHERE " + COLUMN_ID + "=" + id, null);
                        typeCursor.moveToFirst();
                        for (OriginalFilter.ValueType valueType : OriginalFilter.ValueType.values()) {
                            filter.setValueWithType(valueType, typeCursor.getInt(typeCursor.getColumnIndex(valueType.toString())));
                        }
                        filterLinkedList.add(filter);
                        break;
                    case TYPE0_DEFAULT_NAME:
                        FCameraFilter defaultFilter = new DefaultFilter(mContext,id,name);
                        filterLinkedList.add(defaultFilter);
                        break;
                    default:
                        break;
                }
                i++;
            } while (cursor.moveToNext());
            Log.d("x", String.valueOf(i) + "repeated this");

        }
        return filterLinkedList;
    }

    public Drawable getFilterIconImage(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_FILTER_ICON + " FROM " + TABLE_MAIN_NAME + " WHERE " + COLUMN_ID + "='" + String.valueOf(id) + "'", null);
        cursor.moveToFirst();
        byte[] b = cursor.getBlob(cursor.getColumnIndex(COLUMN_FILTER_ICON));
        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        Drawable filterIconImage = new BitmapDrawable(mContext.getResources(), bitmap);
        return filterIconImage;
    }

    public void deleteFilterRecord(int id, int position) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MAIN_NAME + " WHERE position > " + position + " ORDER BY position", null);
        if (cursor.moveToFirst()) {
            do {
                int newPosition = cursor.getInt(cursor.getColumnIndex(COLUMN_FILTER_POS));
                db.execSQL("UPDATE " + TABLE_MAIN_NAME + " SET position ='" + String.valueOf(newPosition - 1) + "' WHERE position='" + newPosition + "'");
            } while (cursor.moveToNext());
        }
        db.execSQL("DELETE FROM " + TABLE_MAIN_NAME + " WHERE _id='" + id + "'");
    }

    public String findTypeWithId(SQLiteDatabase db, int id) {
        Cursor cs = db.rawQuery("SELECT * FROM " + TABLE_MAIN_NAME + " WHERE " + COLUMN_ID + "='" + id + "'", null);
        cs.moveToFirst();
        String type = cs.getString(cs.getColumnIndex(COLUMN_FILTER_TYPE));
        return type;
    }

    public void changePositionByDrag(int fromPos, int toPos) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("SELECT * FROM " + TABLE_MAIN_NAME + " WHERE position='" + fromPos + "'", null);
        cs.moveToFirst();
        int fromId = cs.getInt(cs.getColumnIndex(COLUMN_ID));

        //fromPOs의 id를 받아서 저장한 후 fromPos와 toPos사이의 pos를 전부 바꾼후 마지막으로 저장한 값을 바꾼다.

        if (fromPos > toPos) {
            cs = db.rawQuery("SELECT * FROM " + TABLE_MAIN_NAME + " WHERE position >= " + toPos + " AND position < " + fromPos + " ORDER BY position", null);
            if (cs.moveToLast()) {
                do {
                    int newPosition = cs.getInt(cs.getColumnIndex(COLUMN_FILTER_POS));
                    db.execSQL("UPDATE " + TABLE_MAIN_NAME + " SET position ='" + String.valueOf(newPosition + 1) + "' WHERE position='" + newPosition + "'");
                } while (cs.moveToPrevious());
            }
            db.execSQL("UPDATE " + TABLE_MAIN_NAME + " SET position ='" + toPos + "' WHERE _id='" + fromId + "'");

        } else if (toPos > fromPos) {
            cs = db.rawQuery("SELECT * FROM " + TABLE_MAIN_NAME + " WHERE position <= " + toPos + " AND position > " + fromPos + " ORDER BY position", null);
            if (cs.moveToFirst()) {
                do {
                    int newPosition = cs.getInt(cs.getColumnIndex(COLUMN_FILTER_POS));
                    db.execSQL("UPDATE " + TABLE_MAIN_NAME + " SET position ='" + String.valueOf(newPosition - 1) + "' WHERE position='" + newPosition + "'");
                } while (cs.moveToNext());
            }
            db.execSQL("UPDATE " + TABLE_MAIN_NAME + " SET position ='" + toPos + "' WHERE _id='" + fromId + "'");
        }
    }

    private String EncodeFilterName(String name) {
        String encodedName = "";
        try {
            encodedName = URLEncoder.encode(name, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("Encoding", e.toString());
        }
        return encodedName;
    }

    private String DecodeFilterName(String encodedName) {
        String decodedName = "";
        try {
            decodedName = URLDecoder.decode(encodedName, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("Encoding", e.toString());
        }
        return decodedName;
    }


    private byte[] getByteArrayFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        byte data[] = byteStream.toByteArray();

        return data;
    }


}
