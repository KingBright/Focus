package name.kingbright.android.brilliant.storage;

import android.database.sqlite.SQLiteOpenHelper;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import rx.schedulers.Schedulers;

/**
 * @author Jin Liang
 * @since 16/3/11
 */
public class BStorage {
    private static BStorage ourInstance = new BStorage();

    public static BStorage getInstance() {
        return ourInstance;
    }

    private BriteDatabase db;

    private BStorage() {
    }

    public void craete(SQLiteOpenHelper openHelper) {
        SqlBrite sqlBrite = SqlBrite.create();
        db = sqlBrite.wrapDatabaseHelper(openHelper, Schedulers.io());
    }
}
