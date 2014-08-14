package com.beemindz.notej.dao;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import de.greenrobot.dao.DaoLog;

/* Copy this code snippet into your AndroidManifest.xml inside the
<application> element:

    <provider
            android:name="com.beemindz.notej.dao.EntityContentProvider"
            android:authorities="com.beemindz.notej.dao.provider"/>
    */

public class MiyoteeContentProvider extends ContentProvider {

  public static final String AUTHORITY = "com.beemindz.notej.dao.provider";
  public static final String BASE_PATH = "tasks";
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
  public static final Uri CONTENT_ID_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH + "/");
  public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
      + "/" + BASE_PATH;
  public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
      + "/" + BASE_PATH;

  private static final String TABLENAME = TaskDao.TABLENAME;
  private static final String PK = TaskDao.Properties.Id
      .columnName;

  private static final int TASK_DIR = 0;
  private static final int TASK_ID = 1;

  public static final String TASK_DRAFT_BASE_PATH = "task-drafts";
  public static final Uri TASK_DRAFT_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TASK_DRAFT_BASE_PATH);
  public static final Uri TASK_DRAFT_CONTENT_ID_URI = Uri.parse("content://" + AUTHORITY + "/" + TASK_DRAFT_BASE_PATH + "/");
  public static final String TASK_DRAFT_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
      + "/" + TASK_DRAFT_BASE_PATH;
  public static final String TASK_DRAFT_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
      + "/" + TASK_DRAFT_BASE_PATH;

  private static final String TASK_DRAFT_TABLENAME = TaskDraftDao.TABLENAME;
  private static final String TASK_DRAFT_PK = TaskDraftDao.Properties.Id
      .columnName;

  private static final int TASK_DRAFT_DIR = 2;
  private static final int TASK_DRAFT_ID = 3;


  private static final UriMatcher sURIMatcher;

  static {
    sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    sURIMatcher.addURI(AUTHORITY, BASE_PATH, TASK_DIR);
    sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TASK_ID);
    sURIMatcher.addURI(AUTHORITY, TASK_DRAFT_BASE_PATH, TASK_DRAFT_DIR);
    sURIMatcher.addURI(AUTHORITY, TASK_DRAFT_BASE_PATH + "/#", TASK_DRAFT_ID);
  }

  /**
   * This must be set from outside, it's recommended to do this inside your Application object.
   * Subject to change (static isn't nice).
   */
  public static DaoSession daoSession;

  @Override
  public boolean onCreate() {
    if (daoSession == null) {
      DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(getContext(), "mytodo.db", null);
      SQLiteDatabase db = helper.getWritableDatabase();
      DaoMaster daoMaster = new DaoMaster(db);
      daoSession = daoMaster.newSession();

      //throw new IllegalStateException("DaoSession must be set before content provider is created");
    }
    DaoLog.d("Content Provider started: " + CONTENT_URI);
    return true;
  }

  protected SQLiteDatabase getDatabase() {
    if (daoSession == null) {
      throw new IllegalStateException("DaoSession must be set during content provider is active");
    }
    return daoSession.getDatabase();
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    int uriType = sURIMatcher.match(uri);
    long id = 0;
    String path = "";
    switch (uriType) {
      case TASK_DIR:
        id = getDatabase().insert(TABLENAME, null, values);
        path = BASE_PATH + "/" + id;
        break;
      case TASK_DRAFT_DIR:
        id = getDatabase().insert(TASK_DRAFT_TABLENAME, null, values);
        path = TASK_DRAFT_BASE_PATH + "/" + id;
        break;
      default:
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return Uri.parse(path);
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase db = getDatabase();
    int rowsDeleted = 0;
    String id;
    switch (uriType) {
      case TASK_DIR:
        rowsDeleted = db.delete(TABLENAME, selection, selectionArgs);
        break;
      case TASK_ID:
        id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
          rowsDeleted = db.delete(TABLENAME, PK + "=" + id, null);
        } else {
          rowsDeleted = db.delete(TABLENAME, PK + "=" + id + " and "
              + selection, selectionArgs);
        }
        break;
      case TASK_DRAFT_DIR:
        rowsDeleted = db.delete(TASK_DRAFT_TABLENAME, selection, selectionArgs);
        break;
      case TASK_DRAFT_ID:
        id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
          rowsDeleted = db.delete(TASK_DRAFT_TABLENAME, TASK_DRAFT_PK + "=" + id, null);
        } else {
          rowsDeleted = db.delete(TASK_DRAFT_TABLENAME, TASK_DRAFT_PK + "=" + id + " and "
              + selection, selectionArgs);
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return rowsDeleted;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
                    String[] selectionArgs) {
    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase db = getDatabase();
    int rowsUpdated = 0;
    String id;
    switch (uriType) {
      case TASK_DIR:
        rowsUpdated = db.update(TABLENAME, values, selection, selectionArgs);
        break;
      case TASK_ID:
        id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
          rowsUpdated = db.update(TABLENAME, values, PK + "=" + id, null);
        } else {
          rowsUpdated = db.update(TABLENAME, values, PK + "=" + id
              + " and " + selection, selectionArgs);
        }
        break;
      case TASK_DRAFT_DIR:
        rowsUpdated = db.update(TASK_DRAFT_TABLENAME, values, selection, selectionArgs);
        break;
      case TASK_DRAFT_ID:
        id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
          rowsUpdated = db.update(TASK_DRAFT_TABLENAME, values, TASK_DRAFT_PK + "=" + id, null);
        } else {
          rowsUpdated = db.update(TASK_DRAFT_TABLENAME, values, TASK_DRAFT_PK + "=" + id
              + " and " + selection, selectionArgs);
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return rowsUpdated;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
                      String[] selectionArgs, String sortOrder) {

    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
      case TASK_DIR:
        queryBuilder.setTables(TABLENAME);
        break;
      case TASK_ID:
        queryBuilder.setTables(TABLENAME);
        queryBuilder.appendWhere(PK + "="
            + uri.getLastPathSegment());
        break;
      case TASK_DRAFT_DIR:
        queryBuilder.setTables(TASK_DRAFT_TABLENAME);
        break;
      case TASK_DRAFT_ID:
        queryBuilder.setTables(TASK_DRAFT_TABLENAME);
        queryBuilder.appendWhere(TASK_DRAFT_PK + "="
            + uri.getLastPathSegment());
        break;
      default:
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    SQLiteDatabase db = getDatabase();
    Cursor cursor = queryBuilder.query(db, projection, selection,
        selectionArgs, null, null, sortOrder);
    cursor.setNotificationUri(getContext().getContentResolver(), uri);

    return cursor;
  }

  @Override
  public final String getType(Uri uri) {
    switch (sURIMatcher.match(uri)) {
      case TASK_DIR:
        return CONTENT_TYPE;
      case TASK_ID:
        return CONTENT_ITEM_TYPE;
      case TASK_DRAFT_DIR:
        return TASK_DRAFT_CONTENT_TYPE;
      case TASK_DRAFT_ID:
        return TASK_DRAFT_CONTENT_ITEM_TYPE;
      default:
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }
  }
}
