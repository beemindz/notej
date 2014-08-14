package com.beemindz.notej.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;

import com.beemindz.notej.R;

public class CommonUtils {

  /**
   * Return string date in specified format
   *
   * @param calendar   calendar
   * @param dateFormat Date format
   * @return string date time
   */
  public static String getStringDate(Calendar calendar, String dateFormat) {
    // Create a DateFormatter object for displaying date in specified format.
    DateFormat formatter = new SimpleDateFormat(dateFormat);
    return formatter.format(calendar.getTime());
  }

  public static String getStringDate(Date date, String dateFormat) {
    // Create a DateFormatter object for displaying date in specified format.
    DateFormat formatter = new SimpleDateFormat(dateFormat);
    return formatter.format(date);
  }

  public static String getStringDate(Long timeMillis, String dateFormat) {
    // Create a DateFormatter object for displaying date in specified format.
    Date date = new Date();
    date.setTime(timeMillis);
    DateFormat formatter = new SimpleDateFormat(dateFormat);
    return formatter.format(date);
  }

  public static Long getTimeMillis(String strDate, String dateFormat) {
    DateFormat formatter = new SimpleDateFormat(dateFormat);

    try {
      Date date = formatter.parse(strDate);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      return calendar.getTimeInMillis();
    } catch (ParseException e) {
      e.printStackTrace();
      return System.currentTimeMillis();
    }
  }

  /**
   * Return date in specified format
   *
   * @param strDate    date format string
   * @param dateFormat date format
   * @return Date
   */
  public static Date getDate(String strDate, String dateFormat) {
    // Create a DateFormatter object for displaying date in specified format.
    DateFormat formatter = new SimpleDateFormat(dateFormat);

    try {
      return formatter.parse(strDate);
    } catch (ParseException e) {
      e.printStackTrace();
      return new Date();
    }
  }

  /**
   * Get date format System setting.
   *
   * @param context context activity.
   * @return date format.
   */
  public static String getDateFormatSystem(Context context) {
    String format = Settings.System.getString(context.getContentResolver(), Settings.System.DATE_FORMAT);
    if (TextUtils.isEmpty(format)) {
      format = Constant.DATE_FORMAT;
    }
    return format;
  }

  public static String getDateTimeFormatSystem(Context context) {
    String format = Settings.System.getString(context.getContentResolver(), Settings.System.DATE_FORMAT);
    if (TextUtils.isEmpty(format)) {
      format = Constant.DATE_TIME_FORMAT;
    } else {
      format = format + " " + Constant.TIME_FORMAT;
    }
    return format;
  }


  /**
   * Confirm delete.
   */
  public static AlertDialog confirmDelete(Context context, OnClickListener yesListener, OnClickListener noListener) {
    return new AlertDialog.Builder(context)
        .setTitle(context.getResources().getString(R.string.menu_delete))
        .setMessage(context.getResources().getString(R.string.confirm_delete_task))
        .setPositiveButton(context.getResources().getString(android.R.string.ok), yesListener)
        .setNegativeButton(context.getResources().getString(android.R.string.cancel), noListener).create();
  }

  /**
   * Confirm.
   */
  public static AlertDialog confirm(Context context, int restTitleId, int restMsgId, OnClickListener yesListener, OnClickListener noListener) {
    return new AlertDialog.Builder(context)
        .setTitle(context.getResources().getString(restTitleId))
        .setMessage(context.getResources().getString(restMsgId))
        .setPositiveButton(context.getResources().getString(android.R.string.ok), yesListener)
        .setNegativeButton(context.getResources().getString(android.R.string.cancel), noListener).create();
  }

  public static Intent getOpenFacebookIntent(Context context) {
    try {
      context.getPackageManager()
          .getPackageInfo("com.facebook.katana", 0);
      return new Intent(Intent.ACTION_VIEW,
          Uri.parse(String.format("fb://profile/%s", Constant.FB_BEEMINDZ_FANPAGE_ID)));
    } catch (Exception e) {
      return new Intent(Intent.ACTION_VIEW,
          Uri.parse(String.format("https://www.facebook.com/%s", Constant.FB_BEEMINDZ_FANPAGE_NAME)));
    }
  }

  public static boolean isEmailValid(String email) {
    String regex = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(email);

    return matcher.matches();
  }

  public static String computeMD5Hash(String password) {
    try {
      // Create MD5 Hash
      MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
      digest.update(password.getBytes());
      byte messageDigest[] = digest.digest();

      StringBuffer MD5Hash = new StringBuffer();
      for (int i = 0; i < messageDigest.length; i++) {
        String h = Integer.toHexString(0xFF & messageDigest[i]);
        while (h.length() < 2)
          h = "0" + h;
        MD5Hash.append(h);
      }

      return MD5Hash.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
  }
}
