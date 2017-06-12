package runsdb;

/**
 * Created by AdelinGDobre on 6/5/2017.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocationDBHelper extends SQLiteOpenHelper {
    private final String SQL_CREATE_WAYPOINTS =
            "CREATE TABLE " + RunsContract.Waypoints.TABLE_NAME + " (" +
                    RunsContract.Waypoints._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    RunsContract.Waypoints.COLUMN_NAME_LONGTITUDE + " REAL," +
                    RunsContract.Waypoints.COLUMN_NAME_LATITUDE + " REAL," +
                    RunsContract.Waypoints.COLUMN_NAME_HEIGHT + " REAL," +
                    RunsContract.Waypoints.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    RunsContract.Waypoints.COLUMN_NAME_RUN_ID + " INTEGER," +
                    "FOREIGN KEY(" + RunsContract.Waypoints.COLUMN_NAME_RUN_ID + ") REFERENCES " + RunsContract.Runs.TABLE_NAME + "(" + RunsContract.Runs._ID + "))";
    private final String SQL_CREATE_SECTIONS =
            "CREATE TABLE " + RunsContract.Sections.TABLE_NAME + " (" +
                    RunsContract.Sections._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    RunsContract.Sections.COLUMN_NAME_START_ID + " INTEGER," +
                    RunsContract.Sections.COLUMN_NAME_END_ID + " INTEGER," +
                    RunsContract.Sections.COLUMN_NAME_DISTANCE + " REAL," +
                    RunsContract.Sections.COLUMN_NAME_TIME_INTERVAL + " INTEGER," +
                    RunsContract.Sections.COLUMN_NAME_VELOCITY + " REAL," +
                    RunsContract.Sections.COLUMN_NAME_HEIGHT_INTERVAL + " REAL," +
                    RunsContract.Sections.COLUMN_NAME_RUN_ID  + " INTEGER," +
                    "FOREIGN KEY(" + RunsContract.Sections.COLUMN_NAME_START_ID + ") REFERENCES " + RunsContract.Waypoints.TABLE_NAME + "(" + RunsContract.Waypoints._ID + ")," +
                    "FOREIGN KEY(" + RunsContract.Sections.COLUMN_NAME_END_ID + ") REFERENCES " + RunsContract.Waypoints.TABLE_NAME + "(" + RunsContract.Waypoints._ID + ")," +
                    "FOREIGN KEY(" + RunsContract.Sections.COLUMN_NAME_RUN_ID + ") REFERENCES " + RunsContract.Runs.TABLE_NAME + "(" + RunsContract.Runs._ID + "))";

    private final String SQL_CREATE_RUNS =
            "CREATE TABLE " + RunsContract.Runs.TABLE_NAME + " (" +
                    RunsContract.Runs._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    RunsContract.Runs.COLUMN_NAME_DISTANCE + " REAL," +
                    RunsContract.Runs.COLUMN_NAME_TIME_INTERVAL + " INTEGER," +
                    RunsContract.Runs.COLUMN_NAME_MAX_VELOCITY + " REAL," +
                    RunsContract.Runs.COLUMN_NAME_MED_VELOCITY + " REAL," +
                    RunsContract.Runs.COLUMN_NAME_ASCEND_INTERVAL + " REAL," +
                    RunsContract.Runs.COLUMN_NAME_DESCEND_INTERVAL + " REAL," +
                    RunsContract.Runs.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    RunsContract.Runs.COLUMN_NAME_BREAK_TIME + " INTEGER," +
                    RunsContract.Runs.COLUMN_NAME_USER + " STRING" + " )";

    private final String SQL_DROP_WAYPOINTS = "DROP TABLE IF EXISTS " + RunsContract.Waypoints.TABLE_NAME;
    private final String SQL_DROP_SECTIONS = "DROP TABLE IF EXISTS " + RunsContract.Sections.TABLE_NAME;
    private final String SQL_DROP_RUNS = "DROP TABLE IF EXISTS " + RunsContract.Runs.TABLE_NAME;

    public LocationDBHelper(Context context){
        super(context, RunsContract.DATABASE_NAME, null, RunsContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_RUNS);
        db.execSQL(SQL_CREATE_WAYPOINTS);
        db.execSQL(SQL_CREATE_SECTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //deleteTables(db);
        //onCreate(db);
    }

    private void deleteTables(SQLiteDatabase db){
        db.execSQL(SQL_DROP_WAYPOINTS);
        db.execSQL(SQL_DROP_SECTIONS);
        db.execSQL(SQL_DROP_RUNS);
    }

}
