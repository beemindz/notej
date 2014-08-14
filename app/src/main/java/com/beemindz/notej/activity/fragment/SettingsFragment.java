package com.beemindz.notej.activity.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.beemindz.notej.R;
import com.beemindz.notej.activity.ChangePassActivity;
import com.beemindz.notej.activity.MainActivity;
import com.beemindz.notej.activity.SettingActivity;
import com.beemindz.notej.util.CommonUtils;
import com.beemindz.notej.util.Constant;

public class SettingsFragment extends android.support.v4.preference.PreferenceFragment {

  public static final String TAG = "SettingsFragment";
  private Preference pref, prefChangePass;
  private Account[] accounts;
  PreferenceScreen preferenceScreen;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.setting);
    
    accounts = AccountManager.get(getActivity().getApplicationContext()).getAccountsByType(Constant.ACCOUNT_TYPE);

    preferenceScreen = getPreferenceScreen();

    pref = findPreference(SettingActivity.KEY_PREF_CATEGORY_SYNC);
    prefChangePass = findPreference(SettingActivity.KEY_PREF_KEY_CHANGE_PASS);

    checkAccount();

    onChangePass();
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");
    checkOnline();
    checkAccount();
  }
  
  @Override
  public void onPause() {
    super.onPause();
    Log.d(TAG, "onPause");
    checkOnline();
    checkAccount();
  }

  private void checkOnline() {
    accounts = AccountManager.get(getActivity().getApplicationContext()).getAccountsByType(Constant.ACCOUNT_TYPE);
    Log.d(TAG, "checkOnline:====="+ accounts.length);
    if (accounts.length > 0) {
      pref.setSummary(getResources().getString(R.string.pref_category_sync_lable_you_online));
      preferenceScreen.addPreference(prefChangePass);
    } else {
      pref.setSummary(R.string.pref_category_sync_summary);
      preferenceScreen.removePreference(prefChangePass);
    }
    
    Log.i(TAG, pref.getSummary().toString());
  }

  private void checkAccount() {
    accounts = AccountManager.get(getActivity().getApplicationContext()).getAccountsByType(Constant.ACCOUNT_TYPE);
    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

      @Override
      public boolean onPreferenceClick(Preference preference) {
        Log.d(TAG, "checkAccount:====="+ accounts.length);
        if (accounts.length == 0) {
          Intent intent = new Intent(preference.getContext(), MainActivity.class);
          preference.getContext().startActivity(intent);
        } else {
          final Preference p = preference;
          // show dialog
          CommonUtils.confirm(preference.getContext(), R.string.setting_confirm_title_sign_out,
              R.string.setting_confirm_message_sign_out, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                  eventSignOut(p.getContext());
                  
                  dialog.cancel();
                }
              }, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                  dialog.cancel();
                }
              }).show();
        }
        return false;
      }
    });

  }

  private void onChangePass() {
    accounts = AccountManager.get(getActivity().getApplicationContext()).getAccountsByType(Constant.ACCOUNT_TYPE);
    prefChangePass.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        if (accounts.length > 0) {
          Intent intent = new Intent(preference.getContext(), ChangePassActivity.class);
          preference.getContext().startActivity(intent);
        }

        return false;
      }
    });
  }

  private void eventSignOut(Context context) {
    if (accounts.length > 0) {
      for (Account account : accounts) {
        Log.i(TAG, "remove account : " + account.name);
        AccountManager.get(context).removeAccount(account, null, null);
      }
      accounts = new Account[]{};
      pref.setSummary(R.string.pref_category_sync_summary);
      preferenceScreen.removePreference(prefChangePass);
    }
  }
}
