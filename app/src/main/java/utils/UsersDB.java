package utils;

/**
 * Created by AdelinGDobre on 5/6/2017.
 */

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UsersDB {

    final String TAG = "UsersDB";

    // database constants
    public static final String DB_NAME = "RunForLife.db";
    public static final int    DB_VERSION = 2;

    //users table constants
    public static final String USERS_TABLE = "users";

    public static final String USERS_ID = "_id";
    public static final int    USERS_ID_COL = 0;

    public static final String USERS_USERNAME = "_username";
    public static final int    USERS_USERNAME_COL = 1;

    public static final String USERS_PASSWORD = "_password";
    public static final int    USERS_PASSWORD_COL = 2;

    public static final String USERS_EMAIL = "_email";
    public static final int    USERS_EMAIL_COL = 3;

    public static final String USERS_PICTURE = "_picture";
    public static final int    USERS_PICTURE_COL = 4;

    public static final String USERS_GENDER = "_gender";
    public static final int    USERS_GENDER_COL = 5;

    public static final String USERS_WEIGHT = "_weight";
    public static final int    USERS_WEIGHT_COL = 6;

    public static final String USERS_HEIGHT = "_height";
    public static final int    USERS_HEIGHT_COL = 7;

    public static final String USERS_HEARTRATE = "_heartRate";
    public static final int    USERS_HEARTRATE_COL = 8;

    public static final String USERS_AGE = "_age";
    public static final int    USERS_AGE_COL = 9;

    public static final String USERS_TAG = "_tag";
    public static final int    USERS_TAG_COL = 10;



    // CREATE and DROP TABLE statements
    public static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + USERS_TABLE + " (" +
                    USERS_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    USERS_USERNAME + " TEXT, "+
                    USERS_PASSWORD + " TEXT, "+
                    USERS_EMAIL    + " TEXT, "+
                    USERS_PICTURE  + " TEXT, "+
                    USERS_GENDER   + " TEXT, "+
                    USERS_WEIGHT   + " TEXT, "+
                    USERS_HEIGHT   + " TEXT, "+
                    USERS_HEARTRATE + " TEXT, "+
                    USERS_AGE      + " TEXT, "+
                    USERS_TAG      + " TEXT);";

    public static final String DROP_USERS_TABLE =
            "DROP TABLE IF EXISTS " + USERS_TABLE;

    public static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name,
                        CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create tables
            db.execSQL(CREATE_USERS_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("Task list", "Upgrading db from version "
                    + oldVersion + " to " + newVersion);

            db.execSQL(UsersDB.DROP_USERS_TABLE);
            onCreate(db);
        }
    }

    //databse and database helper objects
    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public UsersDB(Context context) {
        dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
    }

    private void openReadableDB() {
        db = dbHelper.getReadableDatabase();
    }

    private void openWriteableDB() {
        db = dbHelper.getWritableDatabase();
    }

    private void closeDB() {
        if (db != null)
            db.close();
    }

    public boolean checkCredentials(User user){
        boolean success = false;
        if(isPasswordValid(user)) {
            success = true;
        }
        else
            success = false;

        return success;
    }

    private boolean isPasswordValid(User user){
        boolean success = false;
        String email = user.getEmail();

        //checks if the username exists
        if(emailExists(email)){
            User userFromDB = getUserByEmail(email);

            //checks if the passwords equal to each other
            if(userFromDB.getPassword().equals(user.getPassword()))
                success = true;
            else
                success = false;
        }
        else{
            success = false;
        }
        return success;
    }

    public boolean userExists(String aUser) {
        this.openReadableDB();
        Cursor cur = db.rawQuery("SELECT * FROM " + USERS_TABLE + " WHERE "+USERS_USERNAME+" = '" + aUser + "'", null);
        boolean exist = (cur.getCount() > 0);
        cur.close();
        this.closeDB();
        return exist;
    }

    public boolean emailExists(String email) {
        this.openReadableDB();
        Cursor cur = db.rawQuery("SELECT * FROM " + USERS_TABLE + " WHERE " + USERS_EMAIL + " = '" + email + "'", null);
        boolean exist = (cur.getCount() > 0);
        cur.close();
        this.closeDB();
        return exist;
    }

    private static User getUserFromCursor(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0){
            return null;
        }
        else {
            try {
                User user = new User(
                        cursor.getString(USERS_USERNAME_COL),
                        cursor.getString(USERS_PASSWORD_COL),
                        cursor.getString(USERS_EMAIL_COL),
                        cursor.getString(USERS_PICTURE_COL),
                        cursor.getString(USERS_GENDER_COL),
                        cursor.getString(USERS_WEIGHT_COL),
                        cursor.getString(USERS_HEIGHT_COL),
                        cursor.getString(USERS_HEARTRATE_COL),
                        cursor.getString(USERS_AGE_COL),
                        cursor.getString(USERS_TAG_COL));
                return user;
            }
            catch(Exception e) {
                return null;
            }
        }
    }

    public User getUser(String username) {
        String where = USERS_USERNAME + "= ?";
        String[] whereArgs = { username };

        this.openReadableDB();
        Cursor cursor = db.query(USERS_TABLE,
                null, where, whereArgs, null, null, null);
        cursor.moveToFirst();
        User user = getUserFromCursor(cursor);
        if (cursor != null)
            cursor.close();
        this.closeDB();

        return user;
    }

    public User getUserByEmail(String email){
        String where = USERS_EMAIL + "= ?";
        String[] whereArgs = { email };

        this.openReadableDB();
        Cursor cursor = db.query(USERS_TABLE,
                null, where, whereArgs, null, null, null);
        cursor.moveToFirst();
        User user = getUserFromCursor(cursor);
        if(cursor != null)
            cursor.close();
        this.closeDB();

        return user;
    }

    public long insertUser(User user) {
        ContentValues cv = new ContentValues();
        cv.put(USERS_USERNAME, user.getUsername());
        cv.put(USERS_PASSWORD, user.getPassword());
        cv.put(USERS_EMAIL, user.getEmail());
        cv.put(USERS_PICTURE, "");
        cv.put(USERS_GENDER, "");
        cv.put(USERS_WEIGHT, "");
        cv.put(USERS_HEIGHT, "");
        cv.put(USERS_HEARTRATE, "");
        cv.put(USERS_AGE, "");
        cv.put(USERS_TAG, "");


        this.openWriteableDB();
        long rowID = db.insert(USERS_TABLE, null, cv);
        this.closeDB();

        return rowID;
    }

    public long insertFullProfile(User user) {
        ContentValues cv = new ContentValues();
        cv.put(USERS_USERNAME, user.getUsername());
        cv.put(USERS_PASSWORD, user.getPassword());
        cv.put(USERS_EMAIL, user.getEmail());
        cv.put(USERS_PICTURE, user.getPicture());
        cv.put(USERS_TAG, user.getTag());

        this.openWriteableDB();
        long rowID = db.insert(USERS_TABLE, null, cv);
        this.closeDB();

        return rowID;
    }

    public int updateInfo(String email, String gender, String age, String weight, String height, String heartRate){
        ContentValues data=new ContentValues();
        data.put(USERS_GENDER, gender);
        data.put(USERS_AGE, age);
        data.put(USERS_WEIGHT, weight);
        data.put(USERS_HEIGHT, height);
        data.put(USERS_HEARTRATE, heartRate);
        String selection = USERS_EMAIL  + " = ?";
        String[] selectionArgs = {email};
        this.openWriteableDB();
        int rowIdx = db.update(USERS_TABLE, data, selection, selectionArgs);
        this.closeDB();

        return rowIdx;
    }
}
