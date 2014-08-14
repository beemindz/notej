package com.beemindz.notej.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.beemindz.notej.R;
import com.beemindz.notej.dao.MiyoteeContentProvider;
import com.beemindz.notej.service.authentication.AccountAuthenticatorActivity;
import com.beemindz.notej.util.CommonUtils;
import com.beemindz.notej.util.Constant;
import com.beemindz.notej.util.NetworkUtils;
import com.beemindz.notej.util.ToastUtils;
import com.google.analytics.tracking.android.EasyTracker;

import org.json.JSONException;
import org.json.JSONObject;

public class SignUpActivity extends AccountAuthenticatorActivity {

  private static final String TAG = "SIGNUP";
  public static final String JSON_TAG_ERROR = "error";
  // Sync interval constants
  public static final long SECONDS_PER_MINUTE = 60L;
  public static final long SYNC_INTERVAL_IN_MINUTES = 1L;
  public static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;

  EditText signUpName, signUpPassword, signUpFullName;
  TextView signUpError;
  Button btnSignUp;
  String fbAccessToken;

  // Progress Dialog
  private ProgressDialog pDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);

    getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    signUpName = (EditText) findViewById(R.id.signUpName);
    signUpPassword = (EditText) findViewById(R.id.signUpPassword);
    signUpFullName = (EditText) findViewById(R.id.signUpFullName);
    signUpError = (TextView) findViewById(R.id.signUpError);

    if (!TextUtils.isEmpty(getIntent().getStringExtra(Constant.FULL_NAME))
        && !TextUtils.isEmpty(getIntent().getStringExtra(Constant.USERNAME))
        && !TextUtils.isEmpty(getIntent().getStringExtra(Constant.FB_ACCESSTOKEN))) {
      fbAccessToken = getIntent().getStringExtra(Constant.FB_ACCESSTOKEN);
      signUpName.setText(getIntent().getStringExtra(Constant.USERNAME));
      signUpFullName.setText(getIntent().getStringExtra(Constant.FULL_NAME));
      signUpPassword.requestFocus();
    }
    // Đăng ký
    btnSignUp = (Button) findViewById(R.id.btnRegister);
    btnSignUp.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        String userName = signUpName.getText().toString().trim();
        String pass = signUpPassword.getText().toString().trim();
        if (TextUtils.isEmpty(userName)) {
          ToastUtils.toast(SignUpActivity.this, R.string.toast_err_login_username_required);
          return;
        }
        if (TextUtils.isEmpty(pass)) {
          ToastUtils.toast(SignUpActivity.this, R.string.toast_err_login_pass_required);
          return;
        }
        if (!CommonUtils.isEmailValid(userName)) {
          ToastUtils.toast(SignUpActivity.this, R.string.toast_err_email_invalid);
          return;
        }
        if (!NetworkUtils.isOnline(SignUpActivity.this)) {
          ToastUtils.toast(SignUpActivity.this, R.string.toast_msg_network_not_connect);
          return;
        }
        SignUp signUp = new SignUp(SignUpActivity.this);
        signUp.execute();
      }
    });

    // Chuyển sang trang đăng ký
    Button btnLinkToRegisterScreen = (Button) findViewById(R.id.btnLinkToLoginScreen);
    btnLinkToRegisterScreen.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        // pDialog = ProgressDialog.show(SignUpActivity.this, "Loading...", "Please wait...", false, true);
        Intent i = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(i);
        // finish();
      }

    });

  }

  @Override
  protected void onStart() {
    // TODO Auto-generated method stub
    super.onStart();
    EasyTracker.getInstance(this).activityStart(this);
    Log.i(TAG, "==onStart==");
  }

  @Override
  protected void onStop() {
    // TODO Auto-generated method stub
    super.onStop();
    EasyTracker.getInstance(this).activityStop(this);
    Log.i(TAG, "==onStop==");
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

  /**
   * Background Async Task to Get complete user details
   */
  private class SignUp extends AsyncTask<String, Void, Boolean> {
    Context mContext;

    public SignUp(Context context) {
      mContext = context;
      btnSignUp.setEnabled(false);

      pDialog = ProgressDialog.show(context, "Loading...", "Please wait...", false, true);
    }

    /**
     * Getting user details in background thread
     */
    @Override
    protected Boolean doInBackground(String... params) {

      // Check for error tag
      boolean error;

      String username = signUpName.getText().toString();
      String password = signUpPassword.getText().toString();
      String fullName = signUpFullName.getText().toString();

      // md5 password
      if (!TextUtils.isEmpty(password)) password = CommonUtils.computeMD5Hash(password.trim());
      try {
        // Building Parameters
        String[] keys = new String[]{"username", "password", "name", "fbAccessToken"};

        String[] values = new String[]{username, password, fullName, fbAccessToken};

        Log.i(TAG, "fbAccessToken : " + fbAccessToken);
        // getting product details by making HTTP request
        // Note that product details url will use GET request
        JSONObject json = NetworkUtils.postJSONObjFromUrl(Constant.URL_HOST + "sign-up.php", keys, values);

        // check your log for json response
        Log.d("Json sign-up response : ", json.toString());

        // json success tag
        error = json.getBoolean(JSON_TAG_ERROR);
        if (!error) {

          Bundle result = null;
          Account account = new Account(username, Constant.ACCOUNT_TYPE);
          AccountManager acountManager = AccountManager.get(mContext);

          if (acountManager.addAccountExplicitly(account, password, null)) {
            result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            setAccountAuthenticatorResult(result);

            /*
             * Turn on periodic syncing
             */
            ContentResolver.setSyncAutomatically(account, MiyoteeContentProvider.AUTHORITY, true);

            ContentResolver.addPeriodicSync(account, MiyoteeContentProvider.AUTHORITY, new Bundle(), SYNC_INTERVAL);
            return true;
          } else {
            return false;
          }
        } else {
          // signUpError.setText(json.getString("message"));
          return false;
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }

      return null;
    }

    /**
     * After completing background task Dismiss the progress dialog
     */
    @Override
    protected void onPostExecute(Boolean result) {
      // dismiss the dialog once got all details
      btnSignUp.setEnabled(true);
      pDialog.dismiss();
      if (result) {
        Intent i = new Intent(SignUpActivity.this, TaskListActivity.class);
        startActivity(i);
        finish();
      }
    }
  }
}
