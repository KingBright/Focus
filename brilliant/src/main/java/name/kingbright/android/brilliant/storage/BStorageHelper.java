package name.kingbright.android.brilliant.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Jin Liang
 * @since 16/3/11
 */
public class BStorageHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public BStorageHelper(Context context) {
        super(context, getDBName(context), null, DATABASE_VERSION);
    }

    private static String getDBName(Context context) {
        return context.getPackageName();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
