package com.beemindz.notej.dao;

import android.content.Context;
import android.util.Log;

import com.beemindz.notej.MiyoteeApplication;
import com.beemindz.notej.R;
import com.beemindz.notej.activity.adapter.Item;
import com.beemindz.notej.activity.adapter.SectionItem;
import com.beemindz.notej.util.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sony on 7/31/2014.
 */
public class TaskRepository {
  public static long insertOrUpdate(Context context, Task task) {
    if (task != null) {
//      Long idTemp = task.getId();
      Long id = getTaskDao(context).insertOrReplace(task);
      TaskDraft draft = new TaskDraft();
      convertToTaskDraft(draft, task);
      if (draft.getTaskId() != null) {
        draft.setStatus(Constant.TASK_DRAFT_STATUS_UPDATE);
      } else {
        draft.setStatus(Constant.TASK_DRAFT_STATUS_INSERT);
      }
      Long idDraft = TaskDraftRepository.insertOrUpdate(context, draft);
      Log.d("Insert Task ", "Id : " + id);
      Log.d("Insert Task Draft", "Id : " + idDraft);

      return id;
    }
    return 0;
  }

  public static void clearTasks(Context context) {
    getTaskDao(context).deleteAll();
  }

  public static void deleteTaskWithId(Context context, long id) {
    Task task = getTaskForId(context, id);
    if (task != null) {
      TaskDraft taskDraft = new TaskDraft();
      convertToTaskDraft(taskDraft, task);
      getTaskDao(context).delete(task);
      taskDraft.setStatus(Constant.TASK_DRAFT_STATUS_DELETE);
      Long idTaskDraft = TaskDraftRepository.insertOrUpdate(context, taskDraft);
      Log.d("Insert status delete : ", idTaskDraft.toString());
    }
  }

  public static List<Task> getAllTasks(Context context) {
    return getTaskDao(context).loadAll();
  }

  public static Task getTaskForId(Context context, long id) {
      return getTaskDao(context).load(id);
    }

  private static TaskDao getTaskDao(Context c) {
    return ((MiyoteeApplication) c.getApplicationContext()).getDaoSession().getTaskDao();
  }

  private static TaskDraft convertToTaskDraft(TaskDraft taskDraft, Task task) {

    taskDraft.setId(task.getId());
    taskDraft.setTaskId(task.getTaskId());
    taskDraft.setTaskName(task.getTaskName());
    taskDraft.setTaskDescription(task.getTaskDescription());
    taskDraft.setDueDate(task.getDueDate());
    taskDraft.setReminderDate(task.getReminderDate());
//    taskDraft.setIsReminder(task.getIsReminder());
//    taskDraft.setIsDueDate(task.getIsDueDate());
    taskDraft.setIsComplete(task.getIsComplete());
    taskDraft.setCreatedDate(task.getCreatedDate());
    taskDraft.setUpdatedDate(task.getUpdatedDate());

    return taskDraft;
  }

  public static List<Item> getAllItems(Context context) {
    List taskCompleted = getTaskDao(context).queryBuilder()
        .where(TaskDao.Properties.IsComplete.eq(true))
        .orderDesc(TaskDao.Properties.UpdatedDate)
        .list();
    List taskNotCompleted = getTaskDao(context).queryBuilder()
        .where(TaskDao.Properties.IsComplete.eq(false))
        .orderDesc(TaskDao.Properties.CreatedDate)
        .list();

    List<Item> items = new ArrayList<Item>();

    items.addAll(taskNotCompleted);
    if (taskCompleted!=null && taskCompleted.size() > 0) {
      items.add(new SectionItem(context.getResources().getString(R.string.event_completed)));
      items.addAll(taskCompleted);
    }

    return items;
  }
}
