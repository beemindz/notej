package com.beemindz.notej.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.beemindz.notej.util.JSONParser;
import com.beemindz.notej.util.NetworkUtils;
import com.beemindz.notej.util.ToastUtils;
import com.facebook.FacebookException;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.analytics.tracking.android.EasyTracker;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AccountAuthenticatorActivity {

  public static final String JSON_TAG_ERROR = "error";
  // Sync interval constants
  public static final long SECONDS_PER_MINUTE = 60L;
  public static final long SYNC_INTERVAL_IN_MINUTES = 5L;
  public static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;
  private static final String TAG = "LOGIN";
  // PROPERTY FORGOT PASSWORD
  View.OnClickListener sendListener = new View.OnClickListener() {

    @Override
    public void onClick(View v) {
      try {
        if (edUserName != null && !TextUtils.isEmpty(edUserName.getText().toString())) {
          if (!NetworkUtils.isOnline(MainActivity.this)) {
            ToastUtils.toast(MainActivity.this, R.string.toast_msg_network_not_connect);
            return;
          }

          if (CommonUtils.isEmailValid(edUserName.getText().toString().trim())) {
            ForgotPassword forgotPassword = new ForgotPassword(MainActivity.this, edUserName.getText().toString());
            ResponseServer response = new ResponseServer();
            forgotPassword.execute();
            response = forgotPassword.get();
            if (response != null) {
              Log.d(TAG, "edUserName = " + edUserName.getText().toString());
              Log.d(TAG, response.getMessage() + "," + response.getSuccess());
              ToastUtils.toast(MainActivity.this, response.getMessage());
              if (response.getSuccess() == 0) {
                alertDialog.dismiss();
              }
            }
          } else {
            ToastUtils.toast(MainActivity.this, R.string.toast_err_email_invalid);
          }
        } else {
          Log.d(TAG, "edUserName is null");
          ToastUtils.toast(MainActivity.this, R.string.toast_err_forgot_pass_email_required);
        }
      } catch (Exception e) {
        e.printStackTrace();
        Log.e(TAG, e + "");
        ToastUtils.toast(MainActivity.this, R.string.toast_err_system);
      }
    }
  };
  EditText loginName, loginPassword, edUserName;
  TextView loginError;
  Button btnLogin, btnForgotPass;
  LoginButton btnFbLogin;
  String fbAccessToken = "";
  View.OnClickListener cancelListener = new View.OnClickListener() {

    @Override
    public void onClick(View v) {
      alertDialog.dismiss();
    }
  };
  // alert forgot password.
  private AlertDialog alertDialog = null;
  private JSONParser jsonParser = new JSONParser();

  // A content resolver for accessing the provider
  private ProgressDialog progressDialog;
  // Progress Dialog
  private ProgressDialog pDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
    // Check exist account
    AccountManager accountManager = AccountManager.get(this);
    Account[] accounts = accountManager.getAccountsByType(Constant.ACCOUNT_TYPE);

    if (accounts.length > 0) {
      // Chuyen den trang TaskList
      Intent i = new Intent(MainActivity.this, TaskListActivity.class);
      startActivity(i);
      finish();
    }

    loginName = (EditText) findViewById(R.id.loginName);
    loginPassword = (EditText) findViewById(R.id.loginPassword);
    loginError = (TextView) findViewById(R.id.loginError);

    // Đăng nhập
    btnForgotPass = (Button) findViewById(R.id.btnLinkToForgotPassDialog);
    btnFbLogin = (LoginButton) findViewById(R.id.btnFbLogin);
    btnLogin = (Button) findViewById(R.id.btnLogin);
    fbLogin();
    btnLogin.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        String userName = loginName.getText().toString().trim();
        String pass = loginPassword.getText().toString().trim();
        if (TextUtils.isEmpty(userName)) {
          ToastUtils.toast(MainActivity.this, R.string.toast_err_login_username_required);
          return;
        }
        if (TextUtils.isEmpty(pass)) {
          ToastUtils.toast(MainActivity.this, R.string.toast_err_login_pass_required);
          return;
        }
        if (!CommonUtils.isEmailValid(userName)) {
          ToastUtils.toast(MainActivity.this, R.string.toast_err_email_invalid);
          return;
        }

        if (!NetworkUtils.isOnline(MainActivity.this)) {
          ToastUtils.toast(MainActivity.this, R.string.toast_msg_network_not_connect);
          return;
        }
        if (!TextUtils.isEmpty(pass)) pass = CommonUtils.computeMD5Hash(pass.trim());
        Login login = new Login(MainActivity.this, null, userName, pass, "");
        login.execute();
      }
    });

    // Chuyển sang trang đăng ký
    Button btnLinkToRegisterScreen = (Button) findViewById(R.id.btnLinkToRegisterScreen);
    btnLinkToRegisterScreen.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        // pDialog = ProgressDialog.show(MainActivity.this,
        // "Loading...", "Please wait...", false, true);
        Intent i = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(i);
        // finish();
      }

    });

    btnForgotPass.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View arg0) {

        AlertDialog.Builder dialogbuilder = customDialog(MainActivity.this, sendListener, cancelListener);

        alertDialog = dialogbuilder.create();
        alertDialog.show();
      }
    });
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
  protected void onStart() {
    super.onStart();

    EasyTracker.getInstance(this).activityStart(this);
    Log.i(TAG, "==onStart==");
  }

  @Override
  protected void onStop() {
    super.onStop();
    EasyTracker.getInstance(this).activityStop(this);
    Log.i(TAG, "==onStop==");
  }

  private void fbLogin() {
    btnFbLogin.setOnErrorListener(new LoginButton.OnErrorListener() {

      @Override
      public void onError(FacebookException error) {
        // TODO Auto-generated method stub
        Log.i(TAG, "Error : " + error.getMessage());
      }
    });

    // set permission list, Don't foeget to add email
    btnFbLogin.setReadPermissions(Arrays.asList("email"));
    // session state call back event
    btnFbLogin.setSessionStatusCallback(new Session.StatusCallback() {

      @SuppressWarnings("deprecation")
      @Override
      public void call(Session session, SessionState state, Exception exception) {
        // TODO Auto-generated method stub
        if (session.isOpened()) {
          fbAccessToken = session.getAccessToken();
          com.facebook.Request.executeMeRequestAsync(session, new com.facebook.Request.GraphUserCallback() {

            @Override
            public void onCompleted(GraphUser user, Response response) {
              // TODO Auto-generated method stub
              if (user != null) {
                Login login = new Login(MainActivity.this, user.getName(), user.asMap().get("email").toString(), "",
                    fbAccessToken);
                login.execute();

                Log.i(TAG, "User ID " + user.getId());
                Log.i(TAG, "Email " + user.asMap().get("email"));
                Log.i(TAG, "Username" + user.getUsername());
                Log.i(TAG, "Name" + user.getName());
                Log.i(TAG, "First name " + user.getFirstName());
                Log.i(TAG, "Last name " + user.getLastName());
              }
            }
          });
        }
      }
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
  }

  public AlertDialog.Builder customDialog(Context context, android.view.View.OnClickListener rightBtnListener,
                                          android.view.View.OnClickListener leftBtnListener) {
    LayoutInflater inflater = LayoutInflater.from(context);
    View dialogview = inflater.inflate(R.layout.dialog_forgot_password, null);

    AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(context);
    dialogbuilder.setView(dialogview);

    Button buttonR = (Button) dialogview.findViewById(R.id.dialog_forgot_pass_btn_send);
    Button buttonL = (Button) dialogview.findViewById(R.id.dialog_forgot_pass_btn_cancel);
    edUserName = (EditText) dialogview.findViewById(R.id.dialog_forgot_pass_ed_username_email);
    edUserName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

    buttonR.setOnClickListener(rightBtnListener);
    buttonL.setOnClickListener(leftBtnListener);

    return dialogbuilder;
  }

  /**
   * Background Async Task to Get complete user login
   */
  private class Login extends AsyncTask<String, Void, Boolean> {
    Context mContext;
    String username;
    String password;
    String accessToken;
    String fullName;

    public Login(Context context, String fullname, String username, String password, String accessToken) {
      mContext = context;
      this.fullName = fullname;
      this.username = username;
      this.password = password;
      this.accessToken = accessToken;

      btnLogin.setEnabled(false);
      pDialog = ProgressDialog.show(context, "Loading...", "Please wait...", false, true);
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

      try {
        // Building Parameters
        String[] keys = new String[]{"username", "password", "fbAccessToken"};
        String[] values = new String[]{username, password, accessToken};

        JSONObject json = NetworkUtils.postJSONObjFromUrl(Constant.URL_HOST + "login.php", keys, values);
        // json error tagc
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
          Log.i(TAG, json.getString("message"));
          // loginError.setText(json.getString("message"));
          return false;
        }
      } catch (JSONException e) {
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
      btnLogin.setEnabled(true);
      pDialog.dismiss();
      Log.i(TAG, "Access Token : " + accessToken);
      if (result) {
        // Intent i = new Intent(MainActivity.this, TaskListActivity.class);
        // startActivity(i);
        finish();
      } else {
        if (!TextUtils.isEmpty(this.accessToken)) {
          Log.d(TAG, "accessToken != null");
          Intent i = new Intent(MainActivity.this, SignUpActivity.class);
          i.putExtra(Constant.FULL_NAME, this.fullName);
          i.putExtra(Constant.USERNAME, this.username);
          i.putExtra(Constant.FB_ACCESSTOKEN, this.accessToken);
          startActivity(i);
          finish();
        } else {
          loginError.setText(getResources().getString(R.string.toast_err_email_or_pass_incorrect));
        }
      }
    }
  }

  public class ForgotPassword extends AsyncTask<Void, Void, ResponseServer> {

    String email = "";
    Context context;
    int success = 0;
    String message = "";

    public ForgotPassword(Context context, String from) {
      this.context = context;
      this.email = from;
      pDialog = ProgressDialog.show(context, "Loading...", "Please wait...", false, true);
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    @Override
    protected ResponseServer doInBackground(Void... params) {
      try {
        // Building Parameters
        List<NameValuePair> parList = new ArrayList<NameValuePair>();
        parList.add(new BasicNameValuePair("email", email));
        parList.add(new BasicNameValuePair("password", CommonUtils.computeMD5Hash(Constant.DEFAULT_PASSWORD)));

        // getting json object.

        Log.d(TAG, "doInBackground email:" + email);
        JSONObject jsonObject = jsonParser.makeHttpRequest(Constant.REST_URL_FORGOT_PASSWORD, "POST", parList);
        try {
          ResponseServer responseServer = new ResponseServer();
          responseServer.setSuccess(jsonObject.getInt("success"));
          responseServer.setMessage(jsonObject.getString("message"));

          Log.d(TAG, "succes:" + success);
          Log.d(TAG, "message:" + message);
          return responseServer;
        } catch (JSONException e) {
          e.printStackTrace();
          Log.e(TAG, " ForgotPassword=>doInBackground JSONException " + e);
        }
      } catch (Exception e) {
        Log.e(TAG, e + "ForgotPassword=>doInBackground Exception " + e);
      }
      return null;
    }


    @Override
    protected void onPostExecute(ResponseServer responseServer) {
      pDialog.dismiss();
    }
  }
  // END.

  public class ResponseServer {
    int success;
    String message;

    public int getSuccess() {
      return success;
    }

    public void setSuccess(int success) {
      this.success = success;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }
}

