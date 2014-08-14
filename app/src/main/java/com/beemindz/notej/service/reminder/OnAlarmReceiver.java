package com.beemindz.notej.service.reminder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.beemindz.notej.R;
import com.beemindz.notej.activity.SettingActivity;
import com.beemindz.notej.dao.Task;
import com.beemindz.notej.dao.TaskRepository;
import com.beemindz.notej.util.CommonUtils;
import com.beemindz.notej.util.Constant;
import com.beemindz.notej.util.ToastUtils;

import java.util.Calendar;

/**
 * @author Sony
 */
public class OnAlarmReceiver extends BroadcastReceiver {

  private static final String TAG = "OnAlarmReceiver";
  private MediaPlayer mediaPlayer;
  private SharedPreferences sharedPref;

  /*
   * (non-Javadoc)
   * 
   * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
   * android.content.Intent)
   */
  @Override
  public void onReceive(Context context, Intent intent) {

    Log.i(TAG, "begin receive");

    final long taskId = intent.getExtras().getLong(Constant.KEY_TASK_ID);
    final String name = intent.getExtras().getString(Constant.KEY_TASK_NAME);
    final String description = intent.getExtras().getString(Constant.KEY_TASK_DESCRIPTION);
    final long dueDate = intent.getExtras().getLong(Constant.KEY_TASK_REMINDER_DATE);

    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    // thong bao reminder bang sound
    playMedia(context);
    // hien thi dialog reminder
    //showAlarmDialog(context, rowid, name, description);
    onCreateDialog(context, taskId, name, dueDate, description);

    Log.i(TAG, "end receive");
  }

  /**
   * play sound alarm
   *
   * @param context {@link android.content.Context}
   */
  private void playMedia(Context context) {
    try {
      Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
      mediaPlayer = new MediaPlayer();
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      mediaPlayer.setDataSource(context, uri);
      mediaPlayer.prepare();
      mediaPlayer.setLooping(true);
      mediaPlayer.start();
      // set vibrator in setting.
      Boolean isVibrator = sharedPref.getBoolean(SettingActivity.KEY_PREF_VIBRATOR, true);
      if (isVibrator) {
        new ReminderManager().setVibrator(context);
      }
    } catch (Exception e) {
      Log.d("==ERROR==", "" + e);
    }
  }

  private void onCreateDialog(final Context context, final long taskId, final String name, final long timeInMillis, final String description) {
    AlertDialog dialogDetails = null;
    LayoutInflater inflater = LayoutInflater.from(context);
    View dialogview = inflater.inflate(R.layout.alarm_custom_dialog, null);

    AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(context);

    dialogbuilder.setCancelable(false);
    dialogbuilder.setView(dialogview);
    dialogDetails = dialogbuilder.create();

    final AlertDialog alertDialog = (AlertDialog) dialogDetails;
    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    Button snoozebutton = (Button) dialogview.findViewById(R.id.btn_snooze);
    Button cancelbutton = (Button) dialogview.findViewById(R.id.btn_cancel);
    final TextView taskDescription = (TextView) dialogview.findViewById(R.id.alarm_description);
    TextView title = (TextView) dialogview.findViewById(R.id.tvTaskTitle);
    String nameTask = name != null ? name.trim() : "Miyotee";
    if (nameTask.length() > 15) {
      nameTask = String.format("%s ...", nameTask.substring(0, 15));
    }
    title.setText(nameTask);

    if (!TextUtils.isEmpty(description)) {
      taskDescription.setText(description != null ? description.trim() : "");
      taskDescription.setGravity(Gravity.LEFT);
    } else {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(timeInMillis);

      String strHour = CommonUtils.getStringDate(cal, Constant.TIME_FORMAT);
      taskDescription.setText(strHour);
      taskDescription.setTextSize(50);
      taskDescription.setPadding(0, 10, 0, 10);
      taskDescription.setGravity(Gravity.CENTER);
    }

    // BEGIN: EVENT BUTTON CLICK.
    snoozebutton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        mediaPlayer.stop();
        new ReminderManager().cancelVibrator(context);
        new ReminderManager().cancelAlarm(context, taskId, name);
        setReminder(context, taskId, name, description);
        alertDialog.dismiss();
        Log.i(TAG, "SLEEP" + taskId);
      }
    });

    cancelbutton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        mediaPlayer.stop();
        new ReminderManager().cancelVibrator(context);
        // cancel regular alarms
        new ReminderManager().cancelAlarm(context, taskId, name);
        alertDialog.dismiss();
      }
    });
    // END.

    // BEGIN: ON KEY LISTENER
    alertDialog.setOnKeyListener(new OnKeyListener() {

      @Override
      public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
          // stop media player.
          mediaPlayer.stop();
          // cancel regular alarms
          new ReminderManager().cancelAlarm(context, taskId, name);
          dialog.dismiss();
          return true;
        }

        return false;
      }
    });
    // END.

    alertDialog.show();
  }


  /**
   * Update to table task
   *
   * @param taskId      Id of task
   * @param name        name of task
   * @param description description of task
   */
  @SuppressLint("SimpleDateFormat")
  protected void setReminder(Context context, long taskId, String name, String description) {
    String minuteStr = sharedPref.getString(SettingActivity.KEY_PREF_SNOOZE_MINUTE, "5");
    int minutes = Integer.parseInt(minuteStr);
    Calendar mCalendar = Calendar.getInstance();
    mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH));
    mCalendar.add(Calendar.MINUTE, minutes);

    Log.i(TAG, mCalendar.get(Calendar.MINUTE) + " ---");
    Log.i(
        TAG,
        "update to task" + mCalendar.get(Calendar.YEAR) + "-" + mCalendar.get(Calendar.MONTH) + "/"
            + mCalendar.get(Calendar.DAY_OF_MONTH) + "|" + mCalendar.get(Calendar.HOUR_OF_DAY) + ":" + minutes);

    Task task = TaskRepository.getTaskForId(context, taskId);
    task.setTaskName(name);
    task.setTaskDescription(description);
    task.setReminderDate(mCalendar.getTime());
    task.setUpdatedDate(Calendar.getInstance().getTime());
    long count = TaskRepository.insertOrUpdate(context, task);

    if (count > 0) {
      //new ReminderManager().setReminder(context, taskId, mCalendar, name, description);
      new ReminderManager().setUpdateReminder(context, taskId, mCalendar, name, description);
      String msg = context.getResources().getString(R.string.toast_msg_reminder_snooze);
      ToastUtils.toast(context, String.format(msg, name != null ? name.trim() : "Miyotee", minutes));
    }
  }
}
