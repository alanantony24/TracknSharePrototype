package com.example.prototype;

import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentActivity;

public class PermissionHelper {
    public void startPermissionRequest(FragmentActivity fragmentActivity, PermissionInterface permissionInterface, String manifest){
        ActivityResultLauncher<String> requestPermissionLauncher =
                fragmentActivity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), permissionInterface::onGranted);
        requestPermissionLauncher.launch(
                manifest);
    }
}
