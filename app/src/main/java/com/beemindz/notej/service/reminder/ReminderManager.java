package com.beemindz.notej.service.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

import com.beemindz.notej.util.Constant;

import java.util.Calendar;

public class ReminderManager {
  private static final String TAG = "ReminderManager";

  public ReminderManager() {
  }

  public void cancelAlarm(Context mContext, Long taskId, String name) {
    // cancel regular alarms
    PendingIntent pi = getPendingIntent(mContext, taskId.intValue());
    AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(pi);
    pi.cancel();

    // cancel Reminder Alarm
    Intent intent = new Intent(mContext, OnAlarmReceiver.class);
    intent.putExtra(Constant.KEY_TASK_ID, (long) taskId);
    intent.putExtra(Constant.KEY_TASK_NAME, name);
    pi = PendingIntent.getBroadcast(mContext, taskId.intValue(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(pi);
    pi.cancel();
  }

  public void destroyAlarm(Context context, int taskId) {
    Log.d(TAG + "==DESTROY ALARM==", "TaskId=" + taskId);
    Intent intentsOpen = new Intent(context, OnAlarmReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, taskId, intentsOpen, PendingIntent.FLAG_ONE_SHOT);
    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, taskId, intentsOpen,
        PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(pendingIntent);
    alarmManager.cancel(pendingIntent1);
    pendingIntent.cancel();
    pendingIntent1.cancel();
  }

  /**
   * Reads preferences, and schedule a procrastinator alarm for a past due task.
   */
  public void setUpdateReminder(Context mContext, Long taskId, Calendar calendar, String name, String des) {
    Intent intent = new Intent(mContext, OnAlarmReceiver.class).putExtra(Constant.KEY_TASK_ID, (long) taskId).putExtra(
        Constant.KEY_TASK_NAME, name);
    intent.putExtra(Constant.KEY_TASK_DESCRIPTION, des);
    intent.putExtra(Constant.KEY_TASK_REMINDER_DATE, calendar.getTimeInMillis());

    Log.i(TAG, "setUpdateReminder taskId : " + taskId + ",name : " + name + ",mCalendar:" + calendar.getTimeInMillis());
    AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
        PendingIntent.getBroadcast(mContext, taskId.intValue(), intent, PendingIntent.FLAG_UPDATE_CURRENT));
  }

  /**
   * Reads preferences, and schedule a reminder alarm for a past due task
   */
  public void setReminder(Context mContext, Long taskId, Calendar dueCal, String name, String description) {
    Intent intent = new Intent(mContext, OnAlarmReceiver.class).putExtra(Constant.KEY_TASK_ID, (long) taskId)
        .putExtra(Constant.KEY_TASK_NAME, name).putExtra(Constant.KEY_TASK_DESCRIPTION, description)
        .putExtra(Constant.KEY_TASK_REMINDER_DATE, dueCal.getTimeInMillis());
    Log.i(TAG, "setReminder taskId : " + taskId + ",name : " + name + ",mCalendar:" + dueCal.getTimeInMillis());
    AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    am.set(AlarmManager.RTC_WAKEUP, dueCal.getTimeInMillis(),
        PendingIntent.getBroadcast(mContext, taskId.intValue(), intent, PendingIntent.FLAG_ONE_SHOT));
  }

  public void setVibrator(Context context) {
    Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//    if (v.hasVibrator()) {
//      v.cancel();
//    }
    v.vibrate(Constant.VIBRATE_MINUTE);
  }

  public void cancelVibrator(Context context) {
    Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    v.cancel();
  }

  // get a PendingIntent
  PendingIntent getPendingIntent(Context context, int id) {
    Intent intent = new Intent(context, OnAlarmReceiver.class).putExtra(Constant.KEY_TASK_ID, id);
    return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
