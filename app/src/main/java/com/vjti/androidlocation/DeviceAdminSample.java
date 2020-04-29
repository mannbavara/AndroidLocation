package com.vjti.androidlocation;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DeviceAdminSample extends DeviceAdminReceiver {
    void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, "Enabled");
    }
    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "Disable Request Warning!";
    }
    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, "Disabled");
    }
}

