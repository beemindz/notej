package com.beemindz.notej.activity.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beemindz.notej.R;
import com.beemindz.notej.dao.Task;
import com.beemindz.notej.dao.TaskRepository;
import com.beemindz.notej.service.reminder.ReminderManager;
import com.beemindz.notej.util.CommonUtils;
import com.beemindz.notej.util.Constant;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.Calendar;
import java.util.Date;

public class TaskEditorFragment extends Fragment implements View.OnClickListener {
  final static String ARG_TASK_ID = "TaskId";
  // For logging and debugging
  private static final String TAG = "TasksEditorActivity";
  private EditText etName, etDescription;
  private TextView tvDueDate, tvReminderDate;
  private RelativeLayout layoutReminderDate, layoutDueDate;
  private Button btnDelete;
  private CheckBox cbIsComplete;
  private ImageButton btnDueDate, btnReminderDate;

  /**
   * The view to show the ad.
   */
  private AdView adView;

  //  private static Calendar mCalendar;
  private long mTaskId;
  //  private boolean isReminder;
  private Task task;
  private Calendar mCalendarDueDate, mCalendarReminderDate;

  public TaskEditorFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param taskId Id of task
   * @return A new instance of fragment TaskEditorFragment.
   */
  public static TaskEditorFragment newInstance(long taskId) {
    TaskEditorFragment fragment = new TaskEditorFragment();
    Bundle args = new Bundle();
    args.putLong(ARG_TASK_ID, taskId);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      mTaskId = getArguments().getLong(ARG_TASK_ID);
    }
//    mCalendar = Calendar.getInstance();
    task = new Task();
    customActionBar();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    Log.d(TAG, "On Create View");
    View view = inflater.inflate(R.layout.fragment_task_editor, container, false);
    // Gets a handle to the EditText in the the layout.
    etName = (EditText) view.findViewById(R.id.etName);
    etDescription = (EditText) view.findViewById(R.id.etDescription);
    tvDueDate = (TextView) view.findViewById(R.id.tvDueDate);
    tvReminderDate = (TextView) view.findViewById(R.id.tvReminderDate);
    layoutDueDate = (RelativeLayout) view.findViewById(R.id.layoutDueDate);
    layoutReminderDate = (RelativeLayout) view.findViewById(R.id.layoutReminderDate);
    btnDelete = (Button) view.findViewById(R.id.btnDeleteTask);
    cbIsComplete = (CheckBox) view.findViewById(R.id.cbComplete);
    btnDueDate = (ImageButton) view.findViewById(R.id.btnSelectDate);
    btnReminderDate = (ImageButton) view.findViewById(R.id.btnSelectTime);
    mCalendarDueDate = Calendar.getInstance();
    mCalendarReminderDate = Calendar.getInstance();

    layoutDueDate.setOnClickListener(this);
    layoutReminderDate.setOnClickListener(this);
//    btnDueDate = (ImageButton) view.findViewById(R.id.btnSelectDate);
//    btnReminderDate = (ImageButton) view.findViewById(R.id.btnSelectTime);
//    btnDueDate.setOnClickListener(this);
//    btnReminderDate.setOnClickListener(this);
    btnDelete.setOnClickListener(this);

