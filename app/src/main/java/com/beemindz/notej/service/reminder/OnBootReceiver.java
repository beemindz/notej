package com.beemindz.notej.service.reminder;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.beemindz.notej.dao.MiyoteeContentProvider;
import com.beemindz.notej.dao.TaskDao;
import com.beemindz.notej.util.Constant;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class OnBootReceiver extends BroadcastReceiver {

  private static final String[] TASK_PROJECTION = new String[]{TaskDao.Properties.Id.columnName, TaskDao.Properties.TaskId.columnName,
      TaskDao.Properties.TaskName.columnName, TaskDao.Properties.TaskDescription.columnName, TaskDao.Properties.ReminderDate.columnName,
      TaskDao.Properties.CreatedDate.columnName, TaskDao.Properties.IsComplete.columnName};

  @SuppressLint("SimpleDateFormat")
  @Override
  public void onReceive(Context context, Intent intent) {
    // TODO Auto-generated method stub
    Log.d("==OnBootReceiver==", "==onReceive==");
    ReminderManager reminderMgr = new ReminderManager();

    Cursor cursor = context.getContentResolver().query(MiyoteeContentProvider.CONTENT_URI, TASK_PROJECTION,
        "ifnull(length(" + TaskDao.Properties.ReminderDate.columnName + "), 0) != ? AND " + TaskDao.Properties.IsComplete.columnName + " = ? ", new String[]{"0", "0"}, null);
    if (cursor != null) {
      cursor.moveToFirst();
      int rowIdColumnIndex = cursor.getColumnIndex(TaskDao.Properties.Id.columnName);
      int dateTimeColumnIndex = cursor.getColumnIndex(TaskDao.Properties.ReminderDate.columnName);
      int titleColumnIndex = cursor.getColumnIndex(TaskDao.Properties.TaskName.columnName);
      int bodyColumnIndex = cursor.getColumnIndex(TaskDao.Properties.TaskDescription.columnName);
//      int reminderColumnIndex = cursor.getColumnIndex(TaskDao.Properties.IsReminder.columnName);
      int completeColumnIndex = cursor.getColumnIndex(TaskDao.Properties.IsComplete.columnName);
      while (!cursor.isAfterLast()) {
        Long rowId = cursor.getLong(rowIdColumnIndex);
        String dateTime = cursor.getString(dateTimeColumnIndex);
        String title = cursor.getString(titleColumnIndex);
        String body = cursor.getString(bodyColumnIndex);

        boolean isComplete = cursor.getInt(completeColumnIndex) == 0 ? false
            : true;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(Constant.DATE_TIME_FORMAT);
        try {
          java.util.Date date = format.parse(dateTime);
          cal.setTime(date);
          if (!isComplete) {
            reminderMgr.setReminder(context, rowId, cal, title, body);
          }
        } catch (Exception e) {
          Log.e("OnBootReceiver", e.getMessage(), e);
        }
        cursor.moveToNext();
      }
      cursor.close();
    }
  }

}
