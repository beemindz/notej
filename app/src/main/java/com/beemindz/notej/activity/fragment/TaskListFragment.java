package com.beemindz.notej.activity.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.beemindz.notej.R;
import com.beemindz.notej.activity.adapter.Item;
import com.beemindz.notej.activity.adapter.TaskListAdapter;
import com.beemindz.notej.dao.Task;
import com.beemindz.notej.dao.TaskRepository;
import com.beemindz.notej.service.reminder.ReminderManager;
import com.beemindz.notej.util.CommonUtils;
import com.google.analytics.tracking.android.EasyTracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskListFragment extends ListFragment {

  private final String TAG = "TaskListFragment";
  private EditText etTitle;
  private ImageButton btnAddTask;
  private ListView listView;
  private List<Item> items;

  private OnTaskSelectedListener mListener;

  public interface OnTaskSelectedListener {
    public void onTaskSelected(long taskId);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    items = new ArrayList<Item>();
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    registerForContextMenu(getListView());
  }

  /**
   * Custom action bar.
   */
  @SuppressLint("InflateParams")
  public void customActionBar() {
    ActionBar actionBar = getActionBar();
    actionBar.setDisplayShowCustomEnabled(true);
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);

    LayoutInflater inflator = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = inflator.inflate(R.layout.add_task_list_action_bar, null);

    btnAddTask = (ImageButton) v.findViewById(R.id.btnAddTask);
    etTitle = (EditText) v.findViewById(R.id.etTitle);

    btnAddTask = (ImageButton) v.findViewById(R.id.btnAddTask);
    etTitle = (EditText) v.findViewById(R.id.etTitle);

    btnAddTask.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View arg0) {
        Log.d(TAG, "btnAddTask onclick");
        String title = etTitle.getText().toString().trim();
        if (!TextUtils.isEmpty(title)) {
          Task task = new Task();
          task.setTaskName(title);

          task.setIsReminder(false);
          task.setIsComplete(false);
          task.setIsDueDate(false);
          //task.setDueDate(Calendar.getInstance().getTime());
          task.setDueDate(null);
          task.setReminderDate(null);
          task.setUpdatedDate(Calendar.getInstance().getTime());
          task.setCreatedDate(Calendar.getInstance().getTime());
          TaskRepository.insertOrUpdate(getActivity().getApplicationContext(), task);

          etTitle.setText("");
          updateAdapter();
        }
      }
    });

    actionBar.setCustomView(v);
  }

  private ActionBar getActionBar() {
    return ((ActionBarActivity) getActivity()).getSupportActionBar();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_task_list, container, false);
    listView = (ListView) rootView.findViewById(android.R.id.list);
    customActionBar();
    updateAdapter();
    return rootView;
  }

  @Override
  public void onStart() {
    super.onStart();
    EasyTracker.getInstance(getActivity()).activityStart(getActivity());

  }

  @Override
  public void onStop() {
    super.onStop();
    EasyTracker.getInstance(getActivity()).activityStart(getActivity());

  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    try {
      mListener = (OnTaskSelectedListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement OnTaskSelectedListener");
    }
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

    // Inflate menu from XML resource
    MenuInflater inflater = getActivity().getMenuInflater();
    inflater.inflate(R.menu.task_list_context, menu);
//    super.onCreateContextMenu(menu, v, menuInfo);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    Task task = new Task();
    if (items.size() > 0) {
      task = (Task)items.get(info.position);
    }

    switch (item.getItemId()) {
      case R.id.context_edit:
        // Launch activity to view/edit the currently selected item
        mListener.onTaskSelected(task.getId());
        return true;

      case R.id.context_delete:
        this.confirmDelete(task.getId()).show();

        // Returns to the caller and skips further processing.
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {

    if(items.size() > 0) {
      if (!items.get(position).isSection()) {
        mListener.onTaskSelected(((Task) items.get(position)).getId());
      }
    }
    getListView().setItemChecked(position, true);
  }

  /**
   * Update list view adapter
   */
  public void updateAdapter() {
    items = TaskRepository.getAllItems(getActivity());
    TaskListAdapter adapter = new TaskListAdapter(getActivity(), items);
    listView.setAdapter(adapter);
  }

  /*--Confirm dialog delete--*/
  private AlertDialog confirmDelete(final long taskId) {
    return CommonUtils.confirmDelete(getActivity(), new android.content.DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        // your deleting code
        TaskRepository.deleteTaskWithId(getActivity().getApplicationContext(), taskId);
        dialog.dismiss();
        getActivity().getSupportFragmentManager().popBackStack();
        updateAdapter();
        new ReminderManager().destroyAlarm(getActivity(), (int) taskId);
      }
    }, new android.content.DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
  }
}
