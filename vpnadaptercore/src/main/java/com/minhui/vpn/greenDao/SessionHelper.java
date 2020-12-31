package com.minhui.vpn.greenDao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


public class SessionHelper {
    public static final String VIDEO_TABLE = "videoTable.db";
    public static final String VIDEO_FAVORITE_TABLE = "videoFavoriteTable";

    public static DaoSession getDaoSession(Context context, String dbName){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, dbName);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        return daoMaster.newSession();
    }

    public static String getDbName(String lastVpnStartTimeFormat) {
        return "capture_" + lastVpnStartTimeFormat+".db";
    }
}
