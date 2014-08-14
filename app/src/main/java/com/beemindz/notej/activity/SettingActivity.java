package com.beemindz.notej.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.beemindz.notej.R;
import com.beemindz.notej.activity.fragment.SettingsFragment;

public class SettingActivity extends ActionBarActivity {

  public static final String KEY_PREF_VIBRATOR = "pref_key_vibrator";
  public static final String KEY_PREF_SNOOZE_MINUTE = "pref_key_snooze_minute";
  public static final String KEY_PREF_CATEGORY_SYNC = "pref_key_category_sync";
  public static final String KEY_PREF_KEY_CHANGE_PASS = "pref_key_change_pass";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Display home button.
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
    String setting = getResources().getString(R.string.setting);
    getSupportActionBar().setTitle(setting);
    // Display the fragment as the main content.
    getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    switch (menuItem.getItemId()) {
    case android.R.id.home:
      finish();
      return true;
    }
    return (super.onOptionsItemSelected(menuItem));
  }
  
  @Override
  protected void onResume() {
    super.onResume();
  }
}
