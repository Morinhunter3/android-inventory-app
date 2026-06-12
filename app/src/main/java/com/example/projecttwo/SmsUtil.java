package com.example.projecttwo;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class SmsUtil {

    public static boolean isSmsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SmsNotificationActivity.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(SmsNotificationActivity.KEY_SMS_ENABLED, false);
    }

    public static String getSmsPhone(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SmsNotificationActivity.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(SmsNotificationActivity.KEY_SMS_PHONE, "");
    }

    public static boolean hasSmsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void sendLowStockAlert(Context context, String itemName, int qty) {
        if (!isSmsEnabled(context)) return;
        if (!hasSmsPermission(context)) return;

        String phone = getSmsPhone(context).trim();
        if (phone.isEmpty()) return;

        String message = "Low inventory alert: " + itemName + " has quantity " + qty + ".";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, message, null, null);
            Toast.makeText(context, "SMS alert sent.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {

            Toast.makeText(context, "SMS could not be sent on this device.", Toast.LENGTH_LONG).show();
        }
    }
}
