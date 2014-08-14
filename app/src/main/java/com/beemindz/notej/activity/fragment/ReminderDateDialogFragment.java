package com.beemindz.notej.activity.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;

import com.beemindz.notej.R;
import com.beemindz.notej.util.CommonUtils;

import java.util.Calendar;

public class ReminderDateDialogFragment extends DialogFragment {

  DatePicker datePicker;
  TimePicker timePicker;
  Button btnDialogSave, btnDialogCancel;
  public static Calendar mCalendar;

  public static ReminderDateDialogFragment newInstance(Calendar calendar) {
    ReminderDateDialogFragment reminderDateDialogFragment = new ReminderDateDialogFragment();
    mCalendar = calendar;
    return reminderDateDialogFragment;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.setTitle(R.string.dialog_choose_reminder_title);
    return dialog;
  }
  @SuppressLint("NewApi")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_dialog_reminder_date, container, false);
    int currentApiVersion = android.os.Build.VERSION.SDK_INT;


    datePicker = (DatePicker) view.findViewById(R.id.datePicker1);
    if (currentApiVersion >= Build.VERSION_CODES.HONEYCOMB) {
      datePicker.setCalendarViewShown(false);
    } else {
    }
    datePicker.init(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), null);

    timePicker = (TimePicker) view.findViewById(R.id.timePicker1);
    timePicker.setIs24HourView(true);
    timePicker.setCurrentHour(mCalendar.get(Calendar.HOUR_OF_DAY));
    timePicker.setCurrentMinute(mCalendar.get(Calendar.MINUTE));


    TabHost tabs = (TabHost) view.findViewById(R.id.tabHost);
    tabs.setup();
    tabs.setCurrentTab(0);

    TabHost.TabSpec tspec11 = tabs.newTabSpec("Tab1");
    tspec11.setIndicator(getResources().getString(R.string.date));

    tspec11.setContent(R.id.tab1);
    tabs.addTab(tspec11);

    TabHost.TabSpec tspec2 = tabs.newTabSpec("Tab2");
    tspec2.setIndicator(getResources().getString(R.string.time));

    tspec2.setContent(R.id.tab2);
    tabs.addTab(tspec2);

    btnDialogSave = (Button) view.findViewById(R.id.btnDialogSave);
    btnDialogSave.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mCalendar.set(Calendar.YEAR, datePicker.getYear());
        mCalendar.set(Calendar.MONTH, datePicker.getMonth());
        mCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
        mCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
        mCalendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        TextView tvTime = (TextView) getActivity().findViewById(R.id.tvReminderDate);
        tvTime.setText(CommonUtils.getStringDate(mCalendar, CommonUtils.getDateTimeFormatSystem(getActivity())));

        dismiss();
      }
    });

    btnDialogCancel = (Button) view.findViewById(R.id.btnDialogCancel);
    btnDialogCancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        TextView tvTime = (TextView) getActivity().findViewById(R.id.tvReminderDate);
        tvTime.setText("");
        dismiss();
      }
    });
    return view;
  }
}
