package com.dinhtc.taskmaster.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    private static boolean checkPermissionAll = false;

    public static int dp2px (int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    public static void checkPermission (Fragment fragment, String permissionString, int permissionCode) {
        if ((android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) || fragment.getContext() == null) return;
        int existingPermissionStatus = ContextCompat.checkSelfPermission(fragment.getContext(),
                permissionString);
        if (existingPermissionStatus == PackageManager.PERMISSION_GRANTED) return;
        fragment.requestPermissions(new String[]{permissionString}, permissionCode);
    }

    public static void checkAndRequestPermissions(Context fragment, Activity activityLocal, int permissionCode) {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (arePermissionsGranted(fragment, permissions)) {
            // Quyền đã được cấp, tiến hành xử lý tương ứng tại đây.
            return;
        } else {
            // Yêu cầu cấp quyền từ người dùng.
            ActivityCompat.requestPermissions(activityLocal, permissions, permissionCode);
        }
    }

    private static boolean arePermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void checkPermissions(Fragment fragment, String[] permissions, int permissionCode) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M || fragment.getContext() == null) return;

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(fragment.getContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (permissionsToRequest.isEmpty()) {
            // Tất cả các quyền đã được cấp, không cần yêu cầu thêm.
            return;
        }

        // Chuyển permissionsToRequest thành mảng String[] để yêu cầu quyền.
        String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
        fragment.requestPermissions(permissionsArray, permissionCode);
    }


    public static boolean isReadStorageGranted (Context context) {
        int storagePermissionGranted = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        return storagePermissionGranted == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean areBothPermissionsGranted(Context context) {
        int readStoragePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStoragePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readImagePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES);
        int readVideoPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO);
        int cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Đây là Android 11 hoặc cao hơn.
            return cameraPermission == PackageManager.PERMISSION_GRANTED
                    && readImagePermission == PackageManager.PERMISSION_GRANTED
                    && readVideoPermission == PackageManager.PERMISSION_GRANTED;
        } else {
            // Đây là phiên bản Android dưới Android 11.
            return writeStoragePermission == PackageManager.PERMISSION_GRANTED
                    && readStoragePermission == PackageManager.PERMISSION_GRANTED
                    && cameraPermission == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static boolean isWriteStorageGranted (Context context) {
        int storagePermissionGranted = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return storagePermissionGranted == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isCameraGranted (Context context) {
        int cameraPermissionGranted = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        return cameraPermissionGranted == PackageManager.PERMISSION_GRANTED;
    }

}