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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@SmallTest
@RunWith(JUnit4.class)
public class DeviceManagementRoleHolderUpdaterHelperTest {

    private static final String ROLE_HOLDER_UPDATER_PACKAGE_NAME = "com.test.updater.package";
    private static final String ROLE_HOLDER_PACKAGE_NAME = "com.test.roleholder.package";
    private static final String ROLE_HOLDER_UPDATER_EMPTY_PACKAGE_NAME = "";
    private static final String ROLE_HOLDER_UPDATER_NULL_PACKAGE_NAME = null;
    private static final String ROLE_HOLDER_EMPTY_PACKAGE_NAME = "";
    private static final String ROLE_HOLDER_NULL_PACKAGE_NAME = null;
    private static final Intent ROLE_HOLDER_UPDATER_INTENT =
            new Intent(DevicePolicyManager.ACTION_UPDATE_DEVICE_MANAGEMENT_ROLE_HOLDER)
                    .setPackage(ROLE_HOLDER_UPDATER_PACKAGE_NAME);

    private final Context mContext = ApplicationProvider.getApplicationContext();
    private boolean mCanDelegateProvisioningToRoleHolder;

    @Before
    public void setUp() {
        enableRoleHolderDelegation();
    }

    @Test
    public void roleHolderHelperConstructor_roleHolderPackageNameNull_noExceptionThrown() {
        createRoleHolderUpdaterHelperWithUpdaterPackageName(ROLE_HOLDER_UPDATER_NULL_PACKAGE_NAME);
    }

    @Test
    public void roleHolderHelperConstructor_roleHolderPackageNameEmpty_noExceptionThrown() {
        createRoleHolderUpdaterHelperWithUpdaterPackageName(ROLE_HOLDER_UPDATER_EMPTY_PACKAGE_NAME);
    }

    @Test
    public void shouldStartRoleHolderUpdater_works() {
        DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper =
                createRoleHolderUpdaterHelper();

        assertThat(roleHolderUpdaterHelper.shouldStartRoleHolderUpdater(mContext)).isTrue();
    }

    @Test
    public void shouldStartRoleHolderUpdater_nullRoleHolderPackageName_returnsFalse() {
        DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper =
                createRoleHolderUpdaterHelperWithRoleHolderPackageName(
                        ROLE_HOLDER_NULL_PACKAGE_NAME);

        assertThat(roleHolderUpdaterHelper.shouldStartRoleHolderUpdater(mContext)).isFalse();
    }

    @Test
    public void shouldStartRoleHolderUpdater_emptyRoleHolderPackageName_returnsFalse() {
        DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper =
                createRoleHolderUpdaterHelperWithRoleHolderPackageName(
                        ROLE_HOLDER_EMPTY_PACKAGE_NAME);

        assertThat(roleHolderUpdaterHelper.shouldStartRoleHolderUpdater(mContext)).isFalse();
    }

    @Test
    public void shouldStartRoleHolderUpdater_nullRoleHolderUpdaterPackageName_returnsFalse() {
        DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper =
                createRoleHolderUpdaterHelperWithUpdaterPackageName(
                        ROLE_HOLDER_UPDATER_NULL_PACKAGE_NAME);

        assertThat(roleHolderUpdaterHelper.shouldStartRoleHolderUpdater(mContext)).isFalse();
    }

    @Test
    public void shouldStartRoleHolderUpdater_emptyRoleHolderUpdaterPackageName_returnsFalse() {
        DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper =
                createRoleHolderUpdaterHelperWithUpdaterPackageName(
                        ROLE_HOLDER_UPDATER_EMPTY_PACKAGE_NAME);

        assertThat(roleHolderUpdaterHelper.shouldStartRoleHolderUpdater(mContext)).isFalse();
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
                roleHolderUpdaterHelper.createRoleHolderUpdaterIntent(
                        /* parentActivityIntent= */ null),
                ROLE_HOLDER_UPDATER_INTENT);
    }

    @Test
    public void createRoleHolderUpdaterIntent_nullRoleHolderUpdaterPackageName_throwsException() {
        DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper =
                createRoleHolderUpdaterHelperWithUpdaterPackageName(
                        ROLE_HOLDER_UPDATER_NULL_PACKAGE_NAME);

        assertThrows(IllegalStateException.class,
                () -> roleHolderUpdaterHelper.createRoleHolderUpdaterIntent(
                        /* parentActivityIntent= */ null));
    }

    @Test
    public void createRoleHolderUpdaterIntent_emptyRoleHolderUpdaterPackageName_throwsException() {
        DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper =
                createRoleHolderUpdaterHelperWithUpdaterPackageName(
                        ROLE_HOLDER_UPDATER_EMPTY_PACKAGE_NAME);

        assertThrows(IllegalStateException.class,
                () -> roleHolderUpdaterHelper.createRoleHolderUpdaterIntent(
                        /* parentActivityIntent= */ null));
    }

    private FeatureFlagChecker createFeatureFlagChecker() {
        return () -> mCanDelegateProvisioningToRoleHolder;
    }

    private DeviceManagementRoleHolderUpdaterHelper
    createRoleHolderUpdaterHelperWithUpdaterPackageName(
            String packageName) {
        return new DeviceManagementRoleHolderUpdaterHelper(
                packageName,
                ROLE_HOLDER_PACKAGE_NAME,
                /* packageInstallChecker= */ (roleHolderPackageName, packageManager) -> true,
                createFeatureFlagChecker());
    }

    private DeviceManagementRoleHolderUpdaterHelper
    createRoleHolderUpdaterHelperWithRoleHolderPackageName(
            String roleHolderPackageName) {
        return new DeviceManagementRoleHolderUpdaterHelper(
                ROLE_HOLDER_UPDATER_PACKAGE_NAME,
                roleHolderPackageName,
                /* packageInstallChecker= */ (packageName, packageManager) -> true,
                createFeatureFlagChecker());
    }

    private DeviceManagementRoleHolderUpdaterHelper createRoleHolderUpdaterHelper() {
        return new DeviceManagementRoleHolderUpdaterHelper(
                ROLE_HOLDER_UPDATER_PACKAGE_NAME,
                ROLE_HOLDER_PACKAGE_NAME,
                /* packageInstallChecker= */ (roleHolderPackageName, packageManager) -> true,
                createFeatureFlagChecker());
    }

    private DeviceManagementRoleHolderUpdaterHelper
            createRoleHolderUpdaterHelperWithUpdaterNotInstalled() {
        return new DeviceManagementRoleHolderUpdaterHelper(
                ROLE_HOLDER_UPDATER_PACKAGE_NAME,
                ROLE_HOLDER_PACKAGE_NAME,
                /* packageInstallChecker= */ (roleHolderPackageName, packageManager) -> false,
                createFeatureFlagChecker());
    }

    private void enableRoleHolderDelegation() {
        mCanDelegateProvisioningToRoleHolder = true;
    }

    private void disableRoleHolderDelegation() {
        mCanDelegateProvisioningToRoleHolder = false;
    }
}
