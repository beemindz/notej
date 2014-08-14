package com.beemindz.notej;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.beemindz.notej.dao.DaoMaster;
import com.beemindz.notej.dao.DaoSession;

/**
 * Created by Sony on 7/31/2014.
 */
public class MiyoteeApplication extends Application {
  public DaoSession daoSession;

  @Override
  public void onCreate() {
    super.onCreate();
    setupDatabase();
    /*try {
      PackageInfo info = getPackageManager().getPackageInfo(
          "com.beemindz.notej",
          PackageManager.GET_SIGNATURES);
      for (Signature signature : info.signatures) {
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(signature.toByteArray());
        Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
      }
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }*/
  }

  private void setupDatabase() {
    DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "mytodo.db", null);
    SQLiteDatabase db = helper.getWritableDatabase();
    DaoMaster daoMaster = new DaoMaster(db);
    daoSession = daoMaster.newSession();
  }

  public DaoSession getDaoSession() {
    return daoSession;
  }
}
