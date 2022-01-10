/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.managedprovisioning.common;

import static java.util.Objects.requireNonNull;

import android.annotation.Nullable;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.android.managedprovisioning.provisioning.Constants;

/**
 * Helper class for logic related to device management role holder updater launching.
 */
public class DeviceManagementRoleHolderUpdaterHelper {

    private final String mRoleHolderUpdaterPackageName;
    private final String mRoleHolderPackageName;
    private final PackageInstallChecker mPackageInstallChecker;

    public DeviceManagementRoleHolderUpdaterHelper(
            @Nullable String roleHolderUpdaterPackageName,
            @Nullable String roleHolderPackageName,
            PackageInstallChecker packageInstallChecker) {
        mRoleHolderUpdaterPackageName = roleHolderUpdaterPackageName;
        mRoleHolderPackageName = roleHolderPackageName;
        mPackageInstallChecker = requireNonNull(packageInstallChecker);
    }

    /**
     * Returns whether the device management role holder updater should be started.
     */
    public boolean shouldStartRoleHolderUpdater(Context context) {
        if (!Constants.FLAG_DEFER_PROVISIONING_TO_ROLE_HOLDER) {
            ProvisionLogger.logi("Not starting role holder updater, because the feature flag "
                    + "is turned off.");
            return false;
        }
        if (TextUtils.isEmpty(mRoleHolderPackageName)) {
            ProvisionLogger.logi("Not starting role holder updater, because the role holder "
                    + "package name is null or empty.");
            return false;
        }
        if (TextUtils.isEmpty(mRoleHolderUpdaterPackageName)) {
            ProvisionLogger.logi("Not starting role holder updater, because the role holder "
                    + "updater package name is null or empty.");
            return false;
        }
        return mPackageInstallChecker.isPackageInstalled(
                mRoleHolderUpdaterPackageName, context.getPackageManager());
    }

    /**
     * Creates an intent to be used to launch the role holder updater.
     */
    public Intent createRoleHolderUpdaterIntent() {
        if (TextUtils.isEmpty(mRoleHolderUpdaterPackageName)) {
            throw new IllegalStateException("Role holder updater package name is null or empty.");
        }
        return new Intent(DevicePolicyManager.ACTION_UPDATE_DEVICE_MANAGEMENT_ROLE_HOLDER)
                .setPackage(mRoleHolderUpdaterPackageName);
    }
}
