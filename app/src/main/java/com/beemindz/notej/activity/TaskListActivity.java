package com.beemindz.notej.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.beemindz.notej.R;
import com.beemindz.notej.activity.fragment.AboutFragment;
import com.beemindz.notej.activity.fragment.NavigationDrawerFragment;
import com.beemindz.notej.activity.fragment.TaskEditorFragment;
import com.beemindz.notej.activity.fragment.TaskListFragment;
import com.beemindz.notej.util.CommonUtils;

public class TaskListActivity extends ActionBarActivity
    implements NavigationDrawerFragment.NavigationDrawerCallbacks, TaskListFragment.OnTaskSelectedListener {

  private final String TAG = "LIST";
  /**
   * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
   */
  private NavigationDrawerFragment mNavigationDrawerFragment;
  /**
   * Used to store the last screen title. For use in {@link #restoreActionBar()}.
   */
  private CharSequence mTitle;

  public TaskListActivity() {
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_task_list);

    mNavigationDrawerFragment = (NavigationDrawerFragment)
        getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    mTitle = getTitle();

    // Set up the drawer.
    mNavigationDrawerFragment.setUp(
        R.id.navigation_drawer,
        (DrawerLayout) findViewById(R.id.drawer_layout));

  }

  @Override
  public void onNavigationDrawerItemSelected(int position) {
    Log.d(TAG, "onNavigationDrawerItemSelected");
    // update the main content by replacing fragments
    FragmentManager fragmentManager = getSupportFragmentManager();

    fragmentManager.beginTransaction()
        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
        .commit();
  }

  public void onSectionAttached(int number) {
    Log.d(TAG, "onSectionAttached " + number);
    switch (number) {
      case 1:
        mTitle = getString(R.string.title_activity_list);
        break;
      case 2:
        Intent intentSetting = new Intent(TaskListActivity.this, SettingActivity.class);
        startActivity(intentSetting);
        break;
      case 3:
        mTitle = getString(R.string.title_about);
        break;
      case 4:
        Intent facebookIntent = CommonUtils.getOpenFacebookIntent(this);
        startActivity(facebookIntent);
        break;

      default:
        mTitle = getString(R.string.title_activity_list);
        break;
    }
  }

  public void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setDisplayShowCustomEnabled(false);

    actionBar.setTitle(mTitle);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (!mNavigationDrawerFragment.isDrawerOpen()) {
      // Only show items in the action bar relevant to this screen
      // if the drawer is not showing. Otherwise, let the drawer
      // decide what to show in the action bar.
      getMenuInflater().inflate(R.menu.task_list, menu);
      Log.d(TAG, mTitle.toString());
      if (mTitle.equals(getString(R.string.title_activity_list))) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new TaskListFragment()).commit();
      }

      if (mTitle.equals(getString(R.string.title_about))) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
            .replace(R.id.container, new AboutFragment())
            .commit();
        restoreActionBar();
      }
      return true;
    } else {
      restoreActionBar();
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest  .xml.
    int id = item.getItemId();
    /*if (id == R.id.action_settings) {
      return true;
    }*/
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onTaskSelected(long taskId) {
    Log.d(TAG, "TASK ID : " + taskId);
    // update the main content by replacing fragments
    FragmentManager fragmentManager = getSupportFragmentManager();
    android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    fragmentTransaction.replace(R.id.container, TaskEditorFragment.newInstance(taskId));
    fragmentTransaction.addToBackStack("TaskEditorFragment");
    fragmentTransaction.commit();
  }

  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public PlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
      Log.d("PlaceholderFragment", "newInstance " + sectionNumber);

      PlaceholderFragment fragment = new PlaceholderFragment();
      Bundle args = new Bundle();
      args.putInt(ARG_SECTION_NUMBER, sectionNumber);
      fragment.setArguments(args);
      return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_task_list, container, false);

      return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
      Log.d("PlaceholderFragment", "OnAttach");
      super.onAttach(activity);
      ((TaskListActivity) activity).onSectionAttached(
          getArguments().getInt(ARG_SECTION_NUMBER));
    }
  }
}
