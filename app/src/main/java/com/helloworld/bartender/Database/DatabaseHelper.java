package com.helloworld.bartender.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.RetroFilter;
import com.helloworld.bartender.MainActivity;
import com.helloworld.bartender.R;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by 김현식 on 2018-02-05.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private Context mContext;
    private static String DB_NAME = "filter_list.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_MAIN_NAME = "list";
    private static final String TYPE1_FILTER_NAME = "RetroFilter";
    private static final String TYPE0_DEFAULT_NAME = "OriginalFilter";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_FILTER_NAME = "name";
    private static final String COLUMN_FILTER_TYPE = "type";
    private static final String COLUMN_FILTER_POS = "position";
    private static final String COLUMN_FILTER_ICON = "filterIcon";
    private String DB_PATH;
    private SQLiteDatabase myDatabase;

    public DatabaseHelper(Context context) throws IOException {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
        DB_PATH="/data/data/"
                + mContext.getApplicationContext().getPackageName()
                + "/databases/";
        createDataBase();
    }

    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (dbExist) {
            // do nothing - database already exist
        } else {

            // By calling this method and empty database will be created into
            // the default system path
            // of your application so we are gonna be able to overwrite that
            // database with our database.
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null,
                    SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
            // database does't exist yet.
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    private void copyDataBase() throws IOException {
        // Open your local db as the input stream
        InputStream myInput = mContext.getAssets().open(DB_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;
        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

        // Toast.makeText(myContext, "Copy Done", 300).show();
    }

    public void openDataBase() throws SQLException {
        // Open the database
        String myPath = DB_PATH + DB_NAME;
        myDatabase = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.OPEN_READWRITE);

    }

    @Override
    public synchronized void close() {
        if(myDatabase !=null){
            myDatabase.close();
        }
        super.close();
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

        String query = " CREATE TABLE " + TYPE1_FILTER_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, ";

        for (RetroFilter.ValueType valueType : RetroFilter.ValueType.values()) {
            query += valueType.toString() + " INTEGER NOT NULL, ";
        }
        query += "FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " + TABLE_MAIN_NAME + "(" + COLUMN_ID + ") ON DELETE CASCADE); ";

        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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
            FilterListView filterListView = ((MainActivity) mContext).findViewById(R.id.filterListView);
            position = filterListView.getHorizontalAdapter().getItemCount() - 1;
        }
        db.close();
        return saveFilter(filter, position);
    }

    public int saveFilter(FCameraFilter filter, int position) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        ContentValues valuesForMain = new ContentValues();
        Bitmap bitmap;
        BitmapDrawable drawable;

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

        int lastInsertedId = (int) db.replace(TABLE_MAIN_NAME, null, valuesForMain);

        switch (filter.getClass().getSimpleName()) {
            case TYPE0_DEFAULT_NAME:
                drawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.filter_icon_image);
                bitmap = drawable.getBitmap();
                saveBitmapDrawable(bitmap, lastInsertedId);

                break;
            case TYPE1_FILTER_NAME:
                values.put(COLUMN_ID, lastInsertedId);
                for (RetroFilter.ValueType valueType : RetroFilter.ValueType.values()) {
                    values.put(valueType.toString(), filter.getValueWithType(valueType));
                }
                db.replace(TYPE1_FILTER_NAME, null, values);

                FCameraCapture cameraCapture = ((MainActivity) mContext).getFCameraCapture();
                drawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.filter_icon_image);
                bitmap = cameraCapture.bitmapFiltering(filter, drawable.getBitmap());
                saveBitmapDrawable(bitmap, lastInsertedId);
                break;
        }

        db.close();

        return lastInsertedId;

    }

    private void saveBitmapDrawable(Bitmap bitmap, int lastInsertedId) {
        SQLiteDatabase db = this.getWritableDatabase();
        byte[] data = getByteArrayFromDrawable(bitmap);
        String query = "UPDATE " + TABLE_MAIN_NAME + " SET " + COLUMN_FILTER_ICON + "=? WHERE " + COLUMN_ID + "=" + String.valueOf(lastInsertedId);
        SQLiteStatement p = db.compileStatement(query);
        p.bindBlob(1, data);
        p.execute();
        db.close();
    }

    public FCameraFilter pasteFilter(FCameraFilter receivedFilter, int position) {
        SQLiteDatabase db = this.getWritableDatabase();
        FCameraFilter newFilter;
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
            case TYPE1_FILTER_NAME:
                newFilter = new RetroFilter(mContext, null);
                for (RetroFilter.ValueType valueType : RetroFilter.ValueType.values()) {
                    newFilter.setValueWithType(valueType, receivedFilter.getValueWithType(valueType));
                }
                newFilter.setName(receivedFilter.getName());

                pastedFilterId = saveFilter(newFilter, position + 1);
                pastedFilter = new RetroFilter(mContext, pastedFilterId);
                for (RetroFilter.ValueType valueType : RetroFilter.ValueType.values()) {
                    pastedFilter.setValueWithType(valueType, newFilter.getValueWithType(valueType));
                }
                pastedFilter.setName(newFilter.getName());
                break;
            default:
                break;
        }
        db.close();
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
                    case TYPE1_FILTER_NAME:
                        FCameraFilter filter = new RetroFilter(mContext, id);
                        filter.setName(name);
                        Cursor typeCursor = db.rawQuery("SELECT * FROM " + TYPE1_FILTER_NAME + " WHERE " + COLUMN_ID + "=" + id, null);
                        typeCursor.moveToFirst();
                        for (RetroFilter.ValueType valueType : RetroFilter.ValueType.values()) {
                            filter.setValueWithType(valueType, typeCursor.getInt(typeCursor.getColumnIndex(valueType.toString())));
                        }
                        filterLinkedList.add(filter);
                        break;
                    case TYPE0_DEFAULT_NAME:
                        FCameraFilter defaultFilter = new OriginalFilter(mContext, id, name);
                        filterLinkedList.add(defaultFilter);
                        break;
                    default:
                        break;
                }
                i++;
            } while (cursor.moveToNext());
            Log.d("x", String.valueOf(i) + "repeated this");

        }
        db.close();
        return filterLinkedList;
    }

    public Drawable getFilterIconImage(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_FILTER_ICON + " FROM " + TABLE_MAIN_NAME + " WHERE " + COLUMN_ID + "='" + String.valueOf(id) + "'", null);
        cursor.moveToFirst();
        byte[] b = cursor.getBlob(cursor.getColumnIndex(COLUMN_FILTER_ICON));
        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        Drawable filterIconImage = new BitmapDrawable(mContext.getResources(), bitmap);
        db.close();
        return filterIconImage;
    }

    public void deleteFilter(int id, int position) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MAIN_NAME + " WHERE position > " + position + " ORDER BY position", null);
        if (cursor.moveToFirst()) {
            do {
                int newPosition = cursor.getInt(cursor.getColumnIndex(COLUMN_FILTER_POS));
                db.execSQL("UPDATE " + TABLE_MAIN_NAME + " SET position ='" + String.valueOf(newPosition - 1) + "' WHERE position='" + newPosition + "'");
            } while (cursor.moveToNext());
        }
        db.execSQL("DELETE FROM " + TABLE_MAIN_NAME + " WHERE _id='" + id + "'");
        db.close();
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
        db.close();
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
