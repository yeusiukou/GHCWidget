package by.aleks.ghcwidget.data;

/**
 * Created by Alex on 12/16/14.
 */

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;


        import android.content.Context;
        import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class SQLCommitsBase extends SQLiteOpenHelper implements Base {

    final static String TABLE_NAME = "commits";
    final static String _ID = "_id";
    final static String DATE = "date";
    final static String COMMITS_NUMBER = "commits_number";
    final static String LEVEL = "level";
    final static String WEEK = "week";
    final static String[] columns = { _ID, DATE, COMMITS_NUMBER, LEVEL };

    final private static String CREATE_CMD =

            "CREATE TABLE artists (" + _ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + DATE + " TEXT NOT NULL "
                    + COMMITS_NUMBER + " NUMBER NOT NULL "
                    + LEVEL + " NUMBER NOT NULL" +
                    WEEK + " NUMBER NOT NULL )";

    final private static String NAME = "commits_db";
    final private static Integer VERSION = 1;
    final private Context mContext;

    private int currentWeek = -1;

    public SQLCommitsBase(Context context) {
        super(context, NAME, null, VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CMD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // N/A
    }

    void deleteDatabase() {
        mContext.deleteDatabase(NAME);
    }

    public void addDay(Day day){
        ContentValues values = new ContentValues();

        values.put(SQLCommitsBase.DATE, day.getDate());
        values.put(SQLCommitsBase.COMMITS_NUMBER, day.getCommitsNumber());
        values.put(SQLCommitsBase.LEVEL, day.getLevel());
        values.put(SQLCommitsBase.WEEK, currentWeek);
        getWritableDatabase().insert(SQLCommitsBase.TABLE_NAME, null, values);

    }

    public void newWeek(){
        currentWeek++;
    }

    @Override
    public ArrayList<ArrayList<Day>> getWeeks() {
        return null;
    }

    @Override
    public int currentStreak() {
        return 0;
    }

    @Override
    public int commitsNumber() {
        return 0;
    }
}