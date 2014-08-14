package com.beemindz.notej.service.syncadapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.beemindz.notej.dao.MiyoteeContentProvider;
import com.beemindz.notej.dao.TaskDao;
import com.beemindz.notej.dao.TaskDraftDao;
import com.beemindz.notej.util.CommonUtils;
import com.beemindz.notej.util.Constant;
import com.beemindz.notej.util.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

  //TODO REMOVE IsDueDate and IsReminderDate
  // JSON Node names
  public final String TAG_ERROR = "error";
  private final String TAG = "SyncAdapter";
  private final String[] TASK_DRAFT_PROJECTION = new String[]{TaskDraftDao.Properties.Id.columnName,
      TaskDraftDao.Properties.TaskId.columnName, TaskDraftDao.Properties.UserName.columnName, TaskDraftDao.Properties.TaskName.columnName,
      TaskDraftDao.Properties.TaskDescription.columnName, TaskDraftDao.Properties.DueDate.columnName, TaskDraftDao.Properties.ReminderDate.columnName,
      TaskDraftDao.Properties.IsComplete.columnName,
      TaskDraftDao.Properties.CreatedDate.columnName, TaskDraftDao.Properties.UpdatedDate.columnName, TaskDraftDao.Properties.Status.columnName};

  public SyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void onPerformSync(Account account, Bundle bundle, String authority,
                            ContentProviderClient contentProviderClient, SyncResult syncResult) {
    Log.i(TAG, "starting sync");

    String updatedDate = getUpdatedDate(contentProviderClient);
    syncToLocal(account, contentProviderClient, updatedDate);
    syncToServer(account, contentProviderClient);

    Log.i(TAG, "done sync");

  }

  /**
   * @param contentProviderClient
   */
  public void syncToLocal(Account account, ContentProviderClient contentProviderClient, String updated) {
    Log.i(TAG, "begin sync to local");
    String urlGetAllTask = Constant.URL_HOST + "get-all-task.php";

    // Building Parameters
    String[] keys = new String[]{"username", "updatedDate"};
    String[] values = new String[]{account.name, updated};

    try {
      Log.i(TAG, "Last updatedDate : " + updated);
      JSONObject json = NetworkUtils.postJSONObjFromUrl(urlGetAllTask, keys, values);
      if (json != null) {
        // check your log for json response
        Log.d("All task server result", json.toString());

        // json success tag
        boolean error = json.getBoolean(TAG_ERROR);

        if (!error) {

          JSONArray arrTask = json.getJSONArray("task");
          for (int i = 0; i < arrTask.length(); i++) {

            ContentValues contentValues = new ContentValues();
            JSONObject jsonObject = arrTask.getJSONObject(i);

            contentValues.put(TaskDao.Properties.TaskId.columnName, jsonObject.getInt(Constant.JsonNoteName.TASK_ID));
            contentValues.put(TaskDao.Properties.UserName.columnName, account.name);
            contentValues.put(TaskDao.Properties.TaskName.columnName, jsonObject.getString(Constant.JsonNoteName.TASK_NAME));
            contentValues.put(TaskDao.Properties.TaskDescription.columnName,
                jsonObject.getString(Constant.JsonNoteName.TASK_DESCRIPTION));
//            contentValues.put(TaskDao.Properties.IsReminder.columnName, jsonObject.getInt(Constant.JsonNoteName.TASK_IS_REMINDER));
//            contentValues.put(TaskDao.Properties.IsDueDate.columnName, jsonObject.getInt(Constant.JsonNoteName.TASK_IS_DUE_DATE));
            contentValues.put(TaskDao.Properties.IsComplete.columnName, jsonObject.getInt(Constant.JsonNoteName.TASK_IS_COMPLETE));

            String dueDate = jsonObject.getString(Constant.JsonNoteName.TASK_DUE_DATE);
            String reminderDate = jsonObject.getString(Constant.JsonNoteName.TASK_REMINDER_DATE);
            String createdDate = jsonObject.getString(Constant.JsonNoteName.TASK_CREATED_DATE);
            String updatedDate = jsonObject.getString(Constant.JsonNoteName.TASK_UPDATED_DATE);

            if (!TextUtils.isEmpty(reminderDate) && !"0000-00-00 00:00:00".equals(reminderDate)) {
              Long timeMillis = CommonUtils.getTimeMillis(dueDate, Constant.DATE_TIME_FORMAT);
              contentValues.put(TaskDao.Properties.DueDate.columnName, timeMillis);
            }
            if (!TextUtils.isEmpty(reminderDate) && !"0000-00-00 00:00:00".equals(reminderDate)) {
              Long timeMillis = CommonUtils.getTimeMillis(reminderDate, Constant.DATE_TIME_FORMAT);
              contentValues.put(TaskDao.Properties.ReminderDate.columnName, timeMillis);
            }
            if (!TextUtils.isEmpty(createdDate)) {
              Long timeMillis = CommonUtils.getTimeMillis(reminderDate, Constant.DATE_TIME_FORMAT);
              contentValues.put(TaskDao.Properties.CreatedDate.columnName, timeMillis);
            }
            if (!TextUtils.isEmpty(updatedDate) && !"0000-00-00 00:00:00".equals(reminderDate)) {
              Long timeMillis = CommonUtils.getTimeMillis(reminderDate, Constant.DATE_TIME_FORMAT);
              contentValues.put(TaskDao.Properties.UpdatedDate.columnName, timeMillis);
            }

            // Kiểm tra tồn tại task?
            Cursor cursor = contentProviderClient.query(MiyoteeContentProvider.CONTENT_URI, new String[]{TaskDao.Properties.Id.columnName},
                TaskDao.Properties.TaskId.columnName + " = ? ", new String[]{jsonObject.getString(Constant.JsonNoteName.TASK_ID)}, "_ID DESC LIMIT(1)");

            if (cursor.getCount() > 0) {
              cursor.moveToFirst();
              Log.i(TAG, "Count Cursor : " + cursor.getCount());
              // Local taskId
              Long id = cursor.getLong(cursor.getColumnIndex(TaskDao.Properties.Id.columnName));
              // Thực hiện update
              Uri updateUri = Uri.withAppendedPath(MiyoteeContentProvider.CONTENT_ID_URI, id.toString());
              Log.i(TAG, updateUri.toString());
              contentProviderClient.update(updateUri, contentValues, null, null);
            } else {
              // Thực hiện insert
              Log.i(TAG, "Inserting task");
              contentProviderClient.insert(MiyoteeContentProvider.CONTENT_URI, contentValues);
              Log.i(TAG, "Inserted task");
            }
          }
        } else {
          Log.i(TAG, json.getString("message"));
        }
      }

    } catch (JSONException e) {
      e.printStackTrace();
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    Log.i(TAG, "end sync to local");
  }

  /**
   * @param contentProviderClient
   * @return
   */
  private String getUpdatedDate(ContentProviderClient contentProviderClient) {
    try {
      Cursor cursor = contentProviderClient.query(MiyoteeContentProvider.CONTENT_URI,
          new String[]{TaskDao.Properties.UpdatedDate.columnName}, TaskDao.Properties.TaskId.columnName + " > ? ",
          new String[]{"0"}, TaskDao.Properties.UpdatedDate.columnName + " DESC LIMIT(1)");
      if (cursor.getCount() > 0) {
        cursor.moveToFirst();

        int colUpdateIndex = cursor.getColumnIndex(TaskDao.Properties.UpdatedDate.columnName);
        Long timeInMillis = cursor.getLong(colUpdateIndex);
        Date date = new Date();
        date.setTime(timeInMillis);

        return CommonUtils.getStringDate(date, Constant.DATE_TIME_FORMAT);
      }
    } catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return "";
  }

  /**
   * @param contentProviderClient
   */
  private void syncToServer(Account account, ContentProviderClient contentProviderClient) {
    Log.i(TAG, "begin sync to server");
    // Building Parameters
    String[] keys = new String[]{"username", "taskId", "taskName", "taskDescription", "dueDate", "reminderDate", "isReminder", "isDueDate",
        "isComplete", "updatedDate"};
    boolean noteError;

    try {
      // Uri uri = Uri.withAppendedPath(MyToDo.Tasks.CONTENT_DRAP_URI_BASE, "0");
      Cursor cursor = contentProviderClient.query(MiyoteeContentProvider.TASK_DRAFT_CONTENT_URI, TASK_DRAFT_PROJECTION,
          null, null, null);

      if (cursor.getCount() > 0) {
        Log.i(TAG, "Count cursor : " + cursor.getCount());
        cursor.moveToFirst();

        int colIdIndex = cursor.getColumnIndex(TaskDraftDao.Properties.Id.columnName);
        int colTaskIdIndex = cursor.getColumnIndex(TaskDraftDao.Properties.TaskId.columnName);
        int colNameIndex = cursor.getColumnIndex(TaskDraftDao.Properties.TaskName.columnName);
        int colDescriptionIndex = cursor.getColumnIndex(TaskDraftDao.Properties.TaskDescription.columnName);
        int colDueDateIndex = cursor.getColumnIndex(TaskDraftDao.Properties.DueDate.columnName);
        int colReminderIndex = cursor.getColumnIndex(TaskDraftDao.Properties.ReminderDate.columnName);
//        int colIsReminderIndex = cursor.getColumnIndex(TaskDraftDao.Properties.IsReminder.columnName);
//        int colIsDueDateIndex = cursor.getColumnIndex(TaskDraftDao.Properties.IsDueDate.columnName);
        int colIsCompleteIndex = cursor.getColumnIndex(TaskDraftDao.Properties.IsComplete.columnName);
        int colUpdatedDateIndex = cursor.getColumnIndex(TaskDraftDao.Properties.UpdatedDate.columnName);
        int colStatusIndex = cursor.getColumnIndex(TaskDraftDao.Properties.Status.columnName);

        do {
          Long id = cursor.getLong(colIdIndex);
          Long taskId = cursor.getLong(colTaskIdIndex);
          String taskName = cursor.getString(colNameIndex);
          String taskDescription = cursor.getString(colDescriptionIndex);
          String dueDate = CommonUtils.getStringDate(cursor.getLong(colDueDateIndex), Constant.DATE_TIME_FORMAT);
          String reminderDate = CommonUtils.getStringDate(cursor.getLong(colReminderIndex), Constant.DATE_TIME_FORMAT);
//          Long isReminder = cursor.getLong(colIsReminderIndex);
//          Long isDueDate = cursor.getLong(colIsDueDateIndex);
          Long isComplete = cursor.getLong(colIsCompleteIndex);
          String updatedDate = CommonUtils.getStringDate(cursor.getLong(colUpdatedDateIndex), Constant.DATE_TIME_FORMAT);
          Long status = cursor.getLong(colStatusIndex);

          String[] values = new String[]{account.name, taskId.toString(), taskName, taskDescription, dueDate, reminderDate, "0", "0",
              isComplete.toString(), updatedDate};

          switch (status.intValue()) {

            case Constant.TASK_DRAFT_STATUS_INSERT:

              String urlAddTask = Constant.URL_HOST + "add-task.php";

              JSONObject addTaskResult = NetworkUtils.postJSONObjFromUrl(urlAddTask, keys, values);
              // check your log for json response
              Log.d("task add result", addTaskResult.toString());

              // json success tag
              noteError = addTaskResult.getBoolean(TAG_ERROR);

              if (!noteError) {
                JSONArray arrTask = addTaskResult.getJSONArray("task");
                for (int i = 0; i < arrTask.length(); i++) {

                  ContentValues contentValues = new ContentValues();
                  JSONObject jsonObject = arrTask.getJSONObject(i);

                  Log.i(TAG, "Start Update task : " + id);
                  contentValues.put(TaskDao.Properties.TaskId.columnName, jsonObject.getInt(Constant.JsonNoteName.TASK_ID));
                  // TODO contentValues.put(MyToDo.Tasks.COLUMN_NAME_IS_DRAFT, 1);
                  Uri uriUpdateTask = Uri.withAppendedPath(MiyoteeContentProvider.CONTENT_ID_URI, String.valueOf(id));
                  contentProviderClient.update(uriUpdateTask, contentValues, null, null);

                  Log.i(TAG, "Start delete task draft : " + id);
                  Uri uriDeleteTaskDraft = Uri
                      .withAppendedPath(MiyoteeContentProvider.TASK_DRAFT_CONTENT_ID_URI, String.valueOf(id));
                  contentProviderClient.delete(uriDeleteTaskDraft, null, null);
                  Log.i(TAG, "End delete task draft");

                  Log.i(TAG, "End Update task");
                }

              } else {
                Log.i(TAG, addTaskResult.getString("message"));
              }
              break;

            case Constant.TASK_DRAFT_STATUS_UPDATE:
              String urlUpdateTask = Constant.URL_HOST + "update-task.php";

              JSONObject updateTaskResult = NetworkUtils.postJSONObjFromUrl(urlUpdateTask, keys, values);
              // check your log for json response
              Log.d("task update result", updateTaskResult.toString());

              // json success tag
              noteError = updateTaskResult.getBoolean(TAG_ERROR);

              if (!noteError) {
                JSONArray arrTask = updateTaskResult.getJSONArray("task");
                for (int i = 0; i < arrTask.length(); i++) {
                  Log.i(TAG, "Start delete task draft : " + id);
                  Uri uriDeleteTaskDraft = Uri
                      .withAppendedPath(MiyoteeContentProvider.TASK_DRAFT_CONTENT_ID_URI, String.valueOf(id));
                  contentProviderClient.delete(uriDeleteTaskDraft, null, null);
                  Log.i(TAG, "End delete task draft");
                }

              } else {
                Log.i(TAG, updateTaskResult.getString("message"));
              }
              break;

            case Constant.TASK_DRAFT_STATUS_DELETE:
              String urlDeleteTask = Constant.URL_HOST + "delete-task.php";

              String[] keysDelete = new String[]{"username", "taskId"};
              String[] valuesDelete = new String[]{account.name, taskId.toString()};
              JSONObject deleteTaskResult = NetworkUtils.postJSONObjFromUrl(urlDeleteTask, keysDelete, valuesDelete);
              // check your log for json response
              Log.d("task delete result", deleteTaskResult.toString());

              // json success tag
              noteError = deleteTaskResult.getBoolean(TAG_ERROR);

              if (!noteError) {
                Log.i(TAG, "Start delete task draft");
                Uri uriDeleteTaskDraft = Uri.withAppendedPath(MiyoteeContentProvider.TASK_DRAFT_CONTENT_ID_URI, String.valueOf(id));
                contentProviderClient.delete(uriDeleteTaskDraft, null, null);
                Log.i(TAG, "End delete task draft");
              } else {
                Log.i(TAG, deleteTaskResult.getString("message"));
              }
              break;

            default:
              break;
          }

        } while (cursor.moveToNext());

      }
    } catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Log.i(TAG, "end sync to server");
  }

}
