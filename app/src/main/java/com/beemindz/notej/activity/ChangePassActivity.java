package com.beemindz.notej.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.beemindz.notej.R;
import com.beemindz.notej.util.CommonUtils;
import com.beemindz.notej.util.Constant;
import com.beemindz.notej.util.NetworkUtils;
import com.beemindz.notej.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ChangePassActivity extends ActionBarActivity {

  public static final String TAG = "ChangePassActivity";
  public static final String JSON_TAG_ERROR = "error";
  public static final String JSON_TAG_MESSAGE = "message";
  // Progress Dialog
  private ProgressDialog pDialog;
  private Account[] accounts;
  String email = "";

  TextView tvError, tvEmail;
  EditText etPassword, etRePassword, etOldPassword;
  Button btnSend;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_change_password);
    getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    accounts = AccountManager.get(this).getAccountsByType(Constant.ACCOUNT_TYPE);
    if (accounts.length > 0) {
      email = accounts[0].name;
    }

    tvEmail = (TextView) findViewById(R.id.change_pass_tv_email);
    tvError = (TextView) findViewById(R.id.change_pass_tv_error);
    etPassword = (EditText) findViewById(R.id.change_pass_ed_pass);
    etRePassword = (EditText) findViewById(R.id.change_pass_re_pass);
    etOldPassword = (EditText) findViewById(R.id.change_pass_ed_old_pass);
    btnSend = (Button) findViewById(R.id.change_pass_btnSend);

    if (!TextUtils.isEmpty(email)) tvEmail.setText(email);
    onButtonSendListner();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    //int id = item.getItemId();
    //if (id == R.id.action_settings) {
    //    return true;
    //}
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }


  private void onButtonSendListner() {
    btnSend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          String pass = etPassword.getText().toString().trim();
          String re_pass = etRePassword.getText().toString().trim();
          String oldPass = etOldPassword.getText().toString().trim();
          if (TextUtils.isEmpty(email)) {
            ToastUtils.toast(ChangePassActivity.this, R.string.toast_err_login_username_required);
            return;
          }
          if (TextUtils.isEmpty(oldPass)) {
            ToastUtils.toast(ChangePassActivity.this, R.string.toast_err_login_pass_required);
            return;
          }
          if (TextUtils.isEmpty(pass)) {
            ToastUtils.toast(ChangePassActivity.this, R.string.toast_err_login_pass_required);
            return;
          }
          if (TextUtils.isEmpty(re_pass) || !re_pass.trim().equals(pass.trim())) {
            ToastUtils.toast(ChangePassActivity.this, R.string.toast_err_login_re_pass_same_pass);
            return;
          }
          if (!NetworkUtils.isOnline(ChangePassActivity.this)) {
            ToastUtils.toast(ChangePassActivity.this, R.string.toast_msg_network_not_connect);
            return;
          }

          if (!TextUtils.isEmpty(oldPass)) oldPass = CommonUtils.computeMD5Hash(oldPass.trim());
          if (!TextUtils.isEmpty(pass)) pass = CommonUtils.computeMD5Hash(pass.trim());
          ChangePassword changePassword = new ChangePassword(ChangePassActivity.this, email, oldPass, pass);
          changePassword.execute();


          boolean result = changePassword.get();
          if (result) {
            ToastUtils.toast(ChangePassActivity.this, R.string.toast_msg_change_password_success);
            etOldPassword.setText("");
            etPassword.setText("");
            etRePassword.setText("");
          } else {
            ToastUtils.toast(ChangePassActivity.this, R.string.toast_msg_change_password_failed);
          }
        } catch (Exception e) {
          Log.e("ChangeActivity", "" + e);
        }
      }
    });
  }

  /**
   * Background Async Task to Get complete user login
   */
  private class ChangePassword extends AsyncTask<String, Void, Boolean> {
    Context mContext;
    String email;
    String old_password;
    String re_password;
    String message;

    public ChangePassword(Context context,  String email, String oldPass, String re_password) {
      mContext = context;
      this.email = email;
      this.old_password = oldPass;
      this.re_password = re_password;
      Log.d("ChangePassword", "oldPass=" + oldPass + ", new pass=" + re_password);
      btnSend.setEnabled(false);
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      pDialog = ProgressDialog.show(mContext, "Loading...", "Please wait...", false, true);
    }

    /**
     * Getting user details in background thread
     */
    @Override
    protected Boolean doInBackground(String... params) {

      // Check for success tag
      boolean error;
      /*
       * String deviceId = Secure.getString(getActivity().getContentResolver(),
       * Secure.ANDROID_ID); Log.d("android_id:", deviceId);
       */
      JSONObject json = null;
      try {
        // Building Parameters
        String[] keys = new String[]{"email", "old_pass", "new_pass"};
        String[] values = new String[]{email, old_password, re_password};

        json = NetworkUtils.postJSONObjFromUrl(Constant.URL_HOST + "change-pass.php", keys, values);
        Log.d("ChangPassword", json + "");
        if (json != null) {
          // json error tag
          error = json.getBoolean(JSON_TAG_ERROR);
          if (!error) {
              return true;
          } else {
            Log.i(TAG, json.getString(JSON_TAG_MESSAGE));
            message = json.getString(JSON_TAG_MESSAGE);
            return false;
          }
        }

        return false;
      } catch (JSONException e) {
        Log.e("ChangePassword","=doInBackground=" + e);
        message = e.getMessage();
        e.printStackTrace();
        return false;
      } catch (Exception e) {
        Log.e("ChangePassword", "Exception doInBackground" + e);
        message = e.getMessage();
        e.printStackTrace();
        return false;
      }
    }

    /**
     * After completing background task Dismiss the progress dialog
     */
    @Override
    protected void onPostExecute(Boolean result) {
      // dismiss the dialog once got all details
      btnSend.setEnabled(true);
      pDialog.dismiss();
      if (!result && !TextUtils.isEmpty(message)) {
          tvError.setText(message);
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (pDialog != null) {
      pDialog.dismiss();
      pDialog = null;
    }
  }
}
