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

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;

import com.android.managedprovisioning.provisioning.Constants;

/**
 * Helper class for logic related to device management role holder updater launching.
 */
public class DeviceManagementRoleHolderUpdaterHelper {

    private final String mRoleHolderUpdaterPackageName;
    private final PackageInstallChecker mPackageInstallChecker;

    public DeviceManagementRoleHolderUpdaterHelper(
            String roleHolderUpdaterPackageName,
            PackageInstallChecker packageInstallChecker) {
        mRoleHolderUpdaterPackageName = requireNonNull(roleHolderUpdaterPackageName);
        mPackageInstallChecker = requireNonNull(packageInstallChecker);
        if (mRoleHolderUpdaterPackageName.isEmpty()) {
            throw new IllegalArgumentException("Role holder updater package name cannot be empty.");
        }
    }

    /**
     * Returns whether the device management role holder updater should be started.
     */
    public boolean shouldStartRoleHolderUpdater(Context context) {
        if (!Constants.FLAG_DEFER_PROVISIONING_TO_ROLE_HOLDER) {
            return false;
        }
        return mPackageInstallChecker.isPackageInstalled(
                mRoleHolderUpdaterPackageName, context.getPackageManager());
    }

    /**
     * Creates an intent to be used to launch the role holder updater.
     */
    public Intent createRoleHolderUpdaterIntent() {
        return new Intent(DevicePolicyManager.ACTION_UPDATE_DEVICE_MANAGEMENT_ROLE_HOLDER)
                .setPackage(mRoleHolderUpdaterPackageName);
    }
}
