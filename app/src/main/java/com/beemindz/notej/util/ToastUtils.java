package com.beemindz.notej.util;

import android.content.Context;
import android.widget.Toast;

/**
 * This utility for toast message. Using to show message on screen activity.
 */
public class ToastUtils {
	
	/**
	 * Displays a Toast notification for a short duration.
	 * 
	 * @param context
	 *            activity screen.
	 * @param resId
	 *            '@string' id.
	 */
	public static void toast(Context context, int resId) {
		Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Displays a Toast notification for a short duration.
	 * 
	 * @param context
	 *            activity screen.
	 * @param message
	 *            message need show.
	 */
	public static void toast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}
