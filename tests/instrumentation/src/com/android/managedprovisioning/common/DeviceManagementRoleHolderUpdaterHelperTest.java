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

import static com.android.managedprovisioning.TestUtils.assertIntentsEqual;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.Assert.assertThrows;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.SmallTest;

import com.android.managedprovisioning.provisioning.Constants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@SmallTest
@RunWith(JUnit4.class)
public class DeviceManagementRoleHolderUpdaterHelperTest {

    private static final String ROLE_HOLDER_UPDATER_PACKAGE_NAME = "com.test.package";
    private static final String ROLE_HOLDER_UPDATER_EMPTY_PACKAGE_NAME = "";
    private static final String ROLE_HOLDER_UPDATER_NULL_PACKAGE_NAME = null;
    private static final Intent ROLE_HOLDER_UPDATER_INTENT =
            new Intent(DevicePolicyManager.ACTION_UPDATE_DEVICE_MANAGEMENT_ROLE_HOLDER)
                    .setPackage(ROLE_HOLDER_UPDATER_PACKAGE_NAME);

    private final Context mContext = ApplicationProvider.getApplicationContext();

    @Before
    public void setUp() {
        enableRoleHolderDelegation();
    }

    @Test
    public void roleHolderHelperConstructor_roleHolderPackageNameNull_throwsException() {
        assertThrows(NullPointerException.class, () ->
                createRoleHolderUpdaterHelperWithPackageName(
                        ROLE_HOLDER_UPDATER_NULL_PACKAGE_NAME));
    }

    @Test
    public void roleHolderHelperConstructor_roleHolderPackageNameEmpty_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                createRoleHolderUpdaterHelperWithPackageName(
                        ROLE_HOLDER_UPDATER_EMPTY_PACKAGE_NAME));
    }

    @Test
    public void shouldStartRoleHolderUpdater_works() {
        DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper =
                createRoleHolderUpdaterHelper();

        assertThat(roleHolderUpdaterHelper.shouldStartRoleHolderUpdater(mContext)).isTrue();
    }

    @Test
    public void shouldStartRoleHolderUpdater_roleHolderDelegationDisabled_returnsFalse() {
        disableRoleHolderDelegation();
        DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper =
                createRoleHolderUpdaterHelper();

        assertThat(roleHolderUpdaterHelper.shouldStartRoleHolderUpdater(mContext)).isFalse();
    }

    @Test
    public void shouldStartRoleHolderUpdater_roleHolderUpdaterNotInstalled_returnsFalse() {
        disableRoleHolderDelegation();
        DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper =
                createRoleHolderUpdaterHelperWithUpdaterNotInstalled();

        assertThat(roleHolderUpdaterHelper.shouldStartRoleHolderUpdater(mContext)).isFalse();
    }

    @Test
    public void createRoleHolderUpdaterIntent_works() {
        DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper =
                createRoleHolderUpdaterHelper();

        assertIntentsEqual(
                roleHolderUpdaterHelper.createRoleHolderUpdaterIntent(),
                ROLE_HOLDER_UPDATER_INTENT);
    }

    private DeviceManagementRoleHolderUpdaterHelper createRoleHolderUpdaterHelperWithPackageName(
            String packageName) {
        return new DeviceManagementRoleHolderUpdaterHelper(
                packageName,
                /* packageInstallChecker= */ (roleHolderPackageName, packageManager) -> true);
    }

    private DeviceManagementRoleHolderUpdaterHelper createRoleHolderUpdaterHelper() {
        return new DeviceManagementRoleHolderUpdaterHelper(
                ROLE_HOLDER_UPDATER_PACKAGE_NAME,
                /* packageInstallChecker= */ (roleHolderPackageName, packageManager) -> true);
    }

    private DeviceManagementRoleHolderUpdaterHelper
            createRoleHolderUpdaterHelperWithUpdaterNotInstalled() {
        return new DeviceManagementRoleHolderUpdaterHelper(
                ROLE_HOLDER_UPDATER_PACKAGE_NAME,
                /* packageInstallChecker= */ (roleHolderPackageName, packageManager) -> false);
    }

    private void enableRoleHolderDelegation() {
        Constants.FLAG_DEFER_PROVISIONING_TO_ROLE_HOLDER = true;
    }

    private void disableRoleHolderDelegation() {
        Constants.FLAG_DEFER_PROVISIONING_TO_ROLE_HOLDER = false;
    }
}
