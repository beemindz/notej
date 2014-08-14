package com.beemindz.notej.activity.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beemindz.notej.R;
import com.beemindz.notej.dao.Task;
import com.beemindz.notej.util.CommonUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskListAdapter extends ArrayAdapter<Item> {

  //private static final String TAG = "TaskListAdapter";
  LayoutInflater inflater;
  private List<Item> tasks;
  private Context context;
  private ArrayList<Integer> colors;

  public TaskListAdapter(Context context, List<Item> tasks) {
    super(context, 0, tasks);
    this.context = context;
    this.tasks = tasks;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    addBackgroundColor();
  }

  /**
   * Backgroup color item list.
   */
  private void addBackgroundColor() {
    colors = new ArrayList<Integer>();
    colors.add(R.color.GREEN_07ad4c);
    colors.add(R.color.GREEN_55b847);
    colors.add(R.color.GREEN_81c341);
    colors.add(R.color.GREEN_a5ce39);
    colors.add(R.color.GREEN_81c341);
    colors.add(R.color.GREEN_55b847);
    colors.add(R.color.GREEN_07ad4c);
    colors.add(R.color.GREEN_00a65c);
    colors.add(R.color.GREEN_00a76d);
    colors.add(R.color.GREEN_00a99d);
    colors.add(R.color.BLUE_00abc0);
    colors.add(R.color.BLUE_00adef);
    colors.add(R.color.BLUE_0094d9);
    colors.add(R.color.BLUE_0072bc);
    colors.add(R.color.BLUE_005eac);
    colors.add(R.color.BLUE_014fa3);
    colors.add(R.color.BLUE_223f99);
    colors.add(R.color.BLUE_014fa3);

    // complete
    colors.add(R.color.BLUE_005eac);
    colors.add(R.color.BLUE_0072bc);
    colors.add(R.color.BLUE_0094d9);
    colors.add(R.color.BLUE_00adef);
    colors.add(R.color.BLUE_00abc0);
    colors.add(R.color.GREEN_00a99d);
    colors.add(R.color.GREEN_00a76d);
    colors.add(R.color.GREEN_00a65c);
    colors.add(R.color.GREEN_07ad4c);
    colors.add(R.color.GREEN_55b847);
  }

  @SuppressLint("DefaultLocale")
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // color background.
//    int sizeColor = colors.size();

    // get data item task for this position.
    final Item item = tasks.get(position);
    if (item != null) {
      if (!item.isSection()) {
//        int colorPos = position % sizeColor;
        Task task = (Task) item;
        // Check if an existing view is being reused, otherwise inflate the

        // View lookup cache stored in tag
        ViewHolder viewHolder;
        //if (convertView == null) {
        viewHolder = new ViewHolder();
        //LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.task_list_item, parent, false);
        viewHolder.tvTaskName = (TextView) convertView.findViewById(R.id.tvTaskName);
        viewHolder.tvDueDate = (TextView) convertView.findViewById(R.id.tv_due_date);
        viewHolder.imgReminder = (ImageView) convertView.findViewById(R.id.img_reminder_clock);
        convertView.setTag(viewHolder);
        //} else {
        //  viewHolder = (ViewHolder) convertView.getTag();
        //}

        int width = parent.getWidth();

        Log.d("===width list view====", "" + width + "; length:" + task.getTaskName().trim().length());

        if (task.getDueDate() != null && task.getIsDueDate()) {
          Calendar dueDate = Calendar.getInstance();
          dueDate.setTime(task.getDueDate());

          String dayOfWeekString = getDayOfWeek(dueDate);
          viewHolder.tvDueDate.setText(dayOfWeekString);
        }
        //viewHolder.tvTaskName.setIncludeFontPadding(false);
        if (task.getTaskName() != null) {
          viewHolder.tvTaskName.setText(task.getTaskName().trim().toUpperCase());
        }

        if (task.getReminderDate() != null && task.getIsReminder() && task.getReminderDate().getTime() > System.currentTimeMillis()) {
          viewHolder.imgReminder.setVisibility(View.VISIBLE);
        } else {
          viewHolder.imgReminder.setVisibility(View.GONE);
        }

        // set color background item.
//        convertView.setBackgroundResource(colors.get(colorPos));
        //float alpha = 1;
        if (task.getIsComplete()) {
          //alpha = 0.45f;
          viewHolder.tvTaskName.setPaintFlags(viewHolder.tvTaskName.getPaintFlags() | (Paint.STRIKE_THRU_TEXT_FLAG));
          //viewHolder.tvTaskName.set
        } else if ((viewHolder.tvTaskName.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
          viewHolder.tvTaskName.setPaintFlags(viewHolder.tvTaskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        //AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
        //alphaUp.setFillAfter(true);
        //viewHolder.tvTaskName.startAnimation(alphaUp);
      } else {
        SectionItem sectionItem = (SectionItem) item;
        //inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.task_item_section, parent, false);
        final TextView sectionView = (TextView) convertView.findViewById(R.id.list_item_section_text);
        sectionView.setText(sectionItem.getTitle());
        sectionView.setTextColor(context.getResources().getColor(android.R.color.white));
      }
    }

    return convertView;
  }

  private String getDayOfWeek(Calendar dueDate) {
    Calendar today = Calendar.getInstance();
    String todayStr = (String) DateFormat.format(CommonUtils.getDateFormatSystem(context), today);
    String dueDateStr = (String) DateFormat.format(CommonUtils.getDateFormatSystem(context), dueDate);
    int dayOfWeek = 0;
    int day = dueDate.get(Calendar.DAY_OF_WEEK);
    if (dueDateStr.equals(todayStr)) {
      dayOfWeek = R.string.today;
    } else if (dueDate.getTimeInMillis() > System.currentTimeMillis() &&
        dueDate.get(Calendar.DAY_OF_MONTH) - today.get(Calendar.DAY_OF_MONTH) == 1 &&
        dueDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
        dueDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
      dayOfWeek = R.string.tomorrow;
    } else {
      switch (day) {
        case Calendar.MONDAY:
          dayOfWeek = R.string.monday;
          break;
        case Calendar.TUESDAY:
          dayOfWeek = R.string.tuesday;
          break;
        case Calendar.WEDNESDAY:
          dayOfWeek = R.string.wednesday;
          break;
        case Calendar.THURSDAY:
          dayOfWeek = R.string.thursday;
          break;
        case Calendar.FRIDAY:
          dayOfWeek = R.string.friday;
          break;
        case Calendar.SATURDAY:
          dayOfWeek = R.string.saturday;
          break;
        case Calendar.SUNDAY:
          dayOfWeek = R.string.sunday;
          break;
      }
    }

    return String.format("%s, %s", context.getResources().getString(dayOfWeek), dueDateStr);
  }

  /**
   * View lookup cache
   */
  private static class ViewHolder {
    TextView tvTaskName;
    TextView tvDueDate;
    ImageView imgReminder;
  }
}