    updateTaskView(mTaskId);
    initAdModule(view);
    onClickCalendar();
    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    EasyTracker.getInstance(getActivity()).activityStart(getActivity());
  }

  @Override
  public void onResume() {
    Log.d(TAG, "On Resume");
    super.onResume();
    if (adView != null) {
      adView.resume();
    }

  }

  @Override
  public void onStop() {
    super.onStop();
    EasyTracker.getInstance(getActivity()).activityStop(getActivity());
  }

  @Override
  public void onPause() {
    // Destroy the AdView.
    if (adView != null) {
      adView.pause();
    }
    super.onPause();
    Log.d(TAG, "On Pause");
  }

  @Override
  public void onDestroy() {
    // Destroy the AdView.
    if (adView != null) {
      adView.destroy();
    }
    super.onDestroy();
  }

  public void updateTaskView(long taskId) {
    task = TaskRepository.getTaskForId(getActivity(), taskId);
    if (task != null) {
      etName.setTextKeepState(task.getTaskName().trim());
      etName.setSelection(task.getTaskName().trim().length());
      getActivity().getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
      if (!TextUtils.isEmpty(task.getTaskDescription())) {
        etDescription.setTextKeepState(task.getTaskDescription().trim());
      }

      if (task.getDueDate() != null) {
        mCalendarDueDate.setTime(task.getDueDate());
        tvDueDate.setText(CommonUtils.getStringDate(mCalendarDueDate, CommonUtils.getDateFormatSystem(getActivity())));
      }
      if (task.getReminderDate() != null) {
        mCalendarReminderDate.setTime(task.getReminderDate());
        tvReminderDate.setText(CommonUtils.getStringDate(mCalendarReminderDate, CommonUtils.getDateTimeFormatSystem(getActivity())));
      }

      cbIsComplete.setChecked(task.getIsComplete());
    }
  }

  private ActionBar getActionBar() {
    return ((ActionBarActivity) getActivity()).getSupportActionBar();
  }

  /**
   * Custom action bar.
   */
  public void customActionBar() {
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowCustomEnabled(true);

      actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
      actionBar.setCustomView(R.layout.editor_task_action_bar);

      Button btnCancel = (Button) getActivity().findViewById(R.id.btnCancel);
      Button btnOk = (Button) getActivity().findViewById(R.id.btnOK);

      btnCancel.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
          getActivity().getSupportFragmentManager().popBackStack();
        }
      });

      btnOk.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          updateTask(task);
          getActivity().getSupportFragmentManager().popBackStack();
        }
      });
    }
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {

      case R.id.layoutDueDate:
        Log.d(TAG, "layoutDueDate selected");
        DialogFragment dueDateDialogFragment = DueDateDialogFragment.newInstance(mCalendarDueDate);
        dueDateDialogFragment.show(getActivity().getSupportFragmentManager(), "DueDateDialogFragment");
        break;
      case R.id.layoutReminderDate:
        Log.d(TAG, "layoutReminderDate selected");
        DialogFragment reminderDateDialogFragment = ReminderDateDialogFragment.newInstance(mCalendarReminderDate);
        reminderDateDialogFragment.show(getActivity().getSupportFragmentManager(), "ReminderDateDialogFragment");

        break;
      case R.id.btnDeleteTask:
        confirmDelete().show();
        break;
    }
  }

  private void onClickCalendar() {
    btnDueDate.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        DialogFragment dueDateDialogFragment = DueDateDialogFragment.newInstance(mCalendarDueDate);
        dueDateDialogFragment.show(getActivity().getSupportFragmentManager(), "DueDateDialogFragment");
      }
    });

    btnReminderDate.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        DialogFragment reminderDateDialogFragment = ReminderDateDialogFragment.newInstance(mCalendarReminderDate);
        reminderDateDialogFragment.show(getActivity().getSupportFragmentManager(), "ReminderDateDialogFragment");
      }
    });
  }

  private void initAdModule(View view) {
    // Create an ad.
    adView = new AdView(getActivity());

    adView.setAdSize(AdSize.BANNER);
    adView.setAdUnitId(Constant.ADMOD_UNIT_ID);

    // Add the AdView to the view hierarchy. The view will have no size
    // until the ad is loaded.
    LinearLayout layout = (LinearLayout) view.findViewById(R.id.linearEditor);
    layout.addView(adView);

    // Create an ad request. Check logcat output for the hashed device ID to
    // get test ads on a physical device.
    final AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        .addTestDevice("INSERT_YOUR_HASHED_DEVICE_ID_HERE").build();

    // Start loading the ad in the background.
    adView.loadAd(adRequest);
  }

  private void updateTask(Task task) {
    // validate input.
    int valid = validateInputName(etName.getText().toString());
    // case: input not correct.
    if (valid != 0) {
      return;
    } else {
      if (task != null) {
        task.setTaskName(etName.getText().toString());
        task.setTaskDescription(etDescription.getText().toString());
        task.setIsComplete(cbIsComplete.isChecked());
        if (!TextUtils.isEmpty(tvDueDate.getText().toString())) {
          Date date = CommonUtils.getDate(tvDueDate.getText().toString(), CommonUtils.getDateFormatSystem(getActivity()));
          task.setDueDate(date);
          mCalendarDueDate.setTime(date);
          task.setIsDueDate(true);
        } else {
          task.setDueDate(null);
          task.setIsDueDate(false);
        }

        if (!TextUtils.isEmpty(tvReminderDate.getText().toString())) {
          Date date = CommonUtils.getDate(tvReminderDate.getText().toString(), CommonUtils.getDateTimeFormatSystem(getActivity()));
          task.setReminderDate(date);
          mCalendarDueDate.setTime(date);
          task.setIsReminder(false);

          if (!cbIsComplete.isChecked() && mCalendarDueDate.getTimeInMillis() > System.currentTimeMillis()) {
            new ReminderManager().setReminder(getActivity(), task.getId(), mCalendarDueDate,
                task.getTaskName(), task.getTaskDescription());
            task.setIsReminder(true);
          }
        } else {
          task.setReminderDate(null);
          task.setIsReminder(false);
        }


        task.setUpdatedDate(Calendar.getInstance().getTime());

        TaskRepository.insertOrUpdate(getActivity(), task);
      }
    }

  }

  /**
   * Validate input task. + name: required. + description: required. + date &
   * time: required.
   */
  private int validateInputName(String name) {
    // case: name null.
    if (TextUtils.isEmpty(name)) {
      return R.string.toast_err_task_name_required;
    }

    return 0;
  }

  /*--Confirm dialog delete--*/
  private AlertDialog confirmDelete() {
    return CommonUtils.confirmDelete(getActivity(), new android.content.DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        // your deleting code
        TaskRepository.deleteTaskWithId(getActivity().getApplicationContext(), mTaskId);
        dialog.dismiss();
        new ReminderManager().destroyAlarm(getActivity(), (int) mTaskId);
        getActivity().getSupportFragmentManager().popBackStack();
      }
    }, new android.content.DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
  }
}

