package com.ulvijabbarli.imagetaker.permission;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>AppPermissionsRunTime</h1>
 * <p>
 * Class to handle app runtime permissions
 * </p>
 *
 * @since 24/5/16.
 */
public class AppPermissionsRunTime {

    private List<String> permissionsNeeded = null;
    private List<String> permissionsList = null;
    private AlertDialog dialog_parent = null;

    public enum Permission {
        LOCATION, CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, PHONE, RECORD_AUDIO, READ_CONTACT, READ_SMS, RECEIVE_SMS
    }

    /**
     * Singleton Class Object.
     */
    private static AppPermissionsRunTime app_permission = new AppPermissionsRunTime();

    /**
     * Private constructor to make this class as Singleton.
     */
    private AppPermissionsRunTime() {
    }

    /**
     * <h2>getInstance</h2>
     * <p>
     * Method need to access the Object of this single tone class
     * </p>
     */
    public static AppPermissionsRunTime getInstance() {
        return app_permission;
    }

    /**
     * @param permission_list: list of required permissions
     * @param activity:        calling activity reference
     * @return true if requested permissions are already granted
     */
    public boolean getPermission(final ArrayList<Permission> permission_list, Activity activity) {
        /*
         * Creating the List if not created .
         * if created then clear the list for refresh use.*/
        if (permissionsNeeded == null || permissionsList == null) {
            permissionsNeeded = new ArrayList<>();
            permissionsList = new ArrayList<>();
        } else {
            permissionsNeeded.clear();
            permissionsList.clear();
        }

        if (dialog_parent != null && dialog_parent.isShowing()) {
            dialog_parent.dismiss();
            dialog_parent.cancel();
        }
        for (int count = 0; permission_list != null && count < permission_list.size(); count++) {
            switch (permission_list.get(count)) {
                case LOCATION:
                    if (addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION, activity)) {
                        permissionsNeeded.add("GPS Fine Location");
                    }
                    if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION, activity)) {
                        permissionsNeeded.add("GPS Course Location");
                    }
                    break;
                case RECORD_AUDIO:
                    if (addPermission(permissionsList, Manifest.permission.RECORD_AUDIO, activity)) {
                        permissionsNeeded.add("Record audio");
                    }
                    break;
                case CAMERA:
                    if (addPermission(permissionsList, Manifest.permission.CAMERA, activity)) {
                        permissionsNeeded.add("Camera");
                    }
                    break;
                case READ_EXTERNAL_STORAGE:
                    if (addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE, activity)) {
                        permissionsNeeded.add("Write to external Storage");
                    }
                    break;
                case WRITE_EXTERNAL_STORAGE:
                    if (addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE, activity)) {
                        permissionsNeeded.add("Read to external Storage");
                    }
                    break;
                case PHONE:
                    if (addPermission(permissionsList, Manifest.permission.READ_PHONE_STATE, activity)) {
                        permissionsNeeded.add("Read Phone State");
                    }
                    break;
                case READ_CONTACT:
                    if (addPermission(permissionsList, Manifest.permission.READ_CONTACTS, activity)) {
                        permissionsNeeded.add("Read Contact State");
                    }
                case READ_SMS:
                    if (addPermission(permissionsList, Manifest.permission.READ_SMS, activity)) {
                        permissionsNeeded.add("Read SMS State");
                    }
                case RECEIVE_SMS:
                    if (addPermission(permissionsList, Manifest.permission.RECEIVE_SMS, activity)) {
                        permissionsNeeded.add("Receive SMS State");
                    }
                    break;
                default:
                    break;
            }

        }
        if (permissionsList.size() > 0 && permissionsNeeded.size() > 0) {
            StringBuilder message = new StringBuilder("You need to grant access to " + permissionsNeeded.get(0));
            for (int i = 1; i < permissionsNeeded.size(); i++) {
                message.append(", ").append(permissionsNeeded.get(i));
            }

            check_for_Permission(permissionsList.toArray(new String[0]), activity);


            return false;
        } else {
            return true;
        }
    }

    /**
     * @param permissionsList: array of requested permissions
     * @param permission:      array of requested permissions
     * @param activity:        calling activity reference
     */
    private boolean addPermission(List<String> permissionsList, String permission, Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            return true;
        } else {
            return false;
        }
    }

    /**
     * <h2>check_for_Permission</h2>
     * <p>
     * method to check whether requested permission has granted or not
     * </p>
     *
     * @param permissions: array of permissions to be requested
     * @param mactivity:*  calling activity reference
     */
    private void check_for_Permission(String[] permissions, Activity mactivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int REQUEST_CODE_PERMISSIONS = 1000;
            mactivity.requestPermissions(permissions, REQUEST_CODE_PERMISSIONS);
        }
    }

}
