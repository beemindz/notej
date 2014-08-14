package com.beemindz.notej.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.beemindz.notej.R;

public class HomeActivity extends Activity {

  private int DELAY_MILLIS = 3000;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d("HomeActivity", "HomeActivity");
//    getSupportActionBar().hide();
    setContentView(R.layout.activity_home);
    new Handler().postDelayed(new Runnable() {

      @Override
      public void run() {
        final Intent intent = new Intent(HomeActivity.this, TaskListActivity.class);
        startActivity(intent);
        finish();
      }
    }, DELAY_MILLIS);
  }
}
