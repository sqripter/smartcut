package com.mobiotrics.contactless.smartcut;


import android.app.Application;

import org.greenrobot.greendao.database.Database;


public class SmartCut extends Application {
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

//        // regular SQLite database
       DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "smartcut-db");
        Database db = helper.getWritableDb();
//
//        // encrypted SQLCipher database
//        // note: you need to add SQLCipher to your dependencies, check the build.gradle file
//        // DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db-encrypted");
//        // Database db = helper.getEncryptedWritableDb("encryption-key");
//
        daoSession = new DaoMaster(db).newSession();
    }


    public String getSessionUrl(){

        return getString(R.string.baseurl_dev);
    }
   public DaoSession getDaoSession() {
       return daoSession;
    }
}
